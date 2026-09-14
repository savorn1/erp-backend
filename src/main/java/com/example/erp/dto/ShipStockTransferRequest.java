package com.example.erp.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class ShipStockTransferRequest {

    // Optional — only lines whose product is SERIAL-tracked need an entry.
    @Valid
    private List<ShipStockTransferLineRequest> lines;
}
