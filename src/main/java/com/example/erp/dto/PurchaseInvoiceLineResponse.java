package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseInvoiceLineResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal lineTotal;
}
