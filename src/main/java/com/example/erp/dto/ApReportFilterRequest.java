package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

// Reused loosely across ApReportController's endpoints — mirrors
// ArReportFilterRequest exactly, for the payables side.
@Data
@ParameterObject
public class ApReportFilterRequest {

    private Long companyId;
    private Long supplierId;
    // Point-in-time reports (summary, detail, supplier balance) — defaults
    // to today when omitted.
    private LocalDate asOfDate;
    // Period reports (supplier statement, payments) — both optional,
    // omitted means unbounded.
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
