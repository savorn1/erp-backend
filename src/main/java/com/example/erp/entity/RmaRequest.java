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

// A customer's return request, tracked REQUESTED -> APPROVED -> RESOLVED
// (or REJECTED/CANCELLED before resolution) — see RmaServiceImpl. Only
// resolution (RmaResolutionType) actually moves money/stock: REFUND calls
// the existing CreditNoteService against invoiceId (required for that
// path), REPLACEMENT records a same-product stock swap, REPAIR is pure
// audit trail. Line items live in RmaLine, looked up by rmaId.
@Entity
@Table(name = "rma_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RmaRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    // Optional — the original sale this return is against. Required for
    // REFUND resolution (CreditNote needs a real invoice), not for
    // REPLACEMENT/REPAIR.
    @Column(name = "invoice_id")
    private Long invoiceId;

    // Where the returned item goes back into stock / the replacement ships from.
    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(unique = true)
    private String rmaNumber;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RmaStatus status = RmaStatus.REQUESTED;

    // Null until resolved.
    @Enumerated(EnumType.STRING)
    @Column(name = "resolution_type")
    private RmaResolutionType resolutionType;

    @Column(columnDefinition = "text")
    private String reason;

    @Column(columnDefinition = "text")
    private String notes;

    private String createdBy;

    // The status this document held when it was cancelled or rejected.
    // Overwriting `status` destroys the only record of how far the workflow
    // actually got, which is what decides whether anything has to be unwound —
    // reserved stock, a posted receipt — so it is captured on the way past.
    //
    // Nullable, and not only for ddl-auto=update: rows cancelled before this
    // existed genuinely have nothing to report, and the UI shows no progress
    // rather than inventing some.
    @Enumerated(EnumType.STRING)
    @Column(name = "cancelled_from_status")
    private RmaStatus cancelledFromStatus;
}
