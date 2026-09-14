package com.example.erp.dto;

import com.example.erp.entity.JournalEntryStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class JournalEntryFilterRequest {

    private String journalNumber;
    private Long companyId;
    private JournalEntryStatus status;
    // Entries with at least one line against this account — a simple form of
    // "journal audit" for a given account.
    private Long accountId;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
