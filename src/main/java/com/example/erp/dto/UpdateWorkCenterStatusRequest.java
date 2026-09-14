package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateWorkCenterStatusRequest {

    @NotNull
    private Boolean active;
}
