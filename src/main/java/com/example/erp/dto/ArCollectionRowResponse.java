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
public class ArCollectionRowResponse {

    private Long paymentId;
    private String paymentNumber;
    private LocalDate paymentDate;
    private Long customerId;
    private String customerName;
    private String method;
    private BigDecimal amount;
}
