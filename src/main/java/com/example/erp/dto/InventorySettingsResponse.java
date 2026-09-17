package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventorySettingsResponse {

    private Long companyId;
    private boolean allowOverselling;
    private boolean allowNegativeStock;
    private boolean showAvailableStock;
    private boolean reserveStock;
    private boolean backorderEnabled;
    private boolean oversellingApprovalRequired;
    private boolean stockWarningEnabled;
}
