package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockCountLineRequest {

    @NotNull
    private Long productId;

    private Long binId;
}
