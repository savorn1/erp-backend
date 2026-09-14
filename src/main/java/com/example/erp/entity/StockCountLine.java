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

@Entity
@Table(name = "stock_count_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockCountLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_count_id", nullable = false)
    private Long stockCountId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "bin_id")
    private Long binId;

    // Snapshotted from StockLevel when the count is created — the baseline
    // the physical count is compared against.
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal systemQuantity;

    // Filled in by submitCounts(); null until then.
    @Column(precision = 19, scale = 4)
    private BigDecimal countedQuantity;

    // countedQuantity - systemQuantity, set alongside it.
    @Column(precision = 19, scale = 4)
    private BigDecimal varianceQuantity;
}
