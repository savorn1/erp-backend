package com.example.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddLeadNoteRequest {

    @NotBlank
    private String description;
}
