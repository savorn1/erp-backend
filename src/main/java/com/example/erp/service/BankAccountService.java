package com.example.erp.service;

import com.example.erp.dto.BankAccountFilterRequest;
import com.example.erp.dto.BankAccountRequest;
import com.example.erp.dto.BankAccountResponse;
import com.example.erp.dto.BankReconciliationResponse;
import com.example.erp.dto.BankTransactionFilterRequest;
import com.example.erp.dto.BankTransactionRequest;
import com.example.erp.dto.BankTransactionResponse;
import com.example.erp.dto.BankTransferRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ReconcileTransactionsRequest;

public interface BankAccountService {

    PageResponse<BankAccountResponse> list(BankAccountFilterRequest filter);

    BankAccountResponse get(Long id);

    BankAccountResponse create(BankAccountRequest request);

    BankAccountResponse update(Long id, BankAccountRequest request);

    // Refuses once the account has any transaction — an account that's ever
    // moved money is a ledger, not a lookup row.
    void delete(Long id);

    PageResponse<BankTransactionResponse> listTransactions(BankTransactionFilterRequest filter);

    BankTransactionResponse deposit(Long bankAccountId, BankTransactionRequest request, String actingUsername);

    BankTransactionResponse withdraw(Long bankAccountId, BankTransactionRequest request, String actingUsername);

    // Returns the TRANSFER_OUT leg on the source account; the linked
    // TRANSFER_IN leg on the destination is reachable via
    // relatedTransactionId.
    BankTransactionResponse transfer(Long fromBankAccountId, BankTransferRequest request, String actingUsername);

    BankReconciliationResponse reconcile(Long bankAccountId, ReconcileTransactionsRequest request);
}
