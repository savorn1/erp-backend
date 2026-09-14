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
public class PurchaseDiscountRowResponse {

    private Long orderId;
    private String poNumber;
    private LocalDate orderDate;
    private Long supplierId;
    private String supplierName;
    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
}
