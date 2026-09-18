package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateDueInvoicesResult {

    private Long templateId;
    private String templateName;
    private boolean success;
    private String invoiceNumber;
    private String errorMessage;
}
