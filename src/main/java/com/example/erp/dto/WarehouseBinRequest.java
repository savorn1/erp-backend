package com.example.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WarehouseBinRequest {

    @NotNull
    private Long zoneId;

    @NotBlank
    private String name;

    private boolean active = true;
}
