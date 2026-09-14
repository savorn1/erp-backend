package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SubmitStockCountRequest {

    @NotEmpty
    @Valid
    private List<StockCountLineCountRequest> lines;
}
