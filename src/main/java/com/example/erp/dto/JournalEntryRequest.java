package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

// Used for both create and update — a journal entry is only ever editable
// while DRAFT (see JournalEntryServiceImpl), so both shapes are identical.
@Data
public class JournalEntryRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private LocalDate entryDate;

    private String description;

    // Optional — see Journal.
    private Long journalId;

    // Must balance (sum of debits == sum of credits) and have at least two
    // lines — enforced in JournalEntryServiceImpl, not by annotations here.
    @Size(min = 2, message = "A journal entry needs at least two lines")
    @Valid
    private List<JournalEntryLineRequest> lines;
}
