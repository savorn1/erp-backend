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
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Recorded once per failed ManufacturingOrderServiceImpl.recordQualityCheck
// call — distinct from ManufacturingOrder.scrapQuantity/scrapReason (which
// covers routine production-floor scrap/spillage): this is specifically the
// QC-driven rejection of a finished batch, with its own audit trail of who
// rejected it, when, and why.
@Entity
@Table(name = "manufacturing_rejections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManufacturingRejection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "manufacturing_order_id", nullable = false)
    private Long manufacturingOrderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(columnDefinition = "text")
    private String reason;

    @Column(name = "rejected_by")
    private String rejectedBy;

    @CreationTimestamp
    @Column(name = "rejected_at", nullable = false, updatable = false)
    private LocalDateTime rejectedAt;
}
