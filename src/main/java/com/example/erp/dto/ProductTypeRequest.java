package com.example.erp.dto;

import com.example.erp.entity.ProductTypeCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductTypeRequest {

    @NotNull
    private ProductTypeCode code;

    @NotBlank
    private String name;

    private boolean active = true;
}
