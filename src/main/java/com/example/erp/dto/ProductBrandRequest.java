package com.example.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductBrandRequest {

    @NotBlank
    private String name;

    private boolean active = true;
}
