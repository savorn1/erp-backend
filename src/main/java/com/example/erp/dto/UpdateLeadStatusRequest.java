package com.example.erp.dto;

import com.example.erp.entity.LeadStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateLeadStatusRequest {

    @NotNull
    private LeadStatus status;
}
