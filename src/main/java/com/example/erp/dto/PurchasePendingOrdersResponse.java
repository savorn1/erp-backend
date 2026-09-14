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
public class PurchasePendingOrdersResponse {

    // Orders in DRAFT or SUBMITTED — not yet approved. Sorted by order date, ascending.
    private List<PurchasePendingOrderRowResponse> rows;
    private long orderCount;
    private BigDecimal totalAmount;
}
