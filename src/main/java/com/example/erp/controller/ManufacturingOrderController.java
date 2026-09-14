package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CompleteManufacturingOrderRequest;
import com.example.erp.dto.CreateManufacturingOrderRequest;
import com.example.erp.dto.ManufacturingOrderFilterRequest;
import com.example.erp.dto.ManufacturingOrderResponse;
import com.example.erp.dto.MaterialAvailabilityRowResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.QualityCheckRequest;
import com.example.erp.dto.UpdateManufacturingOrderRequest;
import com.example.erp.service.ManufacturingOrderService;

import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/manufacturing-orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ManufacturingOrderController {

    private final ManufacturingOrderService manufacturingOrderService;

    @GetMapping
    public ResponseEntity<PageResponse<ManufacturingOrderResponse>> list(@ModelAttribute ManufacturingOrderFilterRequest filter) {
        return ResponseEntity.ok(manufacturingOrderService.listOrders(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ManufacturingOrderResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingOrderService.getOrder(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ManufacturingOrderResponse>> create(@Valid @RequestBody CreateManufacturingOrderRequest request,
                                                                            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Manufacturing order created", manufacturingOrderService.createOrder(request, authentication.getName())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ManufacturingOrderResponse>> update(@PathVariable Long id,
                                                                            @Valid @RequestBody UpdateManufacturingOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Manufacturing order updated", manufacturingOrderService.updateOrder(id, request)));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<ApiResponse<ManufacturingOrderResponse>> release(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Manufacturing order released", manufacturingOrderService.releaseOrder(id)));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<ManufacturingOrderResponse>> start(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Production started", manufacturingOrderService.startProduction(id, authentication.getName())));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<ManufacturingOrderResponse>> complete(@PathVariable Long id,
                                                                              @Valid @RequestBody CompleteManufacturingOrderRequest request,
                                                                              Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Production completed",
                manufacturingOrderService.completeProduction(id, request, authentication.getName())));
    }

    @PostMapping("/{id}/quality-check")
    public ResponseEntity<ApiResponse<ManufacturingOrderResponse>> qualityCheck(@PathVariable Long id,
                                                                                  @Valid @RequestBody QualityCheckRequest request,
                                                                                  Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Quality check recorded",
                manufacturingOrderService.recordQualityCheck(id, request, authentication.getName())));
    }

    @GetMapping("/{id}/material-availability")
    public ResponseEntity<ApiResponse<List<MaterialAvailabilityRowResponse>>> materialAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingOrderService.checkMaterialAvailability(id)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ManufacturingOrderResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Manufacturing order cancelled", manufacturingOrderService.cancelOrder(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        manufacturingOrderService.deleteOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Manufacturing order deleted", null));
    }
}
