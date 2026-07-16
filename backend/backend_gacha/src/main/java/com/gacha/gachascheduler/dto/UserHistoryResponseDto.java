package com.gacha.gachascheduler.dto;

import java.util.List;
import lombok.Data;

/**
 * 로그인 유저의 활동 이력. 가챠 뽑기 기록은 파트 04에서 서버 측 영속 저장을 하지 않기로 결정했으므로
 * (docs/plans/04-gacha-simulator.md 참고) 여기 포함하지 않는다 — 작성한 글/댓글만 집계한다.
 */
@Data
public class UserHistoryResponseDto {
    private List<PostResponseDto> posts;
    private List<CommentResponseDto> comments;
}
