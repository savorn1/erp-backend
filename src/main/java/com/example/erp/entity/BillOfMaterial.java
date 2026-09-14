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

// Component lines live in the separate BillOfMaterialLine entity/table,
// looked up by bomId — no JPA relationship mapping, matching this codebase's
// existing convention of plain FK columns everywhere.
@Entity
@Table(name = "bill_of_materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillOfMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    // The finished good this recipe produces.
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "bom_number", unique = true)
    private String bomNumber;

    @Column(nullable = false)
    private String name;

    // Quantity of the finished good yielded by one run of this recipe —
    // component line quantities are expressed per this batch size, then
    // scaled by ManufacturingOrderServiceImpl to a specific order's
    // plannedQuantity.
    @Column(name = "output_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal outputQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BillOfMaterialStatus status = BillOfMaterialStatus.ACTIVE;

    // Editing a BOM in place would silently change every past manufacturing
    // order's paper trail, so a change instead creates a new row via
    // BillOfMaterialServiceImpl.createNewVersion: version increments,
    // previousVersionId points back at what it was cloned from, and the old
    // row gets supersededByBomId set and flips to INACTIVE. Nullable (not
    // nullable=false) since this column was added after the table already
    // existed — see ddl-auto=update note on Product.trackingType.
    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;

    @Column(name = "previous_version_id")
    private Long previousVersionId;

    @Column(name = "superseded_by_bom_id")
    private Long supersededByBomId;

    @Column(columnDefinition = "text")
    private String notes;

    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
