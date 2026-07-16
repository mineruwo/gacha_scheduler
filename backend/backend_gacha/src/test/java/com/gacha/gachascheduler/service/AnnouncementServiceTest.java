package com.gacha.gachascheduler.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.gacha.gachascheduler.dto.AnnouncementRequestDto;
import com.gacha.gachascheduler.entity.AnnouncementEntity;
import com.gacha.gachascheduler.enums.AnnouncementType;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(AnnouncementService.class)
class AnnouncementServiceTest {

    @Autowired
    private AnnouncementService announcementService;

    private AnnouncementRequestDto request(
            AnnouncementType type, OffsetDateTime startAt, OffsetDateTime endAt, Boolean isActive) {
        AnnouncementRequestDto dto = new AnnouncementRequestDto();
        dto.setType(type);
        dto.setTitle("제목");
        dto.setContent("내용");
        dto.setStartAt(startAt);
        dto.setEndAt(endAt);
        dto.setIsActive(isActive);
        return dto;
    }

    @Test
    void createPersistsAnnouncementWithDefaultActiveTrue() {
        AnnouncementRequestDto dto = request(AnnouncementType.NOTICE, OffsetDateTime.now().minusDays(1), null, null);

        AnnouncementEntity created = announcementService.create(dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getIsActive()).isTrue();
    }

    @Test
    void getCurrentlyActiveExcludesAnnouncementsOutsideDateWindow() {
        OffsetDateTime now = OffsetDateTime.now();
        announcementService.create(request(AnnouncementType.NOTICE, now.minusDays(5), now.minusDays(1), true));
        AnnouncementEntity active = announcementService.create(
                request(AnnouncementType.NOTICE, now.minusDays(1), now.plusDays(5), true));
        announcementService.create(request(AnnouncementType.NOTICE, now.plusDays(1), now.plusDays(5), true));

        List<AnnouncementEntity> result = announcementService.getCurrentlyActive(AnnouncementType.NOTICE);

        assertThat(result).extracting(AnnouncementEntity::getId).containsExactly(active.getId());
    }

    @Test
    void getCurrentlyActiveExcludesManuallyDeactivatedAnnouncement() {
        OffsetDateTime now = OffsetDateTime.now();
        announcementService.create(request(AnnouncementType.POPUP, now.minusDays(1), now.plusDays(5), false));

        List<AnnouncementEntity> result = announcementService.getCurrentlyActive(AnnouncementType.POPUP);

        assertThat(result).isEmpty();
    }

    @Test
    void getCurrentlyActiveFiltersByType() {
        OffsetDateTime now = OffsetDateTime.now();
        announcementService.create(request(AnnouncementType.NOTICE, now.minusDays(1), null, true));
        AnnouncementEntity popup = announcementService.create(
                request(AnnouncementType.POPUP, now.minusDays(1), null, true));

        List<AnnouncementEntity> result = announcementService.getCurrentlyActive(AnnouncementType.POPUP);

        assertThat(result).extracting(AnnouncementEntity::getId).containsExactly(popup.getId());
    }

    @Test
    void updateChangesFieldsAndDeleteRemovesRow() {
        AnnouncementEntity created = announcementService.create(
                request(AnnouncementType.NOTICE, OffsetDateTime.now().minusDays(1), null, true));

        AnnouncementRequestDto updateDto = request(AnnouncementType.NOTICE, OffsetDateTime.now().minusDays(1), null, false);
        updateDto.setTitle("수정된 제목");
        AnnouncementEntity updated = announcementService.update(created.getId(), updateDto);

        assertThat(updated.getTitle()).isEqualTo("수정된 제목");
        assertThat(updated.getIsActive()).isFalse();

        announcementService.delete(created.getId());
        assertThat(announcementService.getAll(null)).isEmpty();
    }
}
