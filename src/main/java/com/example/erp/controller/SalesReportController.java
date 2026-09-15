package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.SalesByBrandResponse;
import com.example.erp.dto.SalesByCategoryResponse;
import com.example.erp.dto.SalesByCustomerGroupResponse;
import com.example.erp.dto.SalesByCustomerResponse;
import com.example.erp.dto.SalesByCustomerTypeResponse;
import com.example.erp.dto.SalesByDateResponse;
import com.example.erp.dto.SalesByPeriodResponse;
import com.example.erp.dto.SalesByProductResponse;
import com.example.erp.dto.SalesBySalespersonResponse;
import com.example.erp.dto.SalesByWarehouseResponse;
import com.example.erp.dto.SalesCancellationResponse;
import com.example.erp.dto.SalesDetailResponse;
import com.example.erp.dto.SalesDiscountResponse;
import com.example.erp.dto.SalesGrowthResponse;
import com.example.erp.dto.SalesOutstandingInvoicesResponse;
import com.example.erp.dto.SalesOutstandingResponse;
import com.example.erp.dto.SalesPendingDeliveriesResponse;
import com.example.erp.dto.SalesPendingOrdersResponse;
import com.example.erp.dto.SalesPeriodReportRequest;
import com.example.erp.dto.SalesReportFilterRequest;
import com.example.erp.dto.SalesSummaryResponse;
import com.example.erp.service.SalesReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/sales-reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class SalesReportController {

    private final SalesReportService salesReportService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<SalesSummaryResponse>> summary(@ModelAttribute SalesReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(salesReportService.summary(filter)));
    }

    @GetMapping("/by-product")
    public ResponseEntity<ApiResponse<SalesByProductResponse>> byProduct(@ModelAttribute SalesReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(salesReportService.byProduct(filter)));
    }

    @GetMapping("/by-customer")
    public ResponseEntity<ApiResponse<SalesByCustomerResponse>> byCustomer(@ModelAttribute SalesReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(salesReportService.byCustomer(filter)));
    }

    @GetMapping("/by-salesperson")
    public ResponseEntity<ApiResponse<SalesBySalespersonResponse>> bySalesperson(@ModelAttribute SalesReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(salesReportService.bySalesperson(filter)));
    }

    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<SalesDetailResponse>> detail(@ModelAttribute SalesReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(salesReportService.detail(filter)));
    }

    @GetMapping("/by-date")
    public ResponseEntity<ApiResponse<SalesByDateResponse>> byDate(@ModelAttribute SalesReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(salesReportService.byDate(filter)));
    }

    @GetMapping("/by-category")
    public ResponseEntity<ApiResponse<SalesByCategoryResponse>> byCategory(@ModelAttribute SalesReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(salesReportService.byCategory(filter)));
    }

    @GetMapping("/by-customer-group")
    public ResponseEntity<ApiResponse<SalesByCustomerGroupResponse>> byCustomerGroup(@ModelAttribute SalesReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(salesReportService.byCustomerGroup(filter)));
    }

    @GetMapping("/by-warehouse")
    public ResponseEntity<ApiResponse<SalesByWarehouseResponse>> byWarehouse(@ModelAttribute SalesReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(salesReportService.byWarehouse(filter)));
    }

    @GetMapping("/cancellations")
    public ResponseEntity<ApiResponse<SalesCancellationResponse>> cancellations(@ModelAttribute SalesReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(salesReportService.cancellations(filter)));
    }

    @GetMapping("/discounts")
    public ResponseEntity<ApiResponse<SalesDiscountResponse>> discounts(@ModelAttribute SalesReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(salesReportService.discounts(filter)));
    }

    @GetMapping("/outstanding")
    public ResponseEntity<ApiResponse<SalesOutstandingResponse>> outstanding(@ModelAttribute SalesReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(salesReportService.outstanding(filter)));
    }

    @GetMapping("/by-brand")
    public ResponseEntity<ApiResponse<SalesByBrandResponse>> byBrand(@ModelAttribute SalesReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(salesReportService.byBrand(filter)));
    }

    @GetMapping("/by-customer-type")
    public ResponseEntity<ApiResponse<SalesByCustomerTypeResponse>> byCustomerType(@ModelAttribute SalesReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(salesReportService.byCustomerType(filter)));
    }

    @GetMapping("/pending-orders")
    public ResponseEntity<ApiResponse<SalesPendingOrdersResponse>> pendingOrders(@ModelAttribute SalesReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(salesReportService.pendingOrders(filter)));
    }

    @GetMapping("/pending-deliveries")
    public ResponseEntity<ApiResponse<SalesPendingDeliveriesResponse>> pendingDeliveries(@ModelAttribute SalesReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(salesReportService.pendingDeliveries(filter)));
    }

    @GetMapping("/outstanding-invoices")
    public ResponseEntity<ApiResponse<SalesOutstandingInvoicesResponse>> outstandingInvoices(@ModelAttribute SalesReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(salesReportService.outstandingInvoices(filter)));
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<SalesByPeriodResponse>> monthly(@ModelAttribute SalesReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(salesReportService.monthly(filter)));
    }

    @GetMapping("/yearly")
    public ResponseEntity<ApiResponse<SalesByPeriodResponse>> yearly(@ModelAttribute SalesReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(salesReportService.yearly(filter)));
    }

    @GetMapping("/growth")
    public ResponseEntity<ApiResponse<SalesGrowthResponse>> growth(@ModelAttribute SalesPeriodReportRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(salesReportService.growth(filter)));
    }
}
