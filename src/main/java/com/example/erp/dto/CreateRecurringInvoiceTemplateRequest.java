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
public class CreateRecurringInvoiceTemplateRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private Long customerId;

    @NotNull
    private Long warehouseId;

    @NotBlank
    private String name;

    @NotNull
    private RecurringInvoiceFrequency frequency;

    @NotNull
    private LocalDate startDate;

    // Null means indefinite.
    private LocalDate endDate;

    // Both default true/false when omitted — see the entity's own comment.
    private Boolean autoApproveInvoice;
    private Boolean autoEmailInvoice;

    private String notes;

    @NotEmpty
    @Valid
    private List<RecurringInvoiceTemplateLineRequest> lines;
}
