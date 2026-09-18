package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CommissionEntryFilterRequest;
import com.example.erp.dto.CommissionEntryResponse;
import com.example.erp.dto.CommissionReportFilterRequest;
import com.example.erp.dto.CommissionReportResponse;
import com.example.erp.dto.MarkCommissionPaidRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.service.CommissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/commissions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class CommissionController {

    private final CommissionService service;

    @GetMapping("/report")
    public ResponseEntity<ApiResponse<CommissionReportResponse>> report(@ModelAttribute CommissionReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(service.report(filter)));
    }

    @GetMapping("/entries")
    public ResponseEntity<PageResponse<CommissionEntryResponse>> entries(@ModelAttribute CommissionEntryFilterRequest filter) {
        return ResponseEntity.ok(service.listEntries(filter));
    }

    @PostMapping("/mark-paid")
    public ResponseEntity<ApiResponse<Void>> markPaid(@Valid @RequestBody MarkCommissionPaidRequest request) {
        service.markPaid(request);
        return ResponseEntity.ok(ApiResponse.success("Commission marked as paid", null));
    }
}
