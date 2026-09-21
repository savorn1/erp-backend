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
import java.time.LocalDateTime;

// Moves through a pick -> pack -> ship -> deliver workflow (DeliveryStatus).
// Stock only actually decreases at ship (see DeliveryServiceImpl.shipDelivery)
// — everything before that is just planning, so a PENDING/PICKED/PACKED
// delivery can still be cancelled with zero stock effect.
@Entity
@Table(name = "deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "sales_order_id", nullable = false)
    private Long salesOrderId;

    // Copied from the SO at posting time — stable historical fact.
    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(unique = true)
    private String deliveryNumber;

    @Column(nullable = false)
    private LocalDate deliveryDate;

    // Deliberately nullable even though new rows always get PENDING via the
    // builder default — this column was added after deliveries already
    // existed, and a NOT NULL column can't be added to a non-empty table via
    // ddl-auto=update. Treat a null read as an already-completed legacy
    // delivery (see DeliveryServiceImpl).
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Column(columnDefinition = "text")
    private String notes;

    private String createdBy;

    private String pickedBy;
    private LocalDateTime pickedAt;

    private String packedBy;
    private LocalDateTime packedAt;

    private String shippedBy;
    private LocalDateTime shippedAt;

    private String deliveredBy;
    private LocalDateTime deliveredAt;

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
    private DeliveryStatus cancelledFromStatus;
}
