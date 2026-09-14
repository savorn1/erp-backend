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

import java.time.LocalDate;

// The formal price quote produced by "Convert to quotation" on an
// Opportunity (see QuotationServiceImpl.createFromOpportunity) — mirrors
// PurchaseOrder/SalesOrder's header/lines split (lines in QuotationLine,
// looked up by quotationId). A quotation can also be created standalone,
// without an opportunity. customerId is nullable — a fresh prospect may not
// be a Customer yet at quoting time.
@Entity
@Table(name = "quotations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "opportunity_id")
    private Long opportunityId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(unique = true)
    private String quotationNumber;

    @Column(nullable = false)
    private LocalDate quotationDate;

    private LocalDate validUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private QuotationStatus status = QuotationStatus.DRAFT;

    @Column(columnDefinition = "text")
    private String notes;

    private String createdBy;
}
