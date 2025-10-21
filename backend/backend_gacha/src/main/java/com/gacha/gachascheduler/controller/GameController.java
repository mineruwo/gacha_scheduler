package com.gacha.gachascheduler.controller;

import com.gacha.gachascheduler.dto.GameRequestDto;
import com.gacha.gachascheduler.dto.GameResponseDto;
import com.gacha.gachascheduler.entity.GameEntity;
import com.gacha.gachascheduler.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PreAuthorize("hasAnyRole('SUB_ADMIN', 'MAIN_ADMIN')")
    @PostMapping
    public ResponseEntity<GameResponseDto> createGame(@RequestBody GameRequestDto gameRequestDto) {
        GameEntity gameEntity = new GameEntity();
        gameEntity.setTitle(gameRequestDto.getTitle());
        gameEntity.setGameCode(gameRequestDto.getGameCode());
        gameEntity.setHasGacha(gameRequestDto.getHasGacha());
        gameEntity.setHasPass(gameRequestDto.getHasPass());
        gameEntity.setCanRecord(gameRequestDto.getCanRecord());
        gameEntity.setIsService(gameRequestDto.getIsService());
        gameEntity.setComment(gameRequestDto.getComment());
        gameEntity.setCanManageSchedule(gameRequestDto.getCanManageSchedule());
        gameEntity.setCanTrackCurrency(gameRequestDto.getCanTrackCurrency());

        GameEntity createdGame = gameService.createGame(gameEntity);
        return new ResponseEntity<>(convertToDto(createdGame), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('SUB_ADMIN', 'MAIN_ADMIN')")
    @GetMapping
    public ResponseEntity<List<GameResponseDto>> getAllGames() {
        List<GameEntity> games = gameService.getAllGames();
        List<GameResponseDto> gameDtos = games.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(gameDtos);
    }

    @PreAuthorize("hasAnyRole('SUB_ADMIN', 'MAIN_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<GameResponseDto> getGameById(@PathVariable Long id) {
        return gameService.getGameById(id)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('SUB_ADMIN', 'MAIN_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<GameResponseDto> updateGame(@PathVariable Long id, @RequestBody GameRequestDto gameRequestDto) {
        GameEntity gameEntity = new GameEntity();
        gameEntity.setTitle(gameRequestDto.getTitle());
        gameEntity.setHasGacha(gameRequestDto.getHasGacha());
        gameEntity.setHasPass(gameRequestDto.getHasPass());
        gameEntity.setCanRecord(gameRequestDto.getCanRecord());
        gameEntity.setIsService(gameRequestDto.getIsService());
        gameEntity.setComment(gameRequestDto.getComment());
        gameEntity.setCanManageSchedule(gameRequestDto.getCanManageSchedule());
        gameEntity.setCanTrackCurrency(gameRequestDto.getCanTrackCurrency());

        GameEntity updatedGame = gameService.updateGame(id, gameEntity);
        return ResponseEntity.ok(convertToDto(updatedGame));
    }

    @PreAuthorize("hasAnyRole('SUB_ADMIN', 'MAIN_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable Long id) {
        gameService.deleteGame(id);
        return ResponseEntity.noContent().build();
    }

    private GameResponseDto convertToDto(GameEntity gameEntity) {
        GameResponseDto dto = new GameResponseDto();
        dto.setId(gameEntity.getId());
        dto.setTitle(gameEntity.getTitle());
        dto.setGameCode(gameEntity.getGameCode());
        dto.setHasGacha(gameEntity.getHasGacha());
        dto.setHasPass(gameEntity.getHasPass());
        dto.setCanRecord(gameEntity.getCanRecord());
        dto.setIsService(gameEntity.getIsService());
        dto.setComment(gameEntity.getComment());
        dto.setCanManageSchedule(gameEntity.getCanManageSchedule());
        dto.setCanTrackCurrency(gameEntity.getCanTrackCurrency());
        dto.setCreatedAt(gameEntity.getCreatedAt());
        dto.setUpdatedAt(gameEntity.getUpdatedAt());
        return dto;
    }
}
