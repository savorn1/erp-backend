package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// Explains the resolved unit price for a (customer, product) pair — the same
// cascade SalesOrderServiceImpl.resolveUnitPrice applies, surfaced for
// support/admin verification without creating a real sales order.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceLookupResponse {

    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal sellingPrice;

    private Long customerId;
    private String customerName;
    private Long customerGroupId;
    private String customerGroupName;
    private Long priceGroupId;
    private String priceGroupName;
    private BigDecimal discountPercent;

    private Long productPriceId;
    private BigDecimal resolvedUnitPrice;
    private String source;
}
