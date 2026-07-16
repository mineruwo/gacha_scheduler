package com.gacha.gachascheduler.dto;

import com.gacha.gachascheduler.enums.PostTemplateType;
import lombok.Data;

@Data
public class PostRequestDto {
    private Long channelId;
    private String title;
    private String content;
    private PostTemplateType templateType;
}
