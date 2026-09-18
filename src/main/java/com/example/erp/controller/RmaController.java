package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreateRmaRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ResolveRmaRequest;
import com.example.erp.dto.RmaFilterRequest;
import com.example.erp.dto.RmaResponse;
import com.example.erp.exception.AppException;
import com.example.erp.service.RmaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/rma-requests")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class RmaController {

    private final RmaService rmaService;

    @GetMapping
    public ResponseEntity<PageResponse<RmaResponse>> list(@ModelAttribute RmaFilterRequest filter) {
        return ResponseEntity.ok(rmaService.listRmas(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RmaResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(rmaService.getRma(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RmaResponse>> create(@Valid @RequestBody CreateRmaRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("RMA created", rmaService.createRma(request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<RmaResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("RMA approved", rmaService.approveRma(id)));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<RmaResponse>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("RMA rejected", rmaService.rejectRma(id)));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<RmaResponse>> resolve(@PathVariable Long id, @Valid @RequestBody ResolveRmaRequest request,
                                                             Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("RMA resolved", rmaService.resolveRma(id, request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<RmaResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("RMA cancelled", rmaService.cancelRma(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        rmaService.deleteRma(id);
        return ResponseEntity.ok(ApiResponse.success("RMA deleted", null));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
