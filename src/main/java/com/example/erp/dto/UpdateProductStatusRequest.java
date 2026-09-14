package com.example.erp.dto;

import com.example.erp.entity.ProductStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateProductStatusRequest {

    @NotNull
    private ProductStatus status;
}
