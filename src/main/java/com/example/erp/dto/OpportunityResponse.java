package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long leadId;
    private String leadContactName;
    private Long customerId;
    private String customerName;
    private String name;
    private BigDecimal amount;
    private String stage;
    private Integer probability;
    private LocalDate expectedCloseDate;
    private Long assignedToUserId;
    private String assignedToUsername;
    private String notes;
    private LocalDateTime closedAt;
    private String createdBy;
}
