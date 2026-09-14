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
public class CustomerBalanceResponse {

    private LocalDate asOfDate;
    // Only customers with a balance greater than zero — sorted by
    // outstanding amount, descending.
    private List<CustomerBalanceRowResponse> rows;
    private BigDecimal totalOutstanding;
}
