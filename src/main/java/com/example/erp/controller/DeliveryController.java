package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreateDeliveryRequest;
import com.example.erp.dto.DeliveryFilterRequest;
import com.example.erp.dto.DeliveryResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.exception.AppException;
import com.example.erp.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// A delivery moves pick -> pack -> ship -> deliver (DeliveryStatus). Stock
// only actually decreases at ship — see DeliveryServiceImpl.shipDelivery.
@RestController
@RequestMapping("/api/admin/deliveries")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping
    public ResponseEntity<PageResponse<DeliveryResponse>> list(@ModelAttribute DeliveryFilterRequest filter) {
        return ResponseEntity.ok(deliveryService.listDeliveries(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(deliveryService.getDelivery(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryResponse>> create(@Valid @RequestBody CreateDeliveryRequest request,
                                                                    Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Delivery created", deliveryService.createDelivery(request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/pick")
    public ResponseEntity<ApiResponse<DeliveryResponse>> pick(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Products picked", deliveryService.pickDelivery(id, requireUsername(authentication))));
    }

    @PostMapping("/{id}/pack")
    public ResponseEntity<ApiResponse<DeliveryResponse>> pack(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Products packed", deliveryService.packDelivery(id, requireUsername(authentication))));
    }

    @PostMapping("/{id}/ship")
    public ResponseEntity<ApiResponse<DeliveryResponse>> ship(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Products shipped — stock updated", deliveryService.shipDelivery(id, requireUsername(authentication))));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<DeliveryResponse>> complete(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Delivery confirmed", deliveryService.completeDelivery(id, requireUsername(authentication))));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<DeliveryResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Delivery cancelled", deliveryService.cancelDelivery(id)));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
