package com.example.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Used for both create and update.
@Data
public class UomCategoryRequest {

    // Short, stable machine-readable key, e.g. "WEIGHT" — unique across the system.
    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String description;

    private boolean active = true;
}
