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
@Table(name = "sales_order_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sales_order_id", nullable = false)
    private Long salesOrderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityOrdered;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    // Percent, e.g. 10.00 = 10% — defaults to 0 when not supplied.
    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercent = BigDecimal.ZERO;

    // Percent — defaults from Product.taxRate at line creation when not
    // supplied explicitly (see SalesOrderServiceImpl.saveLines).
    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    // Cumulative quantity delivered across all deliveries against this line —
    // updated by DeliveryServiceImpl, never edited directly.
    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal quantityDelivered = BigDecimal.ZERO;
}
