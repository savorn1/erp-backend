package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchasePendingOrderRowResponse {

    private Long orderId;
    private String poNumber;
    private LocalDate orderDate;
    private String status;
    private Long supplierId;
    private String supplierName;
    private BigDecimal amount;
}
