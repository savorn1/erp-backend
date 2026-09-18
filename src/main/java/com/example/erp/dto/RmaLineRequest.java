package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RmaLineRequest {

    @NotNull
    private Long productId;

    // Optional — when set, unitPrice defaults from that InvoiceLine if omitted.
    private Long invoiceLineId;

    // Optional — manually picked, since SerialNumber has no link back to a sale.
    private Long serialNumberId;

    @NotNull
    @DecimalMin(value = "0.0001", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    // Required unless invoiceLineId is given (defaults from it then).
    private BigDecimal unitPrice;

    private String reasonNote;
}
