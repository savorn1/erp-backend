package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
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
import com.example.erp.exception.AppException;
import com.example.erp.service.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/bank-accounts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @GetMapping
    public ResponseEntity<PageResponse<BankAccountResponse>> list(@ModelAttribute BankAccountFilterRequest filter) {
        return ResponseEntity.ok(bankAccountService.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BankAccountResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(bankAccountService.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BankAccountResponse>> create(@Valid @RequestBody BankAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Bank account created", bankAccountService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BankAccountResponse>> update(@PathVariable Long id, @Valid @RequestBody BankAccountRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Bank account updated", bankAccountService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        bankAccountService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Bank account deleted", null));
    }

    @GetMapping("/transactions")
    public ResponseEntity<PageResponse<BankTransactionResponse>> listTransactions(@ModelAttribute BankTransactionFilterRequest filter) {
        return ResponseEntity.ok(bankAccountService.listTransactions(filter));
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<ApiResponse<BankTransactionResponse>> deposit(@PathVariable Long id, @Valid @RequestBody BankTransactionRequest request,
                                                                            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Deposit recorded", bankAccountService.deposit(id, request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<ApiResponse<BankTransactionResponse>> withdraw(@PathVariable Long id, @Valid @RequestBody BankTransactionRequest request,
                                                                             Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Withdrawal recorded", bankAccountService.withdraw(id, request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<ApiResponse<BankTransactionResponse>> transfer(@PathVariable Long id, @Valid @RequestBody BankTransferRequest request,
                                                                             Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfer recorded", bankAccountService.transfer(id, request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/reconcile")
    public ResponseEntity<ApiResponse<BankReconciliationResponse>> reconcile(@PathVariable Long id, @Valid @RequestBody ReconcileTransactionsRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Transactions reconciled", bankAccountService.reconcile(id, request)));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
