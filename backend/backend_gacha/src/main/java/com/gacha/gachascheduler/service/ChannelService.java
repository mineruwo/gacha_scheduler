package com.gacha.gachascheduler.service;

import com.gacha.gachascheduler.dto.ChannelMapper;
import com.gacha.gachascheduler.dto.ChannelResponseDto;
import com.gacha.gachascheduler.entity.ChannelEntity;
import com.gacha.gachascheduler.entity.GameEntity;
import com.gacha.gachascheduler.repository.ChannelRepository;
import com.gacha.gachascheduler.repository.GameRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final GameRepository gameRepository;

    @Transactional
    public ChannelResponseDto createChannel(ChannelEntity channel) {
        GameEntity game = requireExistingGame(channel.getGameId());
        ChannelEntity created = channelRepository.save(channel);
        return ChannelMapper.toDto(created, game);
    }

    @Transactional
    public ChannelResponseDto updateChannel(Long id, ChannelEntity updated) {
        ChannelEntity channel = channelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Channel not found with id " + id));
        channel.setName(updated.getName());
        channel.setDescription(updated.getDescription());
        ChannelEntity saved = channelRepository.save(channel);
        GameEntity game = gameRepository.findById(saved.getGameId()).orElse(null);
        return ChannelMapper.toDto(saved, game);
    }

    @Transactional
    public void deleteChannel(Long id) {
        channelRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ChannelResponseDto> getChannels(Long gameId) {
        List<ChannelEntity> channels = gameId != null
                ? channelRepository.findByGameId(gameId)
                : channelRepository.findAll();

        Map<Long, GameEntity> gamesById = gameRepository.findAllById(
                        channels.stream().map(ChannelEntity::getGameId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(GameEntity::getId, Function.identity()));

        return channels.stream()
                .map(channel -> ChannelMapper.toDto(channel, gamesById.get(channel.getGameId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(Long channelId) {
        return channelRepository.existsById(channelId);
    }

    private GameEntity requireExistingGame(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with id " + gameId));
    }
}
