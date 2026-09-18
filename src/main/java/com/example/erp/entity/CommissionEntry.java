package com.example.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

// One row per PaymentAllocation that has a resolvable sales rep + commission
// rate — see PaymentServiceImpl.recordCommissionEntries, called from both
// recordPayment and refundPayment. basisAmount/commissionAmount mirror
// PaymentAllocation's own signed/additive design: a refund produces a
// negative entry rather than mutating the original, so summed totals stay
// correct without ever needing to look up or adjust a prior row.
@Entity
@Table(name = "commission_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "sales_rep_user_id", nullable = false)
    private Long salesRepUserId;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "payment_allocation_id", nullable = false)
    private Long paymentAllocationId;

    // The PaymentAllocation.amount this entry was computed from — negative
    // for a refund's reversing allocation.
    @Column(name = "basis_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal basisAmount;

    // Snapshot of the CommissionRule rate applied at the time — later rule
    // changes never retroactively alter already-recorded entries.
    @Column(name = "rate_percent", nullable = false, precision = 19, scale = 4)
    private BigDecimal ratePercent;

    @Column(name = "commission_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal commissionAmount;

    @Column(name = "earned_date", nullable = false)
    private LocalDate earnedDate;

    // Set once the business has actually paid this commission out to the
    // rep — see CommissionServiceImpl.markPaid. Purely a payout tracker;
    // no accounting/GL entry is posted for it.
    @Column(name = "paid_out", nullable = false)
    @Builder.Default
    private boolean paidOut = false;

    @Column(name = "paid_out_at")
    private LocalDateTime paidOutAt;
}
