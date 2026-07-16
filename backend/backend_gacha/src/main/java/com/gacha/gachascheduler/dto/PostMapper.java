package com.gacha.gachascheduler.dto;

import com.gacha.gachascheduler.entity.ChannelEntity;
import com.gacha.gachascheduler.entity.PostEntity;
import com.gacha.gachascheduler.entity.UserEntity;

/** PostEntity는 channel/author 연관관계를 지연 로딩으로 들고 있지 않으므로 호출 측이 명시적으로 전달한다. */
public final class PostMapper {

    private PostMapper() {
    }

    public static PostResponseDto toDto(PostEntity entity, ChannelEntity channel, UserEntity author) {
        PostResponseDto dto = new PostResponseDto();
        dto.setId(entity.getId());
        dto.setChannelId(entity.getChannelId());
        dto.setChannelName(channel != null ? channel.getName() : null);
        dto.setAuthorId(entity.getAuthorId());
        dto.setAuthorName(author != null ? author.getName() : null);
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setTemplateType(entity.getTemplateType());
        dto.setViewCount(entity.getViewCount());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
