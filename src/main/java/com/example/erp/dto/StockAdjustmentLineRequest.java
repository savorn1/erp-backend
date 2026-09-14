package com.example.erp.dto;

import com.example.erp.entity.StockAdjustmentReason;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class StockAdjustmentLineRequest {

    @NotNull
    private Long productId;

    private Long binId;

    @NotNull
    private StockAdjustmentReason reason;

    @NotNull
    @DecimalMin(value = "0.0001", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    // Required when the product is BATCH-tracked.
    private String batchNumber;

    // Only used for reason=STOCK_INCREASE when batchNumber doesn't already exist.
    private LocalDate expirationDate;

    // Required when the product is SERIAL-tracked — must have exactly
    // `quantity` entries, validated at approval time.
    private List<String> serialNumbers;
}
