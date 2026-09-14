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

// An immutable ledger entry against a BankAccount — amount is always a
// positive magnitude, direction comes from type, same "amount is a magnitude"
// convention as Payment. A transfer is two rows (TRANSFER_OUT on the source,
// TRANSFER_IN on the destination) linked via relatedTransactionId — see
// BankAccountServiceImpl.transfer.
@Entity
@Table(name = "bank_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "bank_account_id", nullable = false)
    private Long bankAccountId;

    @Column(unique = true)
    private String transactionNumber;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BankTransactionType type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    // e.g. a check number or gateway reference.
    private String reference;

    @Column(columnDefinition = "text")
    private String description;

    // Set on both legs of a transfer, pointing at its counterpart.
    @Column(name = "related_transaction_id")
    private Long relatedTransactionId;

    @Builder.Default
    private boolean reconciled = false;

    private LocalDate reconciledDate;

    private String createdBy;
}
