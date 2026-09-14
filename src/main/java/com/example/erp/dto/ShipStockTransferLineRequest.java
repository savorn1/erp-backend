package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ShipStockTransferLineRequest {

    @NotNull
    private Long stockTransferLineId;

    // Required when the line's product is SERIAL-tracked — must contain
    // exactly quantityRequested serial numbers, each currently IN_STOCK at
    // the source warehouse.
    private List<String> serialNumbers;
}
