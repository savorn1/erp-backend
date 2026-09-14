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

// How much of a SupplierPayment applies to a given PurchaseInvoice. amount is
// signed: positive when it comes from a PAYMENT (reduces the invoice's
// outstanding balance), negative when it comes from a REFUND (restores it) —
// see SupplierPaymentServiceImpl.refundPayment. Mirrors PaymentAllocation on
// the accounts-receivable side.
@Entity
@Table(name = "supplier_payment_allocations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierPaymentAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_payment_id", nullable = false)
    private Long supplierPaymentId;

    @Column(name = "purchase_invoice_id", nullable = false)
    private Long purchaseInvoiceId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
}
