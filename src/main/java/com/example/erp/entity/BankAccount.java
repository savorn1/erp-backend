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

// A cash drawer or bank account this company actually holds money in — the
// operational counterpart to a Chart-of-Accounts "1100 Cash"/"1200 Bank" line
// (accountId, optional, points at that GL account; standalone from it
// otherwise — no Journal Entry is posted automatically, see
// BankAccountServiceImpl). currentBalance is a denormalized running total,
// same pattern as Supplier/Customer.currentBalance — only ever changed
// through deposit/withdraw/transfer (see BankAccountServiceImpl).
@Entity
@Table(name = "bank_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    // Optional link to the chart-of-accounts line this account posts against
    // in spirit — not enforced or auto-posted to.
    @Column(name = "account_id")
    private Long accountId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BankAccountType type;

    // Only meaningful for type=BANK.
    private String bankName;
    private String accountNumber;

    @Column(nullable = false)
    @Builder.Default
    private String currency = "USD";

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Builder.Default
    private boolean active = true;
}
