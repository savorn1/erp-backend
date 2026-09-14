package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private String contactName;
    private String organizationName;
    private String email;
    private String phone;
    private String source;
    private String status;
    private Long assignedToUserId;
    private String assignedToUsername;
    private BigDecimal estimatedValue;
    private String notes;
    private Long convertedOpportunityId;
    private String convertedOpportunityName;
    private LocalDateTime convertedAt;
    private String createdBy;
}
