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

// A single supplier's quoted unit price for one product on an Rfq — recorded
// manually by an admin from the supplier's returned quotation (there's no
// supplier-facing portal). One row per (rfqId, supplierId, productId).
@Entity
@Table(name = "rfq_quotation_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfqQuotationLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rfq_id", nullable = false)
    private Long rfqId;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;
}
