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

// A line in a company's chart of accounts. parentAccountId is a plain FK
// (no JPA relationship, matching this codebase's convention — see
// PurchaseOrder's own comment) that builds an arbitrary-depth tree, e.g.
// "1000 Assets" -> "1100 Cash". There's no separate "account group" entity:
// a group is just an account that other accounts point to as their parent
// (see AccountResponse.hasChildren) — same account shape either way.
@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    // e.g. "1100" — unique within the company, not globally.
    @Column(name = "account_code", nullable = false)
    private String accountCode;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    // Null for a top-level account (e.g. "1000 Assets"). Must belong to the
    // same company and carry the same accountType as this account — see
    // AccountServiceImpl.requireParent.
    @Column(name = "parent_account_id")
    private Long parentAccountId;

    @Column(columnDefinition = "text")
    private String description;

    @Builder.Default
    private boolean active = true;
}
