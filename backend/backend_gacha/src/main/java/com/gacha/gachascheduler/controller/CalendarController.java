package com.gacha.gachascheduler.controller;

import com.gacha.gachascheduler.service.IcsCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * iCal 구독 피드. userCode 자체가 추측 불가능한 비밀값 역할을 하므로 별도 인증 없이 공개 접근을 허용한다
 * (캘린더 앱이 주기적으로 이 URL을 직접 호출해야 하므로 로그인 세션을 요구할 수 없음).
 */
@RestController
@RequiredArgsConstructor
public class CalendarController {

    private final IcsCalendarService icsCalendarService;

    @GetMapping(value = "/api/users/{userCode}/calendar.ics", produces = "text/calendar; charset=utf-8")
    public ResponseEntity<String> getCalendar(@PathVariable String userCode) {
        String ics = icsCalendarService.generateIcsForUserCode(userCode);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"gacha-scheduler.ics\"")
                .body(ics);
    }
}
