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
public class CustomerBalanceRowResponse {

    private Long customerId;
    private String customerName;
    private long invoiceCount;
    private BigDecimal outstandingAmount;
    private LocalDate oldestDueDate;
}
