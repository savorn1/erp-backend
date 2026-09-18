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

// Copied verbatim into a SalesOrderLineRequest each time the owning
// template's invoice is generated — unitPrice/discountPercent/taxRate are
// optional overrides, same convention as SalesOrderLineRequest itself
// (null means "use the product's own default").
@Entity
@Table(name = "recurring_invoice_template_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringInvoiceTemplateLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal discountPercent;

    private BigDecimal taxRate;
}
