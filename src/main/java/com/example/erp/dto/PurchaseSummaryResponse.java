package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseSummaryResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    private long orderCount;
    private BigDecimal totalQuantity;
    private BigDecimal subtotal;
    private BigDecimal discountTotal;
    private BigDecimal taxTotal;
    private BigDecimal totalAmount;
    private BigDecimal averageOrderValue;
    private List<PurchaseStatusBreakdownResponse> byStatus;
}
