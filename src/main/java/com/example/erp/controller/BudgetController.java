package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.BudgetFilterRequest;
import com.example.erp.dto.BudgetResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpsertBudgetRequest;
import com.example.erp.exception.AppException;
import com.example.erp.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/budgets")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<PageResponse<BudgetResponse>> list(@ModelAttribute BudgetFilterRequest filter) {
        return ResponseEntity.ok(budgetService.listBudgets(filter));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> upsert(@Valid @RequestBody UpsertBudgetRequest request,
                                                               Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Budget saved", budgetService.upsertBudget(request, requireUsername(authentication))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.ok(ApiResponse.success("Budget deleted", null));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
