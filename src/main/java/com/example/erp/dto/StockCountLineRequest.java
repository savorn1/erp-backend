package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockCountLineRequest {

    @NotNull
    private Long productId;

    private Long binId;

    // The unit the counter will enter quantities in. Null means the product's
    // own base unit; anything else must be an inventory-allowed ProductUom.
    private Long unitOfMeasureId;
}
