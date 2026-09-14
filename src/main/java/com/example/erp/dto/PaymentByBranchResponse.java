package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentByBranchResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Attribution is the branch of the user who recorded the payment
    // (Payment.createdBy -> User.branchId) — payments themselves carry no branch.
    private List<PaymentByBranchRowResponse> rows;
}
