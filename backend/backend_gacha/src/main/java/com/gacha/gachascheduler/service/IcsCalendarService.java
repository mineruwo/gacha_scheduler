package com.gacha.gachascheduler.service;

import com.gacha.gachascheduler.entity.GameEntity;
import com.gacha.gachascheduler.entity.ScheduleEventEntity;
import com.gacha.gachascheduler.entity.UserEntity;
import com.gacha.gachascheduler.entity.UserGamePreferenceEntity;
import com.gacha.gachascheduler.repository.GameRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 파트 07(확장 기능) — iCal/ICS 구독 피드 생성. docs/plans/07-extended-features-planning.md 2번 항목.
 * 유저의 관심 게임 필터(UserGamePreference)에 해당하는 일정만 RFC 5545 텍스트로 변환한다.
 * 필터가 비어있으면(선택 없음) 스케줄러 페이지와 동일한 컨벤션으로 전체 게임 일정을 담는다.
 */
@Service
@RequiredArgsConstructor
public class IcsCalendarService {

    private static final DateTimeFormatter ICS_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    private final UserService userService;
    private final UserGamePreferenceService userGamePreferenceService;
    private final ScheduleEventService scheduleEventService;
    private final GameRepository gameRepository;

    @Transactional(readOnly = true)
    public String generateIcsForUserCode(String userCode) {
        UserEntity user = userService.findUserByUserCode(userCode)
                .orElseThrow(() -> new RuntimeException("User not found with userCode " + userCode));

        List<String> gameCodes = userGamePreferenceService.getUserPreferences(user.getId()).stream()
                .map(UserGamePreferenceEntity::getGameCode)
                .toList();

        // 캘린더 앱이 주기적으로 재요청하는 구독 피드이므로 최근 과거~향후 일정을 넉넉히 담되 무한정 누적되지 않게 범위를 둔다.
        OffsetDateTime from = OffsetDateTime.now().minusMonths(3);
        OffsetDateTime to = OffsetDateTime.now().plusMonths(12);
        List<ScheduleEventEntity> events = scheduleEventService.getSchedules(gameCodes, from, to);

        Map<String, GameEntity> gamesByCode = gameRepository
                .findByGameCodeIn(events.stream().map(ScheduleEventEntity::getGameCode).distinct().toList())
                .stream()
                .collect(Collectors.toMap(GameEntity::getGameCode, Function.identity()));

        String dtstamp = ICS_DATE_TIME.format(OffsetDateTime.now().toInstant());

        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//Gacha Scheduler//Calendar Feed//KO\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("X-WR-CALNAME:").append(escape("가챠 스케줄러")).append("\r\n");

        for (ScheduleEventEntity event : events) {
            GameEntity game = gamesByCode.get(event.getGameCode());
            String gameTitle = game != null ? game.getTitle() : event.getGameCode();

            sb.append("BEGIN:VEVENT\r\n");
            sb.append("UID:schedule-").append(event.getId()).append("@gacha-scheduler\r\n");
            sb.append("DTSTAMP:").append(dtstamp).append("\r\n");
            sb.append("DTSTART:").append(ICS_DATE_TIME.format(event.getStartAt().toInstant())).append("\r\n");
            if (event.getEndAt() != null) {
                sb.append("DTEND:").append(ICS_DATE_TIME.format(event.getEndAt().toInstant())).append("\r\n");
            }
            sb.append("SUMMARY:").append(escape("[" + gameTitle + "] " + event.getTitle())).append("\r\n");
            if (event.getDescription() != null && !event.getDescription().isBlank()) {
                sb.append("DESCRIPTION:").append(escape(event.getDescription())).append("\r\n");
            }
            sb.append("END:VEVENT\r\n");
        }

        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    private String escape(String text) {
        return text.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,").replace("\n", "\\n");
    }
}
