package com.example.erp.dto;

import com.example.erp.entity.RecurringInvoiceFrequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringInvoiceTemplateResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long customerId;
    private String customerName;
    private Long warehouseId;
    private String warehouseName;
    private String name;
    private RecurringInvoiceFrequency frequency;
    private LocalDate startDate;
    private LocalDate nextRunDate;
    private LocalDate lastGeneratedDate;
    private LocalDate endDate;
    private boolean active;
    private boolean autoApproveInvoice;
    private boolean autoEmailInvoice;
    private String notes;
    private String createdBy;
    private List<RecurringInvoiceTemplateLineResponse> lines;
}
