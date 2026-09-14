package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CostCenterFilterRequest;
import com.example.erp.dto.CostCenterRequest;
import com.example.erp.dto.CostCenterResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.service.CostCenterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/cost-centers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CostCenterController {

    private final CostCenterService costCenterService;

    @GetMapping
    public ResponseEntity<PageResponse<CostCenterResponse>> list(@ModelAttribute CostCenterFilterRequest filter) {
        return ResponseEntity.ok(costCenterService.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CostCenterResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(costCenterService.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CostCenterResponse>> create(@Valid @RequestBody CostCenterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Cost center created", costCenterService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CostCenterResponse>> update(@PathVariable Long id, @Valid @RequestBody CostCenterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cost center updated", costCenterService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        costCenterService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Cost center deleted", null));
    }
}
