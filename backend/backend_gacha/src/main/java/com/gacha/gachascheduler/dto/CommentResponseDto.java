package com.gacha.gachascheduler.dto;

import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class CommentResponseDto {
    private Long id;
    private Long postId;
    private Long authorId;
    private String authorName;
    private String content;
    private Long parentCommentId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
