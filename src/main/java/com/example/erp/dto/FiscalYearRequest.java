package com.example.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FiscalYearRequest {

    @NotNull
    private Long companyId;

    @NotBlank
    private String name;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    // When true, the year's periods are also generated — one per calendar
    // month between startDate and endDate. Ignored on update.
    private boolean generateMonthlyPeriods = true;
}
