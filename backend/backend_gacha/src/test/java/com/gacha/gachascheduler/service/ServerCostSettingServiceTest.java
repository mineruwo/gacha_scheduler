package com.gacha.gachascheduler.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.gacha.gachascheduler.entity.ServerCostSettingEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(ServerCostSettingService.class)
class ServerCostSettingServiceTest {

    @Autowired
    private ServerCostSettingService serverCostSettingService;

    @Test
    void getSettingsCreatesDefaultZeroRowWhenNoneExists() {
        ServerCostSettingEntity settings = serverCostSettingService.getSettings();

        assertThat(settings.getTargetAmount()).isZero();
        assertThat(settings.getCurrentAmount()).isZero();
    }

    @Test
    void getSettingsIsIdempotentAndReturnsSameSingletonRow() {
        ServerCostSettingEntity first = serverCostSettingService.getSettings();
        ServerCostSettingEntity second = serverCostSettingService.getSettings();

        assertThat(first.getId()).isEqualTo(second.getId());
    }

    @Test
    void updateSettingsPersistsAmounts() {
        serverCostSettingService.updateSettings(1_000_000L, 780_000L);

        ServerCostSettingEntity settings = serverCostSettingService.getSettings();
        assertThat(settings.getTargetAmount()).isEqualTo(1_000_000L);
        assertThat(settings.getCurrentAmount()).isEqualTo(780_000L);
    }

    @Test
    void updateSettingsCanBeCalledRepeatedlyWithoutCreatingDuplicateRows() {
        serverCostSettingService.updateSettings(1000L, 100L);
        serverCostSettingService.updateSettings(1000L, 500L);

        ServerCostSettingEntity settings = serverCostSettingService.getSettings();
        assertThat(settings.getCurrentAmount()).isEqualTo(500L);
    }
}
