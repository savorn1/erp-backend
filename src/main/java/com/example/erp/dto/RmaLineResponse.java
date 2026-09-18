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
public class RmaLineResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Long invoiceLineId;
    private Long serialNumberId;
    private String serialNumberValue;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private String reasonNote;
    // Null when the RMA has no invoice, or the product has no
    // warrantyMonths configured — otherwise invoiceDate + warrantyMonths
    // compared against today.
    private Boolean withinWarranty;
}
