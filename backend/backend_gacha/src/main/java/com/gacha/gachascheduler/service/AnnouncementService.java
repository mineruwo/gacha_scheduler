package com.gacha.gachascheduler.service;

import com.gacha.gachascheduler.dto.AnnouncementRequestDto;
import com.gacha.gachascheduler.entity.AnnouncementEntity;
import com.gacha.gachascheduler.enums.AnnouncementType;
import com.gacha.gachascheduler.repository.AnnouncementRepository;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 파트 06 Phase 2 — 사이트 공지사항(NOTICE)/팝업 배너(POPUP) 관리. */
@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    @Transactional(readOnly = true)
    public List<AnnouncementEntity> getCurrentlyActive(AnnouncementType type) {
        return announcementRepository.findCurrentlyActive(type, OffsetDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<AnnouncementEntity> getAll(AnnouncementType type) {
        return announcementRepository.findAllByOptionalType(type);
    }

    @Transactional
    public AnnouncementEntity create(AnnouncementRequestDto request) {
        AnnouncementEntity entity = new AnnouncementEntity();
        applyRequest(entity, request);
        return announcementRepository.save(entity);
    }

    @Transactional
    public AnnouncementEntity update(Long id, AnnouncementRequestDto request) {
        AnnouncementEntity entity = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Announcement not found with id " + id));
        applyRequest(entity, request);
        return announcementRepository.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        announcementRepository.deleteById(id);
    }

    private void applyRequest(AnnouncementEntity entity, AnnouncementRequestDto request) {
        entity.setType(request.getType());
        entity.setTitle(request.getTitle());
        entity.setContent(request.getContent());
        entity.setImageUrl(request.getImageUrl());
        entity.setLinkUrl(request.getLinkUrl());
        entity.setStartAt(request.getStartAt());
        entity.setEndAt(request.getEndAt());
        entity.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
    }
}
