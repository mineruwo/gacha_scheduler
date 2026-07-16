package com.gacha.gachascheduler.service;

import com.gacha.gachascheduler.entity.CharacterEntity;
import com.gacha.gachascheduler.repository.CharacterRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepository;

    @Transactional
    public CharacterEntity createCharacter(CharacterEntity character) {
        return characterRepository.save(character);
    }

    @Transactional
    public CharacterEntity updateCharacter(Long id, CharacterEntity updated) {
        return characterRepository.findById(id).map(character -> {
            character.setName(updated.getName());
            character.setRarity(updated.getRarity());
            character.setIconUrl(updated.getIconUrl());
            return characterRepository.save(character);
        }).orElseThrow(() -> new RuntimeException("Character not found with id " + id));
    }

    @Transactional
    public void deleteCharacter(Long id) {
        characterRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<CharacterEntity> getCharactersByGame(Long gameId) {
        return characterRepository.findByGameId(gameId);
    }

    @Transactional(readOnly = true)
    public Optional<CharacterEntity> getCharacterById(Long id) {
        return characterRepository.findById(id);
    }
}
