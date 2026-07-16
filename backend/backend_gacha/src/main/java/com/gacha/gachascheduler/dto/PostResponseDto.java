package com.gacha.gachascheduler.dto;

import com.gacha.gachascheduler.enums.PostTemplateType;
import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class PostResponseDto {
    private Long id;
    private Long channelId;
    private String channelName;
    private Long authorId;
    private String authorName;
    private String title;
    private String content;
    private PostTemplateType templateType;
    private Integer viewCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
