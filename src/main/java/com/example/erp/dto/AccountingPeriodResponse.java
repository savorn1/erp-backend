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
public class AccountingPeriodResponse {

    private Long id;
    private Long fiscalYearId;
    private String fiscalYearName;
    private Long companyId;
    private Integer periodNumber;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
