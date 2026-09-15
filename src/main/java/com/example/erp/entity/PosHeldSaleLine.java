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

// Deliberately carries no price/tax snapshot, unlike PosSaleLine — a held
// cart isn't a transaction yet, so resuming it re-prices every line against
// the product's *current* price the same way adding it to the cart fresh
// would (see PosSaleServiceImpl/PosPricingService).
@Entity
@Table(name = "pos_held_sale_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosHeldSaleLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pos_held_sale_id", nullable = false)
    private Long posHeldSaleId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercent = BigDecimal.ZERO;
}
