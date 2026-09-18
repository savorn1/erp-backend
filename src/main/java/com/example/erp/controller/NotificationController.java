package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.NotificationSummaryResponse;
import com.example.erp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Compute-on-demand alert feed — re-runs a handful of cheap existing
// queries every call rather than reading from a persisted table, since
// nothing in this codebase populates one in the background (no scheduling
// infrastructure exists anywhere). Aggregates across every company; this
// app has no global "current company" header context to scope it to one.
@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<NotificationSummaryResponse>> summary() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getSummary()));
    }
}
