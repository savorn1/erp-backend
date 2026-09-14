package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOutstandingResponse {

    // Non-cancelled orders with at least one line not yet fully delivered —
    // a live backlog snapshot, not scoped to a date range.
    private List<SalesOutstandingRowResponse> rows;
    private long orderCount;
    private BigDecimal totalOutstandingValue;
}
