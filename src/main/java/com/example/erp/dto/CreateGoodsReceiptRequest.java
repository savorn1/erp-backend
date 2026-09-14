package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateGoodsReceiptRequest {

    @NotNull
    private Long purchaseOrderId;

    @NotNull
    private LocalDate receiptDate;

    private String notes;

    @NotEmpty
    @Valid
    private List<GoodsReceiptLineRequest> lines;
}
