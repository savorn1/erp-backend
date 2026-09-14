package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

// Replaces the header fields and the whole line list wholesale — mirrors
// UpdatePurchaseOrderRequest's convention. companyId/productId are
// deliberately not editable here (changing either would orphan any
// ManufacturingOrder already created against this BOM's identity).
@Data
public class UpdateBillOfMaterialRequest {

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin(value = "0.0001", message = "Output quantity must be greater than zero")
    private BigDecimal outputQuantity;

    private String notes;

    @NotEmpty
    @Valid
    private List<BillOfMaterialLineRequest> lines;
}
