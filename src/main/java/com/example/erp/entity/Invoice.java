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

// Generated either from a SalesOrder (bills the full ordered quantity — an
// advance/full invoice) or from a Delivery (bills only what was actually
// shipped) — see InvoiceServiceImpl.createFromSalesOrder/createFromDelivery.
// Approving charges the customer's balance via CustomerService.adjustBalance;
// cancelling an approved invoice reverses that charge. A CreditNote against
// an approved invoice applies the opposite (a payment-like credit).
@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "sales_order_id")
    private Long salesOrderId;

    @Column(name = "delivery_id")
    private Long deliveryId;

    @Column(unique = true)
    private String invoiceNumber;

    @Column(nullable = false)
    private LocalDate invoiceDate;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(columnDefinition = "text")
    private String notes;

    private String createdBy;

    // Reference-only foreign currency, for display/printing — see
    // SalesOrder's own comment. Never consulted by AutoPostingService/GL/
    // payments: totalAmount always stays in the company's base currency.
    @Column(name = "foreign_currency", length = 3)
    private String foreignCurrency;

    @Column(name = "exchange_rate", precision = 19, scale = 6)
    private BigDecimal exchangeRate;
}
