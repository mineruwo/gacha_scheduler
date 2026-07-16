package com.gacha.gachascheduler.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.gacha.gachascheduler.entity.ServerCostSettingEntity;
import org.junit.jupiter.api.Test;

class ServerCostSettingMapperTest {

    @Test
    void calculatesPercentageFromTargetAndCurrent() {
        ServerCostSettingEntity entity = new ServerCostSettingEntity();
        entity.setTargetAmount(1000L);
        entity.setCurrentAmount(780L);

        assertThat(ServerCostSettingMapper.toDto(entity).getPercentage()).isEqualTo(78);
    }

    @Test
    void percentageIsZeroWhenTargetIsZero() {
        ServerCostSettingEntity entity = new ServerCostSettingEntity();
        entity.setTargetAmount(0L);
        entity.setCurrentAmount(500L);

        assertThat(ServerCostSettingMapper.toDto(entity).getPercentage()).isZero();
    }

    @Test
    void percentageIsClampedAt100WhenCurrentExceedsTarget() {
        ServerCostSettingEntity entity = new ServerCostSettingEntity();
        entity.setTargetAmount(1000L);
        entity.setCurrentAmount(5000L);

        assertThat(ServerCostSettingMapper.toDto(entity).getPercentage()).isEqualTo(100);
    }
}
