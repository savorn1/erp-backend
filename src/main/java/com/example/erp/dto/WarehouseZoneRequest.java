package com.example.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Used for both create and update — this lookup's shape doesn't diverge
// between the two, matching ProductCategory/ProductBrand's lean pattern.
@Data
public class WarehouseZoneRequest {

    @NotNull
    private Long warehouseId;

    @NotBlank
    private String name;

    private String description;

    private boolean active = true;
}
