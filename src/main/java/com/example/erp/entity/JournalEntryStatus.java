package com.example.erp.entity;

public enum JournalEntryStatus {
    // Editable — lines can still be changed or the whole entry deleted.
    DRAFT,
    // Approved and immutable — see JournalEntryServiceImpl.postJournalEntry.
    // Corrected via a reversing entry (reversalOfJournalEntryId), never by
    // mutating a posted entry, same "append-only ledger" reasoning as
    // GoodsReceipt/Payment/CreditNote.
    POSTED
}
