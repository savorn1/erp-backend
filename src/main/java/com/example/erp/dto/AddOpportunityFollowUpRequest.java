package com.example.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddOpportunityFollowUpRequest {

    @NotBlank
    private String description;
}
