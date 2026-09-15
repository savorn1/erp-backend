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

// One returned portion of an original PosSaleLine — priced at that line's
// own stored unitPrice/discountPercent/taxRate (not today's product price),
// scaled down to the quantity actually being returned.
@Entity
@Table(name = "pos_exchange_return_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosExchangeReturnLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pos_exchange_id", nullable = false)
    private Long posExchangeId;

    @Column(name = "original_pos_sale_line_id", nullable = false)
    private Long originalPosSaleLineId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal net;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal tax;

    @Column(name = "line_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal lineValue;
}
