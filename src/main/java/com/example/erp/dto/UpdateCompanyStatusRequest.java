package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCompanyStatusRequest {

    @NotNull
    private Boolean active;
}
