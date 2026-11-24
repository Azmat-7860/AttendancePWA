package com.attendance.worker.dto;

import com.attendance.worker.entity.PayType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateWorkerRequest(
        @NotBlank(message = "Worker name is required")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name,

        @Size(max = 32, message = "Phone must not exceed 32 characters")
        String phone,

        @Size(max = 64, message = "Skill must not exceed 64 characters")
        String skill,

        @NotNull(message = "Pay type is required (DAILY, HOURLY, MONTHLY)")
        PayType payType,

        @NotNull(message = "Wage rate is required")
        @PositiveOrZero(message = "Wage rate cannot be negative")
        BigDecimal rate
) {
}
