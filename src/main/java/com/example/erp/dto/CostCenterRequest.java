package com.example.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CostCenterRequest {

    @NotNull
    private Long companyId;

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String description;

    private boolean active = true;
}
