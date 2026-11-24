package com.attendance.worker.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record AssignSiteRequest(
        @NotNull(message = "Site ID is required")
        UUID siteId,

        @NotNull(message = "From date is required")
        LocalDate fromDate,

        LocalDate toDate
) {
}
