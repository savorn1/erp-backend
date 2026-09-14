package com.example.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddSupplierNoteRequest {

    @NotBlank
    private String description;
}
