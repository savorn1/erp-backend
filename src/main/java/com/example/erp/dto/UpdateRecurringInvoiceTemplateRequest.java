package com.example.erp.dto;

import com.example.erp.entity.RecurringInvoiceFrequency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateRecurringInvoiceTemplateRequest {

    @NotNull
    private Long customerId;

    @NotNull
    private Long warehouseId;

    @NotBlank
    private String name;

    @NotNull
    private RecurringInvoiceFrequency frequency;

    @NotNull
    private LocalDate nextRunDate;

    private LocalDate endDate;

    private boolean active;
    private boolean autoApproveInvoice;
    private boolean autoEmailInvoice;

    private String notes;

    @NotEmpty
    @Valid
    private List<RecurringInvoiceTemplateLineRequest> lines;
}
