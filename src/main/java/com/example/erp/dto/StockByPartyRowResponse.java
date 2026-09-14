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
public class StockByPartyRowResponse {

    private Long partyId;
    private String partyName;
    private Long productId;
    private String productName;
    private String productSku;
    // Remaining (ordered - fulfilled) quantity on this party's open orders.
    private BigDecimal quantity;
}
