package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.DashboardSummaryFilterRequest;
import com.example.erp.dto.DashboardSummaryResponse;
import com.example.erp.dto.DashboardTrendFilterRequest;
import com.example.erp.dto.DashboardTrendResponse;
import com.example.erp.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> summary(@ModelAttribute DashboardSummaryFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.summary(filter)));
    }

    @GetMapping("/trend")
    public ResponseEntity<ApiResponse<DashboardTrendResponse>> trend(@ModelAttribute DashboardTrendFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.trend(filter)));
    }
}
