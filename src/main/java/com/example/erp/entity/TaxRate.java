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

// A named tax rate a company uses. Sales/purchase order and invoice lines
// still carry their own free-typed taxRate percent (see
// SalesOrderLine/PurchaseOrderLine/InvoiceLine) rather than a link to a row
// here, so pick a rate value here that matches what's typed on those lines
// if you want TaxReportServiceImpl's grouping to line up with it. accountId
// (the "Tax Account" for this rate) is likewise unreachable from an
// invoice/order line for the same reason — AutoPostingServiceImpl can only
// use it when it's asked to post a specific TaxRate directly (there is no
// such call site yet); everyday Invoice/PurchaseInvoice auto-posting falls
// back to PostingRule's own taxPayableAccountId/taxReceivableAccountId.
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

    // The GL account this tax posts to when it can be posted directly —
    // see the class comment above for why that's rare today.
    @Column(name = "account_id")
    private Long accountId;
}
