package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long opportunityId;
    private String opportunityName;
    private Long customerId;
    private String customerName;
    private String quotationNumber;
    private LocalDate quotationDate;
    private LocalDate validUntil;
    private String status;
    private String notes;
    private String createdBy;
    private BigDecimal totalAmount;
    private List<QuotationLineResponse> lines;
}
