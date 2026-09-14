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
public class SalesDetailRowResponse {

    private Long orderId;
    private String soNumber;
    private LocalDate orderDate;
    private String status;
    private Long customerId;
    private String customerName;
    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal lineTotal;
}
