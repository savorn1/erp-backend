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
import java.time.LocalDate;

// An immutable ledger entry — recording a payment (or issuing a refund)
// immediately adjusts the customer's balance via CustomerService.adjustBalance
// (see PaymentServiceImpl), same "create = immediate effect" reasoning as
// GoodsReceipt/Delivery/CreditNote. amount is always a positive magnitude;
// direction comes from type. How much of it applies to which invoice lives
// in the separate PaymentAllocation table.
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(unique = true)
    private String paymentNumber;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentType type = PaymentType.PAYMENT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    // Positive magnitude — the sum of this payment's allocations.
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    // e.g. a transaction id, check number, or gateway reference.
    private String reference;

    @Column(columnDefinition = "text")
    private String notes;

    // Set on a REFUND row, pointing back to the PAYMENT it reverses.
    @Column(name = "related_payment_id")
    private Long relatedPaymentId;

    private String createdBy;
}
