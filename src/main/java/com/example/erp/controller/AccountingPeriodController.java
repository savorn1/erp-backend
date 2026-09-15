package com.example.erp.controller;

import com.example.erp.dto.AccountingPeriodFilterRequest;
import com.example.erp.dto.AccountingPeriodResponse;
import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.service.AccountingPeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/accounting-periods")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class AccountingPeriodController {

    private final AccountingPeriodService accountingPeriodService;

    @GetMapping
    public ResponseEntity<PageResponse<AccountingPeriodResponse>> list(@ModelAttribute AccountingPeriodFilterRequest filter) {
        return ResponseEntity.ok(accountingPeriodService.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountingPeriodResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(accountingPeriodService.get(id)));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<AccountingPeriodResponse>> close(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Period closed", accountingPeriodService.close(id)));
    }

    @PostMapping("/{id}/reopen")
    public ResponseEntity<ApiResponse<AccountingPeriodResponse>> reopen(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Period reopened", accountingPeriodService.reopen(id)));
    }
}
