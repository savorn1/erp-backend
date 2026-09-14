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
public class ApPaymentRowResponse {

    private Long paymentId;
    private String paymentNumber;
    private LocalDate paymentDate;
    private Long supplierId;
    private String supplierName;
    private String method;
    private BigDecimal amount;
}
