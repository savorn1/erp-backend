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
public class SalesPendingOrdersResponse {

    // Orders in DRAFT or SUBMITTED — not yet approved/confirmed for
    // fulfillment. Sorted by order date, ascending (oldest waiting first).
    private List<SalesPendingOrderRowResponse> rows;
    private long orderCount;
    private BigDecimal totalAmount;
}
