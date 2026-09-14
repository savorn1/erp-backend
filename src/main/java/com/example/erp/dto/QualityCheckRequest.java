package com.example.erp.dto;

import com.example.erp.entity.QualityCheckStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QualityCheckRequest {

    // Must be PASSED or FAILED — PENDING is rejected by
    // GoodsReceiptServiceImpl.recordQualityCheck.
    @NotNull
    private QualityCheckStatus status;

    private String notes;
}
