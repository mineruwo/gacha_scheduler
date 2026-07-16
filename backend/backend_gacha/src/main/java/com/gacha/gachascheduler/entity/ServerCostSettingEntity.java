package com.gacha.gachascheduler.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * 파트 08(수익화 전략) — "서버비 충당 프로그레스 바" 설정. docs/plans/08-monetization-strategy.md 참고.
 * 애드센스/후원 등 다채널 수익을 관리자가 수동으로 집계해 입력하는 단일 설정값이라 테이블에 항상 1행만 존재한다
 * (다른 설정이 늘어나면 그때 범용 key-value 테이블로 승격 — 지금은 이 용도 하나뿐이라 단순하게 유지).
 */
@Entity
@Table(name = "server_cost_settings")
@Data
public class ServerCostSettingEntity {

    @Id
    private Long id = 1L;

    @Column(name = "target_amount", nullable = false)
    private Long targetAmount = 0L;

    @Column(name = "current_amount", nullable = false)
    private Long currentAmount = 0L;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
