package com.example.erp.controller;

import com.example.erp.dto.AccountFilterRequest;
import com.example.erp.dto.AccountRequest;
import com.example.erp.dto.AccountResponse;
import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/accounts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<PageResponse<AccountResponse>> list(@ModelAttribute AccountFilterRequest filter) {
        return ResponseEntity.ok(accountService.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> create(@Valid @RequestBody AccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Account created", accountService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> update(@PathVariable Long id, @Valid @RequestBody AccountRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Account updated", accountService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        accountService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Account deleted", null));
    }

    @PostMapping("/seed-sample/{companyId}")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> seedSample(@PathVariable Long companyId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Standard chart of accounts seeded", accountService.seedSampleChartOfAccounts(companyId)));
    }
}
