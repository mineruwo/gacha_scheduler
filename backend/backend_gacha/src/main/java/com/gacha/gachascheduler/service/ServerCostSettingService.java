package com.gacha.gachascheduler.service;

import com.gacha.gachascheduler.entity.ServerCostSettingEntity;
import com.gacha.gachascheduler.repository.ServerCostSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 파트 08 — 서버비 충당 프로그레스 바 설정. 테이블에 항상 id=1 행 하나만 두고 관리자가 수동으로 갱신한다.
 */
@Service
@RequiredArgsConstructor
public class ServerCostSettingService {

    private static final Long SINGLETON_ID = 1L;

    private final ServerCostSettingRepository serverCostSettingRepository;

    @Transactional
    public ServerCostSettingEntity getSettings() {
        if (!serverCostSettingRepository.existsById(SINGLETON_ID)) {
            serverCostSettingRepository.save(new ServerCostSettingEntity());
        }
        return serverCostSettingRepository.findById(SINGLETON_ID).orElseThrow();
    }

    @Transactional
    public ServerCostSettingEntity updateSettings(Long targetAmount, Long currentAmount) {
        ServerCostSettingEntity settings = getSettings();
        settings.setTargetAmount(targetAmount != null ? targetAmount : 0L);
        settings.setCurrentAmount(currentAmount != null ? currentAmount : 0L);
        return serverCostSettingRepository.save(settings);
    }
}
