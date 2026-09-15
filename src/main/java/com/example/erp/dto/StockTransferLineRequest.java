package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockTransferLineRequest {

    @NotNull
    private Long productId;

    @NotNull
    @DecimalMin(value = "0.0001", message = "Quantity requested must be greater than zero")
    private BigDecimal quantityRequested;

    // The unit quantityRequested is expressed in. Null means the product's
    // own base unit; any other value must already be registered as an
    // inventory-allowed ProductUom for this product.
    private Long unitOfMeasureId;

    private Long sourceBinId;

    private Long destinationBinId;

    // Required when the product is BATCH- or SERIAL-tracked — must reference
    // a batch that already exists at the source warehouse.
    private String batchNumber;
}
