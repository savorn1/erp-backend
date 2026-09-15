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

// One open/close cycle (shift) for a Register. At most one OPEN session per
// register at a time — enforced in PosSessionServiceImpl, not a DB
// constraint (same convention as PostingRule's one-per-company rule).
// expectedCash/cashVariance are only meaningful once status is CLOSED.
@Entity
@Table(name = "pos_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "register_id", nullable = false)
    private Long registerId;

    // Denormalized from Register, so filtering by company doesn't need a
    // join — same convention as AccountingPeriod.companyId.
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "opened_by")
    private String openedBy;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "opening_float", nullable = false, precision = 19, scale = 4)
    private BigDecimal openingFloat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PosSessionStatus status = PosSessionStatus.OPEN;

    @Column(name = "closed_by")
    private String closedBy;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "counted_cash", precision = 19, scale = 4)
    private BigDecimal countedCash;

    // openingFloat + cash tendered − cash refunded, snapshotted at close.
    @Column(name = "expected_cash", precision = 19, scale = 4)
    private BigDecimal expectedCash;

    // countedCash - expectedCash. Positive = over, negative = short.
    @Column(name = "cash_variance", precision = 19, scale = 4)
    private BigDecimal cashVariance;
}
