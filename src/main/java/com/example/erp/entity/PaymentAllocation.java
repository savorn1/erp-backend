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

// How much of a Payment applies to a given Invoice. amount is signed: positive
// when it comes from a PAYMENT (reduces the invoice's outstanding balance),
// negative when it comes from a REFUND (restores it) — see
// PaymentServiceImpl.refundPayment. Summing every allocation for an invoice
// gives its net paidAmount directly, no join back to Payment.type needed
// (see InvoiceServiceImpl.toResponse).
@Entity
@Table(name = "payment_allocations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
}
