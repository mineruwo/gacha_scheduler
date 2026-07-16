package com.gacha.gachascheduler.service;

import com.gacha.gachascheduler.dto.BannerCharacterResponseDto;
import java.util.List;

public record PullOutcome(List<BannerCharacterResponseDto> results, int pityCount) {
}
