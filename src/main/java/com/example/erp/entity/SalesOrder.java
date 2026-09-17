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

// Line items live in the separate SalesOrderLine entity/table, looked up by
// salesOrderId — mirrors PurchaseOrder's own comment/reasoning exactly.
@Entity
@Table(name = "sales_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    // Source warehouse — where deliveries against this SO decrease stock from.
    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(unique = true)
    private String soNumber;

    @Column(nullable = false)
    private LocalDate orderDate;

    private LocalDate expectedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SalesOrderStatus status = SalesOrderStatus.DRAFT;

    @Column(columnDefinition = "text")
    private String notes;

    private String createdBy;

    // Reference-only foreign currency, for display/printing — null means
    // "base currency only," the default and only option before this field
    // existed. Never consulted by AutoPostingService/GL/payments: the amounts
    // above (subtotal/discountAmount/taxAmount/totalAmount, computed by
    // SalesOrderServiceImpl) always stay in the company's base currency.
    @Column(name = "foreign_currency", length = 3)
    private String foreignCurrency;

    // 1 foreignCurrency unit = exchangeRate base-currency units. Required
    // together with foreignCurrency — both null or both set, enforced in
    // SalesOrderServiceImpl.
    @Column(name = "exchange_rate", precision = 19, scale = 6)
    private BigDecimal exchangeRate;
}
