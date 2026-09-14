package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RfqSupplierResponse {

    private Long supplierId;
    private String supplierName;
    private String status;
    // Sum of quoted line totals — null until every line has been quoted, so
    // partial quotes don't look comparable to complete ones.
    private BigDecimal quotedTotal;
    private List<RfqQuotationLineResponse> lines;
}
