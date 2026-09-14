package com.example.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private String contactName;

    // The lead's own business, if this is a B2B lead — not to be confused
    // with companyId/companyName above, which is the ERP tenant.
    private String organizationName;

    private String email;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LeadStatus status = LeadStatus.NEW;

    // The salesperson (User.id) this lead is assigned to — see
    // LeadServiceImpl.assignSalesperson.
    @Column(name = "assigned_to_user_id")
    private Long assignedToUserId;

    @Column(precision = 19, scale = 4)
    private BigDecimal estimatedValue;

    @Column(columnDefinition = "text")
    private String notes;

    // Legacy — no longer set by convertLead() (it now creates an Opportunity,
    // not a Customer directly; see convertedOpportunityId). Left in place
    // rather than dropped, per the "never destructively alter columns"
    // convention.
    @Column(name = "converted_customer_id")
    private Long convertedCustomerId;

    // Set once convertLead() creates an Opportunity from this lead — the
    // Opportunity itself becomes a Customer only when it's won.
    @Column(name = "converted_opportunity_id")
    private Long convertedOpportunityId;

    private LocalDateTime convertedAt;

    private String createdBy;
}
