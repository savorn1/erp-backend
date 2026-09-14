package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.ProductPurchasePriceHistoryFilterRequest;
import com.example.erp.dto.ProductPurchasePriceHistoryResponse;
import com.example.erp.dto.PurchaseByBrandResponse;
import com.example.erp.dto.PurchaseByCategoryResponse;
import com.example.erp.dto.PurchaseByDateResponse;
import com.example.erp.dto.PurchaseByPeriodResponse;
import com.example.erp.dto.PurchaseByProductResponse;
import com.example.erp.dto.PurchaseBySupplierResponse;
import com.example.erp.dto.PurchaseBySupplierTypeResponse;
import com.example.erp.dto.PurchaseByUomResponse;
import com.example.erp.dto.PurchaseByWarehouseResponse;
import com.example.erp.dto.PurchaseCancellationResponse;
import com.example.erp.dto.PurchaseDetailResponse;
import com.example.erp.dto.PurchaseDiscountResponse;
import com.example.erp.dto.PurchaseOutstandingInvoicesResponse;
import com.example.erp.dto.PurchasePendingGoodsReceiptsResponse;
import com.example.erp.dto.PurchasePendingOrdersResponse;
import com.example.erp.dto.PurchaseReportFilterRequest;
import com.example.erp.dto.PurchaseSummaryResponse;
import com.example.erp.dto.SupplierPerformanceResponse;
import com.example.erp.dto.SupplierPriceHistoryFilterRequest;
import com.example.erp.dto.SupplierPriceHistoryResponse;
import com.example.erp.service.PurchaseReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/purchase-reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PurchaseReportController {

    private final PurchaseReportService purchaseReportService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<PurchaseSummaryResponse>> summary(@ModelAttribute PurchaseReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(purchaseReportService.summary(filter)));
    }

    @GetMapping("/by-supplier")
    public ResponseEntity<ApiResponse<PurchaseBySupplierResponse>> bySupplier(@ModelAttribute PurchaseReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(purchaseReportService.bySupplier(filter)));
    }

    @GetMapping("/by-product")
    public ResponseEntity<ApiResponse<PurchaseByProductResponse>> byProduct(@ModelAttribute PurchaseReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(purchaseReportService.byProduct(filter)));
    }

    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<PurchaseDetailResponse>> detail(@ModelAttribute PurchaseReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(purchaseReportService.detail(filter)));
    }

    @GetMapping("/by-date")
    public ResponseEntity<ApiResponse<PurchaseByDateResponse>> byDate(@ModelAttribute PurchaseReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(purchaseReportService.byDate(filter)));
    }

    @GetMapping("/by-category")
    public ResponseEntity<ApiResponse<PurchaseByCategoryResponse>> byCategory(@ModelAttribute PurchaseReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(purchaseReportService.byCategory(filter)));
    }

    @GetMapping("/by-brand")
    public ResponseEntity<ApiResponse<PurchaseByBrandResponse>> byBrand(@ModelAttribute PurchaseReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(purchaseReportService.byBrand(filter)));
    }

    @GetMapping("/by-uom")
    public ResponseEntity<ApiResponse<PurchaseByUomResponse>> byUom(@ModelAttribute PurchaseReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(purchaseReportService.byUom(filter)));
    }

    @GetMapping("/by-supplier-type")
    public ResponseEntity<ApiResponse<PurchaseBySupplierTypeResponse>> bySupplierType(@ModelAttribute PurchaseReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(purchaseReportService.bySupplierType(filter)));
    }

    @GetMapping("/by-warehouse")
    public ResponseEntity<ApiResponse<PurchaseByWarehouseResponse>> byWarehouse(@ModelAttribute PurchaseReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(purchaseReportService.byWarehouse(filter)));
    }

    @GetMapping("/pending-orders")
    public ResponseEntity<ApiResponse<PurchasePendingOrdersResponse>> pendingOrders(@ModelAttribute PurchaseReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(purchaseReportService.pendingOrders(filter)));
    }

    @GetMapping("/pending-goods-receipts")
    public ResponseEntity<ApiResponse<PurchasePendingGoodsReceiptsResponse>> pendingGoodsReceipts(@ModelAttribute PurchaseReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(purchaseReportService.pendingGoodsReceipts(filter)));
    }

    @GetMapping("/outstanding-invoices")
    public ResponseEntity<ApiResponse<PurchaseOutstandingInvoicesResponse>> outstandingInvoices(@ModelAttribute PurchaseReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(purchaseReportService.outstandingInvoices(filter)));
    }

    @GetMapping("/cancellations")
    public ResponseEntity<ApiResponse<PurchaseCancellationResponse>> cancellations(@ModelAttribute PurchaseReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(purchaseReportService.cancellations(filter)));
    }

    @GetMapping("/discounts")
    public ResponseEntity<ApiResponse<PurchaseDiscountResponse>> discounts(@ModelAttribute PurchaseReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(purchaseReportService.discounts(filter)));
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<PurchaseByPeriodResponse>> monthly(@ModelAttribute PurchaseReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(purchaseReportService.monthly(filter)));
    }

    @GetMapping("/yearly")
    public ResponseEntity<ApiResponse<PurchaseByPeriodResponse>> yearly(@ModelAttribute PurchaseReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(purchaseReportService.yearly(filter)));
    }

    @GetMapping("/supplier-price-history")
    public ResponseEntity<ApiResponse<SupplierPriceHistoryResponse>> supplierPriceHistory(@ModelAttribute SupplierPriceHistoryFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(purchaseReportService.supplierPriceHistory(filter)));
    }

    @GetMapping("/product-price-history")
    public ResponseEntity<ApiResponse<ProductPurchasePriceHistoryResponse>> productPriceHistory(
            @ModelAttribute ProductPurchasePriceHistoryFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(purchaseReportService.productPriceHistory(filter)));
    }

    @GetMapping("/supplier-performance")
    public ResponseEntity<ApiResponse<SupplierPerformanceResponse>> supplierPerformance(@ModelAttribute PurchaseReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(purchaseReportService.supplierPerformance(filter)));
    }
}
