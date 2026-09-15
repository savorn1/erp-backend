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
public class PosHeldSaleLineResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private String imageUrl;
    private BigDecimal quantity;
    private BigDecimal discountPercent;
}
