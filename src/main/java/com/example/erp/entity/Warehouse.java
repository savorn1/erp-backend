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

@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Plain FK columns (not JPA relations) — matches this codebase's existing
    // convention (see RefreshToken.userId, Branch.companyId).
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private String name;

    // Warehouse location (its own physical address).
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    // The User who manages this warehouse — optional, same pattern as Branch.managerId.
    @Column(name = "manager_id")
    private Long managerId;

    // Settings — same lean set as Branch (phone/email/timezone).
    private String phone;
    private String email;
    private String timezone;

    @Builder.Default
    private boolean active = true;
}
