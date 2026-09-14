package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class GoodsReceiptLineRequest {

    @NotNull
    private Long purchaseOrderLineId;

    @NotNull
    @DecimalMin(value = "0.0001", message = "Quantity received must be greater than zero")
    private BigDecimal quantityReceived;

    private Long binId;

    // Required when the product is BATCH-tracked; ignored otherwise. If the
    // batch already exists (same product + number) its existing expiration
    // date wins over whatever is supplied here — see GoodsReceiptServiceImpl.
    private String batchNumber;
    private LocalDate expirationDate;

    // Required when the product is SERIAL-tracked — must contain exactly
    // quantityReceived distinct, previously-unused serial numbers.
    private List<String> serialNumbers;
}
