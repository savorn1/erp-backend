package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateWarehouseStatusRequest {

    @NotNull
    private Boolean active;
}
