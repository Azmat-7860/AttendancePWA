package com.attendance.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSiteRequest(
        @NotBlank(message = "Site name is required")
        @Size(max = 255, message = "Site name must not exceed 255 characters")
        String name,

        @Size(max = 500, message = "Address must not exceed 500 characters")
        String address
) {
}
