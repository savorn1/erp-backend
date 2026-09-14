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
import java.time.LocalDate;

// An immutable corrective document against an approved PurchaseInvoice —
// issuing one immediately credits the supplier's balance back (see
// PurchaseCreditNoteServiceImpl.createPurchaseCreditNote), mirroring
// CreditNote on the accounts-receivable side.
@Entity
@Table(name = "purchase_credit_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseCreditNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "purchase_invoice_id", nullable = false)
    private Long purchaseInvoiceId;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(unique = true)
    private String creditNoteNumber;

    @Column(nullable = false)
    private LocalDate creditNoteDate;

    @Column(columnDefinition = "text")
    private String reason;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    private String createdBy;
}
