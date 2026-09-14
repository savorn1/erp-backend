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
public class TaxBySupplierRowResponse {

    private Long supplierId;
    private String supplierName;
    private BigDecimal taxableAmount;
    private BigDecimal taxAmount;
}
