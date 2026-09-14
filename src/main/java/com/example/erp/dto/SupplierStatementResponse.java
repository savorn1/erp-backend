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
public class SupplierStatementResponse {

    private Long supplierId;
    private String supplierName;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private BigDecimal openingBalance;
    private List<SupplierStatementLineResponse> lines;
    private BigDecimal closingBalance;
}
