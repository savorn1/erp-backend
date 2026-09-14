package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreateRoutingRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.RoutingFilterRequest;
import com.example.erp.dto.RoutingResponse;
import com.example.erp.dto.UpdateRoutingRequest;
import com.example.erp.service.RoutingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/routings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoutingController {

    private final RoutingService routingService;

    @GetMapping
    public ResponseEntity<PageResponse<RoutingResponse>> list(@ModelAttribute RoutingFilterRequest filter) {
        return ResponseEntity.ok(routingService.listRoutings(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoutingResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(routingService.getRouting(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoutingResponse>> create(@Valid @RequestBody CreateRoutingRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Routing created", routingService.createRouting(request, authentication.getName())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoutingResponse>> update(@PathVariable Long id, @Valid @RequestBody UpdateRoutingRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Routing updated", routingService.updateRouting(id, request)));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<RoutingResponse>> activate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Routing activated", routingService.activateRouting(id)));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<RoutingResponse>> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Routing deactivated", routingService.deactivateRouting(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        routingService.deleteRouting(id);
        return ResponseEntity.ok(ApiResponse.success("Routing deleted", null));
    }
}
