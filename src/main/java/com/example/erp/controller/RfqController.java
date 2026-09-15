package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreateRfqRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.RecordRfqQuotationRequest;
import com.example.erp.dto.RfqFilterRequest;
import com.example.erp.dto.RfqResponse;
import com.example.erp.dto.UpdateRfqRequest;
import com.example.erp.exception.AppException;
import com.example.erp.service.RfqService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/rfqs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class RfqController {

    private final RfqService rfqService;

    @GetMapping
    public ResponseEntity<PageResponse<RfqResponse>> list(@ModelAttribute RfqFilterRequest filter) {
        return ResponseEntity.ok(rfqService.listRfqs(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RfqResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(rfqService.getRfq(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RfqResponse>> create(@Valid @RequestBody CreateRfqRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("RFQ created", rfqService.createRfq(request, requireUsername(authentication))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RfqResponse>> update(@PathVariable Long id, @Valid @RequestBody UpdateRfqRequest request) {
        return ResponseEntity.ok(ApiResponse.success("RFQ updated", rfqService.updateRfq(id, request)));
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<ApiResponse<RfqResponse>> send(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("RFQ sent", rfqService.sendRfq(id)));
    }

    @PostMapping("/{id}/suppliers/{supplierId}/quotation")
    public ResponseEntity<ApiResponse<RfqResponse>> recordQuotation(@PathVariable Long id, @PathVariable Long supplierId,
                                                                       @Valid @RequestBody RecordRfqQuotationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Supplier quotation recorded", rfqService.recordQuotation(id, supplierId, request)));
    }

    @PostMapping("/{id}/suppliers/{supplierId}/select")
    public ResponseEntity<ApiResponse<RfqResponse>> selectSupplier(@PathVariable Long id, @PathVariable Long supplierId,
                                                                      Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Supplier selected, purchase order created",
                rfqService.selectSupplier(id, supplierId, requireUsername(authentication))));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<RfqResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("RFQ cancelled", rfqService.cancelRfq(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        rfqService.deleteRfq(id);
        return ResponseEntity.ok(ApiResponse.success("RFQ deleted", null));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
