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
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

// A general-ledger posting — manual only for now (no module yet posts these
// automatically on approval of an Invoice/Payment/etc.). Lines live in the
// separate JournalEntryLine table, looked up by journalEntryId — no JPA
// relationship mapping, matching this codebase's plain-FK convention (see
// PurchaseOrder's own comment). Reversing a POSTED entry creates a second,
// already-POSTED entry with debits/credits swapped rather than mutating this
// one — see JournalEntryServiceImpl.reverseJournalEntry.
@Entity
@Table(name = "journal_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(unique = true)
    private String journalNumber;

    @Column(nullable = false)
    private LocalDate entryDate;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private JournalEntryStatus status = JournalEntryStatus.DRAFT;

    // Set on a reversing entry, pointing back to the POSTED entry it reverses.
    @Column(name = "reversal_of_journal_entry_id")
    private Long reversalOfJournalEntryId;

    // Set on the original once reversed, pointing forward to the reversing
    // entry — a POSTED entry can only be reversed once (see
    // JournalEntryServiceImpl.reverseJournalEntry).
    @Column(name = "reversed_by_journal_entry_id")
    private Long reversedByJournalEntryId;

    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private String postedBy;

    private LocalDateTime postedAt;
}
