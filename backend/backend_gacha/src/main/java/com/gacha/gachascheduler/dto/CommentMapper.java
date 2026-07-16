package com.gacha.gachascheduler.dto;

import com.gacha.gachascheduler.entity.CommentEntity;
import com.gacha.gachascheduler.entity.UserEntity;

/** CommentEntity는 author 연관관계를 지연 로딩으로 들고 있지 않으므로 호출 측이 명시적으로 전달한다. */
public final class CommentMapper {

    private CommentMapper() {
    }

    public static CommentResponseDto toDto(CommentEntity entity, UserEntity author) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(entity.getId());
        dto.setPostId(entity.getPostId());
        dto.setAuthorId(entity.getAuthorId());
        dto.setAuthorName(author != null ? author.getName() : null);
        dto.setContent(entity.getContent());
        dto.setParentCommentId(entity.getParentCommentId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
