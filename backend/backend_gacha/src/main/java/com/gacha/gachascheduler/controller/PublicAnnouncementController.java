package com.gacha.gachascheduler.controller;

import com.gacha.gachascheduler.dto.AnnouncementMapper;
import com.gacha.gachascheduler.dto.AnnouncementResponseDto;
import com.gacha.gachascheduler.enums.AnnouncementType;
import com.gacha.gachascheduler.service.AnnouncementService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 현재 활성 기간(startAt~endAt)이고 isActive인 공지사항/팝업 배너만 노출. 등록/수정은 {@link AdminAnnouncementController}. */
@RestController
@RequiredArgsConstructor
public class PublicAnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping("/api/announcements")
    public ResponseEntity<List<AnnouncementResponseDto>> getActiveAnnouncements(
            @RequestParam(required = false) AnnouncementType type) {
        List<AnnouncementResponseDto> response = announcementService.getCurrentlyActive(type).stream()
                .map(AnnouncementMapper::toDto)
                .toList();
        return ResponseEntity.ok(response);
    }
}
