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
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "supplier_type_id")
    private Long supplierTypeId;

    @Column(nullable = false)
    private String name;

    // Contact information
    private String contactName;
    private String phone;
    private String email;

    // Address
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    // Payment terms — when we must pay this supplier. Shares PaymentTerms
    // with Customer (same set of values, e.g. NET_30), not supplier-specific.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentTerms paymentTerms = PaymentTerms.NET_30;

    // Credit terms — the maximum credit this supplier extends to us.
    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SupplierStatus status = SupplierStatus.ACTIVE;

    // Running accounts-payable balance — positive means we owe this supplier
    // this amount. Only ever changed through a balance adjustment (see
    // SupplierServiceImpl.adjustBalance), same pattern as Customer.currentBalance.
    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;
}
