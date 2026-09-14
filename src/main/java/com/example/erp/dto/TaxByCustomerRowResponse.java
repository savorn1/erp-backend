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
public class TaxByCustomerRowResponse {

    private Long customerId;
    private String customerName;
    private BigDecimal taxableAmount;
    private BigDecimal taxAmount;
}
