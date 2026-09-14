package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

// Reused loosely across ArReportController's endpoints — each only reads the
// subset of fields relevant to it (e.g. customerStatement requires
// customerId + dateFrom/dateTo; badDebt reads thresholdDays; most others
// just need companyId + asOfDate), same pattern as SalesReportFilterRequest.
@Data
@ParameterObject
public class ArReportFilterRequest {

    private Long companyId;
    private Long customerId;
    // Point-in-time reports (summary, detail, customer balance, bad debt) —
    // defaults to today when omitted.
    private LocalDate asOfDate;
    // Period reports (customer statement, collections) — both optional,
    // omitted means unbounded.
    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Bad debt only — defaults to 90 when omitted.
    private Integer thresholdDays;
}
