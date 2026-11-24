package com.attendance.worker.dto;

import com.attendance.worker.entity.PayType;
import com.attendance.worker.entity.Worker;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WorkerResponse(
        UUID id,
        String name,
        String phone,
        String photoKey,
        String skill,
        PayType payType,
        BigDecimal rate,
        boolean active,
        UUID organizationId,
        Instant createdAt,
        Instant updatedAt
) {
    public static WorkerResponse fromEntity(Worker worker) {
        return new WorkerResponse(
                worker.getId(),
                worker.getName(),
                worker.getPhone(),
                worker.getPhotoKey(),
                worker.getSkill(),
                worker.getPayType(),
                worker.getRate(),
                worker.isActive(),
                worker.getOrganizationId(),
                worker.getCreatedAt(),
                worker.getUpdatedAt()
        );
    }
}
