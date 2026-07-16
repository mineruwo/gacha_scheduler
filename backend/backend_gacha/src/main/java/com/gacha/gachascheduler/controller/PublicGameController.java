package com.gacha.gachascheduler.controller;

import com.gacha.gachascheduler.dto.GameMapper;
import com.gacha.gachascheduler.dto.GameResponseDto;
import com.gacha.gachascheduler.service.GameService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그인 여부와 관계없이 누구나 조회 가능한 게임 목록 (스케줄러/시뮬레이터 화면용).
 * 게임 등록/수정/삭제는 {@link GameController}(관리자 전용)를 사용한다.
 */
@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class PublicGameController {

    private final GameService gameService;

    @GetMapping
    public ResponseEntity<List<GameResponseDto>> getAllGames() {
        List<GameResponseDto> games = gameService.getAllGames().stream()
                .map(GameMapper::toDto)
                .toList();
        return ResponseEntity.ok(games);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameResponseDto> getGameById(@PathVariable Long id) {
        return gameService.getGameById(id)
                .map(GameMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
