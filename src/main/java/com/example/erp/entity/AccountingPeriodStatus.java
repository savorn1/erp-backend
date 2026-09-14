package com.example.erp.entity;

public enum AccountingPeriodStatus {
    OPEN,
    // JournalEntryServiceImpl refuses to create, edit, or post any entry
    // dated inside a CLOSED period. AutoPostingServiceImpl falls back to
    // leaving an auto-generated entry as DRAFT (never blocking the
    // underlying business action) when its date falls in one.
    CLOSED
}
