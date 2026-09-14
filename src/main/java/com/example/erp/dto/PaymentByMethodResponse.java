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
public class PaymentByMethodResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // One row per PaymentMethod that saw activity.
    private List<PaymentByMethodRowResponse> rows;
    private BigDecimal totalCustomerNet;
    private BigDecimal totalSupplierNet;
}
