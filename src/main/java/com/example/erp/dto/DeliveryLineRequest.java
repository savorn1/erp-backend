package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DeliveryLineRequest {

    @NotNull
    private Long salesOrderLineId;

    @NotNull
    @DecimalMin(value = "0.0001", message = "Quantity delivered must be greater than zero")
    private BigDecimal quantityDelivered;

    private Long binId;

    // Required when the product is BATCH-tracked — must reference a batch
    // that already exists (you can only ship from stock that was received).
    private String batchNumber;

    // Required when the product is SERIAL-tracked — must contain exactly
    // quantityDelivered serial numbers, each currently IN_STOCK at this warehouse.
    private List<String> serialNumbers;
}
