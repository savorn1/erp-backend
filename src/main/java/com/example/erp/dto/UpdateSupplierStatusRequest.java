package com.example.erp.dto;

import com.example.erp.entity.SupplierStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSupplierStatusRequest {

    @NotNull
    private SupplierStatus status;
}
