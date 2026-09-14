package com.example.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Used for both create and update — this lookup's shape doesn't diverge
// between the two, unlike Company/Branch/Department.
@Data
public class ProductCategoryRequest {

    @NotBlank
    private String name;

    private boolean active = true;
}
