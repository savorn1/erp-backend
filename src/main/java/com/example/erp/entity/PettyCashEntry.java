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

// An append-only petty cash ledger entry — TOPUP funds petty cash from a
// cash/bank account, EXPENSE spends it against an expense account. Creating
// one immediately posts a journal entry (see
// AutoPostingServiceImpl.postPettyCashEntry), same "create = immediate
// effect" reasoning as CreditNote — a mistake is corrected with an
// offsetting entry, not an edit, same as a real petty cash book.
@Entity
@Table(name = "petty_cash_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PettyCashEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PettyCashEntryType type;

    @Column(unique = true)
    private String entryNumber;

    @Column(nullable = false)
    private LocalDate entryDate;

    // The OTHER leg of the entry: an ASSET (cash/bank) account for TOPUP, an
    // EXPENSE account for EXPENSE. The petty cash leg itself always comes
    // from PostingRule.pettyCashAccountId, never stored per-entry.
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(columnDefinition = "text")
    private String description;

    private String createdBy;
}
