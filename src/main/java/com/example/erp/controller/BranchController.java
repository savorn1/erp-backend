package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.BranchFilterRequest;
import com.example.erp.dto.BranchResponse;
import com.example.erp.dto.CreateBranchRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateBranchRequest;
import com.example.erp.dto.UpdateBranchStatusRequest;
import com.example.erp.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Admin-only branch management — kept admin-only like CompanyController/UserController.
@RestController
@RequestMapping("/api/admin/branches")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    public ResponseEntity<PageResponse<BranchResponse>> list(@ModelAttribute BranchFilterRequest filter) {
        return ResponseEntity.ok(branchService.listBranches(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(branchService.getBranch(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BranchResponse>> create(@Valid @RequestBody CreateBranchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Branch created", branchService.createBranch(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchResponse>> update(@PathVariable Long id,
                                                                @Valid @RequestBody UpdateBranchRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Branch updated", branchService.updateBranch(id, request)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<BranchResponse>> updateStatus(@PathVariable Long id,
                                                                       @Valid @RequestBody UpdateBranchStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", branchService.updateStatus(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok(ApiResponse.success("Branch deleted", null));
    }
}
