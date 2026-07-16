package com.gacha.gachascheduler.controller;

import com.gacha.gachascheduler.dto.ChannelResponseDto;
import com.gacha.gachascheduler.service.ChannelService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 채널 목록 조회는 로그인 여부와 관계없이 누구나 가능하다. 등록/수정/삭제는 {@link ChannelController}(관리자 전용). */
@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class PublicChannelController {

    private final ChannelService channelService;

    @GetMapping
    public ResponseEntity<List<ChannelResponseDto>> getChannels(
            @RequestParam(required = false) Long gameId) {
        return ResponseEntity.ok(channelService.getChannels(gameId));
    }
}
