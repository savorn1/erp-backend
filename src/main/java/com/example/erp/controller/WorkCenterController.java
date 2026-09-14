package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreateWorkCenterRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateWorkCenterRequest;
import com.example.erp.dto.UpdateWorkCenterStatusRequest;
import com.example.erp.dto.WorkCenterFilterRequest;
import com.example.erp.dto.WorkCenterResponse;
import com.example.erp.service.WorkCenterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/work-centers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class WorkCenterController {

    private final WorkCenterService workCenterService;

    @GetMapping
    public ResponseEntity<PageResponse<WorkCenterResponse>> list(@ModelAttribute WorkCenterFilterRequest filter) {
        return ResponseEntity.ok(workCenterService.listWorkCenters(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkCenterResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(workCenterService.getWorkCenter(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WorkCenterResponse>> create(@Valid @RequestBody CreateWorkCenterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Work center created", workCenterService.createWorkCenter(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkCenterResponse>> update(@PathVariable Long id, @Valid @RequestBody UpdateWorkCenterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Work center updated", workCenterService.updateWorkCenter(id, request)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<WorkCenterResponse>> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateWorkCenterStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", workCenterService.updateStatus(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        workCenterService.deleteWorkCenter(id);
        return ResponseEntity.ok(ApiResponse.success("Work center deleted", null));
    }
}
