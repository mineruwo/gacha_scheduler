package com.gacha.gachascheduler.service;

import com.gacha.gachascheduler.dto.BannerCharacterResponseDto;
import com.gacha.gachascheduler.dto.BannerMapper;
import com.gacha.gachascheduler.dto.BannerResponseDto;
import com.gacha.gachascheduler.entity.BannerCharacterEntity;
import com.gacha.gachascheduler.entity.BannerCharacterId;
import com.gacha.gachascheduler.entity.BannerEntity;
import com.gacha.gachascheduler.entity.CharacterEntity;
import com.gacha.gachascheduler.entity.GameEntity;
import com.gacha.gachascheduler.repository.BannerCharacterRepository;
import com.gacha.gachascheduler.repository.BannerRepository;
import com.gacha.gachascheduler.repository.CharacterRepository;
import com.gacha.gachascheduler.repository.GameRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 배너/캐릭터 풀 관리 + 가챠 뽑기 로직.
 *
 * BannerEntity/CharacterEntity/BannerCharacterEntity는 game/character 연관관계를 지연 로딩 필드로
 * 들고 있지 않는다(참조 컬럼이 대상 테이블의 PK가 아니거나, 복합키 엔티티라 Hibernate가 프록시를 만들지
 * 못해 null이 채워지는 문제가 있었음). 그래서 이 서비스는 항상 gameId/characterId로 관련 엔티티를
 * 명시적으로 배치 조회한 뒤 DTO를 조립한다.
 *
 * 뽑기 규칙(단순화된 확정 천장 + 픽업 rate-up 모델):
 * 1. 매 뽑기마다 pity를 1 증가시킨다. pity가 배너의 pityThreshold 이상이면 이번 뽑기는 "확정"이다.
 * 2. 확정이 아니면, 배너 풀의 가중치 합 중 최고 등급(rarity 최댓값) 캐릭터들의 가중치 비율만큼의 확률로 "최고 등급 적중"을 굴린다.
 * 3. 확정이거나 최고 등급 적중이면: 픽업 캐릭터가 있을 경우 rateUpRate 확률로 픽업 중, 그렇지 않으면 비픽업 최고 등급 중 가중치 비례로 선택하고 pity를 0으로 리셋한다.
 * 4. 최고 등급 적중이 아니면: 최고 등급을 제외한 나머지 풀에서 가중치 비례로 선택한다.
 */
