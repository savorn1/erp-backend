package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventorySettingsRequest {

    @NotNull
    private Long companyId;

    private boolean allowOverselling;
    private boolean allowNegativeStock;
    private boolean showAvailableStock;
    private boolean reserveStock;
    private boolean backorderEnabled;
    private boolean oversellingApprovalRequired;
    private boolean stockWarningEnabled;
}
