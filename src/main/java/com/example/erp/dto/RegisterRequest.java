package com.example.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private Long warehouseId;

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private boolean active = true;
}
