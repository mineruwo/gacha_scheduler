package com.gacha.gachascheduler.controller;

import com.gacha.gachascheduler.dto.CharacterMapper;
import com.gacha.gachascheduler.dto.CharacterRequestDto;
import com.gacha.gachascheduler.dto.CharacterResponseDto;
import com.gacha.gachascheduler.entity.CharacterEntity;
import com.gacha.gachascheduler.service.CharacterService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/characters")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUB_ADMIN', 'MAIN_ADMIN')")
public class CharacterController {

    private final CharacterService characterService;

    @PostMapping
    public ResponseEntity<CharacterResponseDto> createCharacter(@RequestBody CharacterRequestDto request) {
        CharacterEntity entity = new CharacterEntity();
        entity.setGameId(request.getGameId());
        entity.setName(request.getName());
        entity.setRarity(request.getRarity());
        entity.setIconUrl(request.getIconUrl());

        CharacterEntity created = characterService.createCharacter(entity);
        return new ResponseEntity<>(CharacterMapper.toDto(created), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CharacterResponseDto>> getCharactersByGame(@RequestParam Long gameId) {
        List<CharacterResponseDto> characters = characterService.getCharactersByGame(gameId).stream()
                .map(CharacterMapper::toDto)
                .toList();
        return ResponseEntity.ok(characters);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CharacterResponseDto> updateCharacter(
            @PathVariable Long id, @RequestBody CharacterRequestDto request) {
        CharacterEntity entity = new CharacterEntity();
        entity.setName(request.getName());
        entity.setRarity(request.getRarity());
        entity.setIconUrl(request.getIconUrl());

        CharacterEntity updated = characterService.updateCharacter(id, entity);
        return ResponseEntity.ok(CharacterMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharacter(@PathVariable Long id) {
        characterService.deleteCharacter(id);
        return ResponseEntity.noContent().build();
    }
}
