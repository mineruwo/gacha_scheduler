package com.gacha.gachascheduler.dto;

import lombok.Data;

@Data
public class CommentRequestDto {
    private String content;
    private Long parentCommentId;
}