@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;
    private final BannerCharacterRepository bannerCharacterRepository;
    private final CharacterRepository characterRepository;
    private final GameRepository gameRepository;
    private final Random random = new Random();

    @Transactional
    public BannerResponseDto createBanner(BannerEntity banner) {
        GameEntity game = requireExistingGame(banner.getGameId());
        BannerEntity created = bannerRepository.save(banner);
        return BannerMapper.toDto(created, game, List.of());
    }

    @Transactional
    public BannerResponseDto updateBanner(Long id, BannerEntity updated) {
        BannerEntity banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found with id " + id));
        banner.setName(updated.getName());
        banner.setStartAt(updated.getStartAt());
        banner.setEndAt(updated.getEndAt());
        banner.setPityThreshold(updated.getPityThreshold());
        banner.setRateUpRate(updated.getRateUpRate());
        BannerEntity saved = bannerRepository.save(banner);

        GameEntity game = gameRepository.findById(saved.getGameId()).orElse(null);
        List<BannerCharacterEntity> pool = bannerCharacterRepository.findByBannerId(id);
        return BannerMapper.toDto(saved, game, pool);
    }

    @Transactional
    public void deleteBanner(Long id) {
        bannerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<BannerResponseDto> getBannersByGame(Long gameId) {
        return toResponseDtos(bannerRepository.findByGameId(gameId));
    }

    @Transactional(readOnly = true)
    public List<BannerResponseDto> getAllBanners() {
        return toResponseDtos(bannerRepository.findAll());
    }

    @Transactional(readOnly = true)
    public Optional<BannerResponseDto> getBannerById(Long id) {
        return bannerRepository.findById(id).map(banner -> {
            GameEntity game = gameRepository.findById(banner.getGameId()).orElse(null);
            List<BannerCharacterEntity> pool = bannerCharacterRepository.findByBannerId(id);
            return BannerMapper.toDto(banner, game, pool);
        });
    }

    @Transactional(readOnly = true)
    public List<BannerCharacterResponseDto> getBannerCharacters(Long bannerId) {
        List<BannerCharacterEntity> pool = bannerCharacterRepository.findByBannerId(bannerId);
        Map<Long, CharacterEntity> charactersById = loadCharactersById(pool);
        return pool.stream()
                .map(bc -> BannerMapper.toCharacterDto(bc, charactersById.get(bc.getCharacterId())))
                .toList();
    }

    @Transactional
    public BannerCharacterResponseDto upsertPoolCharacter(Long bannerId, Long characterId, double weight, boolean isPickup) {
        requireExistingBanner(bannerId);
        CharacterEntity character = characterRepository.findById(characterId)
                .orElseThrow(() -> new RuntimeException("Character not found with id " + characterId));

        BannerCharacterEntity entity = new BannerCharacterEntity();
        entity.setBannerId(bannerId);
        entity.setCharacterId(characterId);
        entity.setWeight(weight);
        entity.setIsPickup(isPickup);
        bannerCharacterRepository.save(entity);

        return BannerMapper.toCharacterDto(entity, character);
    }

    @Transactional
    public void removePoolCharacter(Long bannerId, Long characterId) {
        bannerCharacterRepository.deleteById(new BannerCharacterId(bannerId, characterId));
    }

    @Transactional(readOnly = true)
    public PullOutcome pull(Long bannerId, int count, int currentPity) {
        BannerEntity banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new RuntimeException("Banner not found with id " + bannerId));
        List<BannerCharacterEntity> pool = bannerCharacterRepository.findByBannerId(bannerId);
        if (pool.isEmpty()) {
            throw new RuntimeException("Banner has no characters configured: " + bannerId);
        }
        Map<Long, CharacterEntity> charactersById = loadCharactersById(pool);

        int maxRarity = pool.stream()
                .mapToInt(bc -> charactersById.get(bc.getCharacterId()).getRarity())
                .max().orElseThrow();
        List<BannerCharacterEntity> topTier = pool.stream()
                .filter(bc -> charactersById.get(bc.getCharacterId()).getRarity() == maxRarity)
                .toList();
        List<BannerCharacterEntity> pickups = topTier.stream()
                .filter(BannerCharacterEntity::getIsPickup)
                .toList();
        List<BannerCharacterEntity> nonPickupTop = topTier.stream()
                .filter(bc -> !bc.getIsPickup())
                .toList();
        List<BannerCharacterEntity> lowerTier = pool.stream()
                .filter(bc -> charactersById.get(bc.getCharacterId()).getRarity() != maxRarity)
                .toList();

        double topTierWeight = sumWeights(topTier);
        double totalWeight = sumWeights(pool);
        double naturalTopProbability = totalWeight > 0 ? topTierWeight / totalWeight : 0;

        List<BannerCharacterEntity> resultEntities = new ArrayList<>(count);
        int pity = currentPity;
        for (int i = 0; i < count; i++) {
            pity++;
            boolean guaranteed = pity >= banner.getPityThreshold();
            boolean topHit = guaranteed || random.nextDouble() < naturalTopProbability;

            BannerCharacterEntity picked;
            if (topHit) {
                picked = pickTopTierWithRateUp(pickups, nonPickupTop, topTier, banner.getRateUpRate());
                pity = 0;
            } else {
                picked = weightedRandomPick(!lowerTier.isEmpty() ? lowerTier : pool);
            }
            resultEntities.add(picked);
        }

        List<BannerCharacterResponseDto> results = resultEntities.stream()
                .map(bc -> BannerMapper.toCharacterDto(bc, charactersById.get(bc.getCharacterId())))
                .toList();
        return new PullOutcome(results, pity);
    }

    private List<BannerResponseDto> toResponseDtos(List<BannerEntity> banners) {
        Map<Long, GameEntity> gamesById = gameRepository.findAllById(
                        banners.stream().map(BannerEntity::getGameId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(GameEntity::getId, Function.identity()));

        return banners.stream()
                .map(banner -> {
                    List<BannerCharacterEntity> pool = bannerCharacterRepository.findByBannerId(banner.getId());
                    return BannerMapper.toDto(banner, gamesById.get(banner.getGameId()), pool);
                })
                .toList();
    }

    private Map<Long, CharacterEntity> loadCharactersById(List<BannerCharacterEntity> pool) {
        List<Long> characterIds = pool.stream().map(BannerCharacterEntity::getCharacterId).distinct().toList();
        return characterRepository.findAllById(characterIds).stream()
                .collect(Collectors.toMap(CharacterEntity::getId, Function.identity()));
    }

    private BannerCharacterEntity pickTopTierWithRateUp(
            List<BannerCharacterEntity> pickups,
            List<BannerCharacterEntity> nonPickupTop,
            List<BannerCharacterEntity> topTier,
            double rateUpRate) {
        if (pickups.isEmpty()) {
            return weightedRandomPick(topTier);
        }
        if (nonPickupTop.isEmpty() || random.nextDouble() < rateUpRate) {
            return weightedRandomPick(pickups);
        }
        return weightedRandomPick(nonPickupTop);
    }

    private BannerCharacterEntity weightedRandomPick(List<BannerCharacterEntity> candidates) {
        double totalWeight = sumWeights(candidates);
        if (totalWeight <= 0) {
            return candidates.get(random.nextInt(candidates.size()));
        }
        double roll = random.nextDouble() * totalWeight;
        double cumulative = 0;
        for (BannerCharacterEntity candidate : candidates) {
            cumulative += candidate.getWeight();
            if (roll < cumulative) {
                return candidate;
            }
        }
        return candidates.get(candidates.size() - 1);
    }

    private double sumWeights(List<BannerCharacterEntity> list) {
        return list.stream().mapToDouble(BannerCharacterEntity::getWeight).sum();
    }

    private GameEntity requireExistingGame(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with id " + gameId));
    }

    private void requireExistingBanner(Long bannerId) {
        if (!bannerRepository.existsById(bannerId)) {
            throw new RuntimeException("Banner not found with id " + bannerId);
        }
    }
}
