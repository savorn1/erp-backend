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
public class SalesPendingOrderRowResponse {

    private Long orderId;
    private String soNumber;
    private LocalDate orderDate;
    private String status;
    private Long customerId;
    private String customerName;
    private BigDecimal amount;
}
