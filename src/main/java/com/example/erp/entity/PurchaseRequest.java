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

// The internal ask that a department makes before any supplier is involved —
// upstream of Rfq and PurchaseOrder (see those entities). Line items live in
// the separate PurchaseRequestLine entity/table, looked up by
// purchaseRequestId — no JPA relationship mapping, matching this codebase's
// existing convention of plain FK columns everywhere.
@Entity
@Table(name = "purchase_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    // The requesting department — drives who is asking and, informally, who
    // signs off. No separate approver role: any admin can approve/reject.
    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    // Human-readable reference (e.g. "PR-000001") — assigned right after the
    // first save, once the generated id is known (see PurchaseRequestServiceImpl).
    @Column(unique = true)
    private String requestNumber;

    @Column(nullable = false)
    private LocalDate requestDate;

    // When the requested goods/services are needed by.
    private LocalDate requiredDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PurchaseRequestStatus status = PurchaseRequestStatus.DRAFT;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(columnDefinition = "text")
    private String rejectionReason;

    private String requestedBy;
}
