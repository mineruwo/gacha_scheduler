package com.gacha.gachascheduler.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.gacha.gachascheduler.dto.BannerResponseDto;
import com.gacha.gachascheduler.entity.BannerEntity;
import com.gacha.gachascheduler.entity.CharacterEntity;
import com.gacha.gachascheduler.entity.GameEntity;
import com.gacha.gachascheduler.repository.CharacterRepository;
import com.gacha.gachascheduler.repository.GameRepository;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(BannerService.class)
class BannerServiceTest {

    @Autowired
    private BannerService bannerService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CharacterRepository characterRepository;

    @Test
    void pullNeverExceedsPityThreshold() {
        Setup setup = setUpBannerWithPool(5);

        int currentPity = 0;
        for (int i = 0; i < 200; i++) {
            PullOutcome outcome = bannerService.pull(setup.bannerId(), 1, currentPity);
            currentPity = outcome.pityCount();
            assertThat(currentPity).isLessThan(5);
        }
    }

    @Test
    void guaranteedPullAtPityThresholdReturnsTopRarity() {
        Setup setup = setUpBannerWithPool(5);

        PullOutcome outcome = bannerService.pull(setup.bannerId(), 1, 4);

        assertThat(outcome.pityCount()).isZero();
        assertThat(outcome.results()).hasSize(1);
        assertThat(outcome.results().get(0).getRarity()).isEqualTo(5);
    }

    @Test
    void tenPullBatchReturnsTenResultsAndPityWithinBounds() {
        Setup setup = setUpBannerWithPool(90);

        PullOutcome outcome = bannerService.pull(setup.bannerId(), 10, 0);

        assertThat(outcome.results()).hasSize(10);
        // 자연 확률로 최고 등급을 뽑으면 도중에 pity가 0으로 리셋될 수 있으므로 정확히 10이라고 단정할 수
        // 없다 — 다만 천장(90) 미만이어야 하고, 10번 뽑았으므로 최대 10을 넘을 수는 없다.
        assertThat(outcome.pityCount()).isBetween(0, 10);
    }

    @Test
    void pullOnlyReturnsCharactersFromThatBannersPool() {
        Setup setup = setUpBannerWithPool(90);
        Set<Long> poolCharacterIds = Set.of(setup.topCharacterId(), setup.lowCharacterId());

        for (int i = 0; i < 30; i++) {
            PullOutcome outcome = bannerService.pull(setup.bannerId(), 1, 0);
            assertThat(poolCharacterIds).contains(outcome.results().get(0).getId());
        }
    }

    private Setup setUpBannerWithPool(int pityThreshold) {
        GameEntity game = new GameEntity();
        game.setTitle("Test Game");
        game.setGameCode("banner-test-game-" + pityThreshold);
        game = gameRepository.save(game);

        CharacterEntity topRarity = new CharacterEntity();
        topRarity.setGameId(game.getId());
        topRarity.setName("Top Rarity Pickup");
        topRarity.setRarity(5);
        topRarity = characterRepository.save(topRarity);

        CharacterEntity lowRarity = new CharacterEntity();
        lowRarity.setGameId(game.getId());
        lowRarity.setName("Low Rarity");
        lowRarity.setRarity(3);
        lowRarity = characterRepository.save(lowRarity);

        BannerEntity banner = new BannerEntity();
        banner.setGameId(game.getId());
        banner.setName("Test Banner");
        banner.setStartAt(java.time.OffsetDateTime.now());
        banner.setPityThreshold(pityThreshold);
        banner.setRateUpRate(0.5);
        BannerResponseDto createdBanner = bannerService.createBanner(banner);

        bannerService.upsertPoolCharacter(createdBanner.getId(), topRarity.getId(), 1.0, true);
        bannerService.upsertPoolCharacter(createdBanner.getId(), lowRarity.getId(), 10.0, false);

        return new Setup(createdBanner.getId(), topRarity.getId(), lowRarity.getId());
    }

    private record Setup(Long bannerId, Long topCharacterId, Long lowCharacterId) {
    }
}
