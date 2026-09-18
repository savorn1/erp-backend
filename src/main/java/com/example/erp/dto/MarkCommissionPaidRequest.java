package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

// Marks every unpaid CommissionEntry for one sales rep, earned on or before
// throughDate, as paid out in one batch — see CommissionServiceImpl.markPaid.
@Data
public class MarkCommissionPaidRequest {

    @NotNull
    private Long salesRepUserId;

    @NotNull
    private LocalDate throughDate;
}
