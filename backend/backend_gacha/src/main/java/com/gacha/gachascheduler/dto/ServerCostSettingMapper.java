package com.gacha.gachascheduler.dto;

import com.gacha.gachascheduler.entity.ServerCostSettingEntity;

public final class ServerCostSettingMapper {

    private ServerCostSettingMapper() {
    }

    public static ServerCostSettingResponseDto toDto(ServerCostSettingEntity entity) {
        ServerCostSettingResponseDto dto = new ServerCostSettingResponseDto();
        dto.setTargetAmount(entity.getTargetAmount());
        dto.setCurrentAmount(entity.getCurrentAmount());
        dto.setPercentage(calculatePercentage(entity.getTargetAmount(), entity.getCurrentAmount()));
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    /** targetAmount가 0 이하면 0%. 목표를 초과 달성해도 게이지 UI가 넘치지 않도록 100%에서 클램프한다. */
    private static int calculatePercentage(Long targetAmount, Long currentAmount) {
        if (targetAmount == null || targetAmount <= 0) {
            return 0;
        }
        long current = currentAmount != null ? currentAmount : 0L;
        long percentage = Math.round(current * 100.0 / targetAmount);
        return (int) Math.max(0, Math.min(100, percentage));
    }
}
