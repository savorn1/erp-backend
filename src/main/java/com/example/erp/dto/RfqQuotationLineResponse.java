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
public class RfqQuotationLineResponse {

    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal quantity;
    // Null until this supplier has quoted this product.
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
