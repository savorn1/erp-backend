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
public class PaymentByMethodRowResponse {

    private String method;
    private long customerCount;
    private BigDecimal customerNet;
    private long supplierCount;
    private BigDecimal supplierNet;
}
