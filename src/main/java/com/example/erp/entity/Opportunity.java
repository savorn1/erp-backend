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
import java.time.LocalDate;
import java.time.LocalDateTime;

// Sits between a qualified Lead and a won Customer in the funnel:
// Lead -> (qualified) -> Opportunity -> (won) -> Customer. leadId is set when
// sourced from LeadServiceImpl.convertLead; an opportunity can also be
// created directly (e.g. an upsell against an existing customerId). customerId
// is set at creation for that upsell case, or by winOpportunity() otherwise.
@Entity
@Table(name = "opportunities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "lead_id")
    private Long leadId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(nullable = false)
    private String name;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OpportunityStage stage = OpportunityStage.QUALIFICATION;

    // 0-100.
    private Integer probability;

    private LocalDate expectedCloseDate;

    @Column(name = "assigned_to_user_id")
    private Long assignedToUserId;

    @Column(columnDefinition = "text")
    private String notes;

    private LocalDateTime closedAt;

    private String createdBy;
}
