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
    private String createdBy;
    private LocalDate nextFollowUpDate;
    private boolean followUpDue;

    // ── Deal-specific fields, absorbed from the old Opportunity entity ──
    private String dealName;
    private BigDecimal amount;
    private Integer probability;
    private LocalDate expectedCloseDate;
    private Long customerId;
    private String customerName;
    private LocalDateTime closedAt;
}
