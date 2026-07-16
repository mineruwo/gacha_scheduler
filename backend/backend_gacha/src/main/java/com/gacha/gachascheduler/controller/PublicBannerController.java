package com.gacha.gachascheduler.controller;

import com.gacha.gachascheduler.dto.BannerCharacterResponseDto;
import com.gacha.gachascheduler.dto.BannerResponseDto;
import com.gacha.gachascheduler.dto.PullRequestDto;
import com.gacha.gachascheduler.dto.PullResponseDto;
import com.gacha.gachascheduler.service.BannerService;
import com.gacha.gachascheduler.service.PullOutcome;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 가챠 배너/캐릭터 조회 및 뽑기는 로그인 여부와 관계없이 누구나 가능하다(SYNC.md 계약).
 * 뽑기 확률 계산은 전부 서버(BannerService)에서 수행하고, 천장 카운트는 요청/응답으로 클라이언트가
 * 직접 들고 다닌다(로그인 유저의 서버 측 영속 카운트는 아직 미도입 — docs/plans/04-gacha-simulator.md 참고).
 */
@RestController
@RequiredArgsConstructor
public class PublicBannerController {

    private final BannerService bannerService;

    @GetMapping("/api/games/{gameId}/banners")
    public ResponseEntity<List<BannerResponseDto>> getBannersByGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(bannerService.getBannersByGame(gameId));
    }

    @GetMapping("/api/banners")
    public ResponseEntity<List<BannerResponseDto>> getAllBanners() {
        return ResponseEntity.ok(bannerService.getAllBanners());
    }

    @GetMapping("/api/banners/{bannerId}/characters")
    public ResponseEntity<List<BannerCharacterResponseDto>> getBannerCharacters(@PathVariable Long bannerId) {
        return ResponseEntity.ok(bannerService.getBannerCharacters(bannerId));
    }

    @PostMapping("/api/banners/{bannerId}/pull")
    public ResponseEntity<PullResponseDto> pull(@PathVariable Long bannerId, @RequestBody PullRequestDto request) {
        int count = request.getCount() != null ? request.getCount() : 1;
        int currentPity = request.getCurrentPity() != null ? request.getCurrentPity() : 0;

        PullOutcome outcome = bannerService.pull(bannerId, count, currentPity);

        PullResponseDto response = new PullResponseDto();
        response.setResults(outcome.results());
        response.setPityCount(outcome.pityCount());
        return ResponseEntity.ok(response);
    }
}
