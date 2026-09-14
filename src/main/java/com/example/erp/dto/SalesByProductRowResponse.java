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
public class SalesByProductRowResponse {

    private Long productId;
    private String productSku;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal revenue;
}
