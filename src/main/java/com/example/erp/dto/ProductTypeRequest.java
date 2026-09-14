package com.example.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductTypeRequest {

    @NotBlank
    private String name;

    private boolean active = true;
}
