package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.ConvertUomRequest;
import com.example.erp.dto.ConvertUomResponse;
import com.example.erp.dto.ProductUomRequest;
import com.example.erp.dto.ProductUomResponse;
import com.example.erp.service.ProductUomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Steps 1-16 (foundation): this is reference data + a conversion calculator.
// Nothing on a PurchaseOrder/SalesOrder/Invoice/GoodsReceipt line reads it
// yet — that integration is a deliberate later phase.
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class ProductUomController {

    private final ProductUomService productUomService;

    @GetMapping("/api/admin/products/{productId}/uoms")
    public ResponseEntity<ApiResponse<List<ProductUomResponse>>> list(@PathVariable Long productId,
                                                                       @RequestParam(required = false) Long priceGroupId) {
        return ResponseEntity.ok(ApiResponse.success(productUomService.list(productId, null, priceGroupId)));
    }

    @PostMapping("/api/admin/products/{productId}/uoms")
    public ResponseEntity<ApiResponse<ProductUomResponse>> create(@PathVariable Long productId, @Valid @RequestBody ProductUomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Product UOM created", productUomService.create(productId, null, request)));
    }

    @PutMapping("/api/admin/products/{productId}/uoms/{id}")
    public ResponseEntity<ApiResponse<ProductUomResponse>> update(@PathVariable Long productId, @PathVariable Long id,
                                                                      @Valid @RequestBody ProductUomRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Product UOM updated", productUomService.update(productId, null, id, request)));
    }

    @DeleteMapping("/api/admin/products/{productId}/uoms/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long productId, @PathVariable Long id) {
        productUomService.delete(productId, null, id);
        return ResponseEntity.ok(ApiResponse.success("Product UOM deleted", null));
    }

    // Variant-scoped: same shape, but each variant carries its own
    // independent set of UOM rows (alternate units/barcodes/pricing) rather
    // than sharing the parent product's.
    @GetMapping("/api/admin/products/{productId}/variants/{variantId}/uoms")
    public ResponseEntity<ApiResponse<List<ProductUomResponse>>> listForVariant(@PathVariable Long productId, @PathVariable Long variantId,
                                                                                 @RequestParam(required = false) Long priceGroupId) {
        return ResponseEntity.ok(ApiResponse.success(productUomService.list(productId, variantId, priceGroupId)));
    }

    @PostMapping("/api/admin/products/{productId}/variants/{variantId}/uoms")
    public ResponseEntity<ApiResponse<ProductUomResponse>> createForVariant(@PathVariable Long productId, @PathVariable Long variantId,
                                                                             @Valid @RequestBody ProductUomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product UOM created", productUomService.create(productId, variantId, request)));
    }

    @PutMapping("/api/admin/products/{productId}/variants/{variantId}/uoms/{id}")
    public ResponseEntity<ApiResponse<ProductUomResponse>> updateForVariant(@PathVariable Long productId, @PathVariable Long variantId,
                                                                             @PathVariable Long id, @Valid @RequestBody ProductUomRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Product UOM updated", productUomService.update(productId, variantId, id, request)));
    }

    @DeleteMapping("/api/admin/products/{productId}/variants/{variantId}/uoms/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteForVariant(@PathVariable Long productId, @PathVariable Long variantId, @PathVariable Long id) {
        productUomService.delete(productId, variantId, id);
        return ResponseEntity.ok(ApiResponse.success("Product UOM deleted", null));
    }

    @GetMapping("/api/admin/product-uoms/convert")
    public ResponseEntity<ApiResponse<ConvertUomResponse>> convert(@Valid @ModelAttribute ConvertUomRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productUomService.convert(request)));
    }
}
