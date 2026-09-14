package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Data
@ParameterObject
public class DashboardSummaryFilterRequest {

    private Long companyId;
    // Scopes revenue/expense/profit/sales/purchase to this period — both
    // optional, omitted means "no lower/upper bound". Cash/receivable/
    // payable/inventoryValue are point-in-time snapshots and ignore this.
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
