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

    private Long sourceBinId;

    private Long destinationBinId;

    // Required when the product is BATCH- or SERIAL-tracked — must reference
    // a batch that already exists at the source warehouse.
    private String batchNumber;
}
