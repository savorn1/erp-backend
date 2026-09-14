package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionActivityResponse {

    private Long id;
    private Long companyId;
    private Long invoiceId;
    private String invoiceNumber;
    private Long customerId;
    private String customerName;
    private LocalDate activityDate;
    private String method;
    private String notes;
    private LocalDate followUpDate;
    private boolean resolved;
    private String createdBy;
}
