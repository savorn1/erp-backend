package com.example.erp.dto;

import com.example.erp.entity.OpportunityStage;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOpportunityStageRequest {

    @NotNull
    private OpportunityStage stage;
}
