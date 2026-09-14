package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiscalYearResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private int periodCount;
    private int openPeriodCount;
}
