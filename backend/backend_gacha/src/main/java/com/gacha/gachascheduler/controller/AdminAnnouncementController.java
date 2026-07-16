package com.gacha.gachascheduler.controller;

import com.gacha.gachascheduler.dto.AnnouncementMapper;
import com.gacha.gachascheduler.dto.AnnouncementRequestDto;
import com.gacha.gachascheduler.dto.AnnouncementResponseDto;
import com.gacha.gachascheduler.enums.AnnouncementType;
import com.gacha.gachascheduler.service.AnnouncementService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 파트 06 Phase 2 — 관리자용 공지사항/팝업 배너 CRUD. 활성 기간과 무관하게 전체 목록을 관리한다. */
@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUB_ADMIN', 'MAIN_ADMIN')")
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<List<AnnouncementResponseDto>> getAllAnnouncements(
            @RequestParam(required = false) AnnouncementType type) {
        List<AnnouncementResponseDto> response = announcementService.getAll(type).stream()
                .map(AnnouncementMapper::toDto)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<AnnouncementResponseDto> createAnnouncement(@RequestBody AnnouncementRequestDto request) {
        AnnouncementResponseDto created = AnnouncementMapper.toDto(announcementService.create(request));
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnnouncementResponseDto> updateAnnouncement(
            @PathVariable Long id, @RequestBody AnnouncementRequestDto request) {
        return ResponseEntity.ok(AnnouncementMapper.toDto(announcementService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
