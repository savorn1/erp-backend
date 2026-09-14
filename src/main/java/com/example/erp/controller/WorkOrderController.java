package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CompleteWorkOrderRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.WorkOrderFilterRequest;
import com.example.erp.dto.WorkOrderResponse;
import com.example.erp.service.WorkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/work-orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @GetMapping
    public ResponseEntity<PageResponse<WorkOrderResponse>> list(@ModelAttribute WorkOrderFilterRequest filter) {
        return ResponseEntity.ok(workOrderService.listWorkOrders(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(workOrderService.getWorkOrder(id)));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> start(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Work order started", workOrderService.start(id)));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> complete(@PathVariable Long id, @Valid @RequestBody CompleteWorkOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Work order completed", workOrderService.complete(id, request)));
    }
}
