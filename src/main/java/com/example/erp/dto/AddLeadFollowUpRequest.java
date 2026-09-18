package com.example.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AddLeadFollowUpRequest {

    @NotBlank
    private String description;

    // Optional — sets Lead.nextFollowUpDate (null/omitted clears it).
    private LocalDate nextFollowUpDate;
}
