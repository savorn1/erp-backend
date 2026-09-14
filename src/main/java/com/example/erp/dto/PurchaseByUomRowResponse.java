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
public class PurchaseByUomRowResponse {

    private Long uomId;
    private String uomName;
    // Sum of quantityOrdered as typed on each line — mixed units aren't
    // converted to a common base, so this is only meaningful when a single
    // UOM dominates a line's purchases.
    private BigDecimal quantity;
    private BigDecimal amount;
}
