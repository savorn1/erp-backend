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
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// An append-only activity/audit entry for a Supplier ("Supplier history") —
// written automatically on creation, status changes, and balance adjustments,
// or manually as a free-form note. Mirrors CustomerActivity.
@Entity
@Table(name = "supplier_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupplierActivityType type;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    // Signed delta for BALANCE_ADJUSTMENT entries (+charge / -payment); null otherwise.
    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
