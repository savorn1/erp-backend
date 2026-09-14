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

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Plain FK columns (not JPA relations) — matches this codebase's existing
    // convention (see RefreshToken.userId, Branch.companyId).
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "customer_type_id")
    private Long customerTypeId;

    @Column(name = "customer_group_id")
    private Long customerGroupId;

    @Column(nullable = false)
    private String name;

    // Contact information
    private String contactName;
    private String phone;
    private String email;

    // Billing address
    private String billingAddressLine1;
    private String billingAddressLine2;
    private String billingCity;
    private String billingState;
    private String billingPostalCode;
    private String billingCountry;

    // Shipping address
    private String shippingAddressLine1;
    private String shippingAddressLine2;
    private String shippingCity;
    private String shippingState;
    private String shippingPostalCode;
    private String shippingCountry;

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentTerms paymentTerms = PaymentTerms.NET_30;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CustomerStatus status = CustomerStatus.ACTIVE;

    // Running accounts-receivable balance — positive means the customer owes
    // this amount. Only ever changed through a balance adjustment (see
    // CustomerServiceImpl.adjustBalance), never via the general update, so it
    // can't be silently overwritten by an unrelated field edit.
    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;
}
