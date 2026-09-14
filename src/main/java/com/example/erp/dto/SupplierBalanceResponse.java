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
public class SupplierBalanceResponse {

    private LocalDate asOfDate;
    // Only suppliers with a balance greater than zero — sorted by
    // outstanding amount, descending.
    private List<SupplierBalanceRowResponse> rows;
    private BigDecimal totalOutstanding;
}
