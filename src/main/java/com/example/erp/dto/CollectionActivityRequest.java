package com.example.erp.dto;

import com.example.erp.entity.CollectionContactMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CollectionActivityRequest {

    @NotNull
    private Long invoiceId;

    @NotNull
    private LocalDate activityDate;

    @NotNull
    private CollectionContactMethod method;

    private String notes;

    private LocalDate followUpDate;

    private boolean resolved;
}
