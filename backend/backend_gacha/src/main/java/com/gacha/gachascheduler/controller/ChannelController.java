package com.gacha.gachascheduler.controller;

import com.gacha.gachascheduler.dto.ChannelRequestDto;
import com.gacha.gachascheduler.dto.ChannelResponseDto;
import com.gacha.gachascheduler.entity.ChannelEntity;
import com.gacha.gachascheduler.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/channels")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUB_ADMIN', 'MAIN_ADMIN')")
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping
    public ResponseEntity<ChannelResponseDto> createChannel(@RequestBody ChannelRequestDto request) {
        ChannelEntity entity = new ChannelEntity();
        entity.setGameId(request.getGameId());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());

        return new ResponseEntity<>(channelService.createChannel(entity), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChannelResponseDto> updateChannel(
            @PathVariable Long id, @RequestBody ChannelRequestDto request) {
        ChannelEntity entity = new ChannelEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());

        return ResponseEntity.ok(channelService.updateChannel(id, entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChannel(@PathVariable Long id) {
        channelService.deleteChannel(id);
        return ResponseEntity.noContent().build();
    }
}
