package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreateProductionPlanRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ProductionPlanFilterRequest;
import com.example.erp.dto.ProductionPlanResponse;
import com.example.erp.dto.UpdateProductionPlanRequest;
import com.example.erp.service.ProductionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/production-plans")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProductionPlanController {

    private final ProductionPlanService productionPlanService;

    @GetMapping
    public ResponseEntity<PageResponse<ProductionPlanResponse>> list(@ModelAttribute ProductionPlanFilterRequest filter) {
        return ResponseEntity.ok(productionPlanService.listPlans(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductionPlanResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productionPlanService.getPlan(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductionPlanResponse>> create(@Valid @RequestBody CreateProductionPlanRequest request,
                                                                        Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Production plan created", productionPlanService.createPlan(request, authentication.getName())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductionPlanResponse>> update(@PathVariable Long id, @Valid @RequestBody UpdateProductionPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Production plan updated", productionPlanService.updatePlan(id, request)));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<ProductionPlanResponse>> activate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Production plan activated", productionPlanService.activatePlan(id)));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<ProductionPlanResponse>> close(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Production plan closed", productionPlanService.closePlan(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        productionPlanService.deletePlan(id);
        return ResponseEntity.ok(ApiResponse.success("Production plan deleted", null));
    }
}
