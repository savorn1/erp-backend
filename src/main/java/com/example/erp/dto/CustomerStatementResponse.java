package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatementResponse {

    private Long customerId;
    private String customerName;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private BigDecimal openingBalance;
    private List<CustomerStatementLineResponse> lines;
    private BigDecimal closingBalance;
}
