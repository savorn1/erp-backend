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

import java.math.BigDecimal;

// A named tax rate a company uses — reference data only for now. Sales/
// purchase order and invoice lines still carry their own free-typed taxRate
// percent (see SalesOrderLine/PurchaseOrderLine/InvoiceLine); this isn't
// wired into them, so pick a rate value here that matches what's typed on
// those lines if you want TaxReportServiceImpl's grouping to line up with it.
@Entity
@Table(name = "tax_rates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    // e.g. "VAT10" — unique within the company.
    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaxType type;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal ratePercent;

    @Builder.Default
    private boolean active = true;
}
