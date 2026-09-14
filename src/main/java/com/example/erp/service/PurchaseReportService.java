package com.example.erp.service;

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

public interface PurchaseReportService {

    PurchaseSummaryResponse summary(PurchaseReportFilterRequest filter);

    PurchaseBySupplierResponse bySupplier(PurchaseReportFilterRequest filter);

    PurchaseByProductResponse byProduct(PurchaseReportFilterRequest filter);

    PurchaseDetailResponse detail(PurchaseReportFilterRequest filter);

    PurchaseByDateResponse byDate(PurchaseReportFilterRequest filter);

    PurchaseByCategoryResponse byCategory(PurchaseReportFilterRequest filter);

    PurchaseByBrandResponse byBrand(PurchaseReportFilterRequest filter);

    PurchaseByUomResponse byUom(PurchaseReportFilterRequest filter);

    PurchaseBySupplierTypeResponse bySupplierType(PurchaseReportFilterRequest filter);

    PurchaseByWarehouseResponse byWarehouse(PurchaseReportFilterRequest filter);

    PurchasePendingOrdersResponse pendingOrders(PurchaseReportFilterRequest filter);

    PurchasePendingGoodsReceiptsResponse pendingGoodsReceipts(PurchaseReportFilterRequest filter);

    PurchaseOutstandingInvoicesResponse outstandingInvoices(PurchaseReportFilterRequest filter);

    PurchaseCancellationResponse cancellations(PurchaseReportFilterRequest filter);

    PurchaseDiscountResponse discounts(PurchaseReportFilterRequest filter);

    PurchaseByPeriodResponse monthly(PurchaseReportFilterRequest filter);

    PurchaseByPeriodResponse yearly(PurchaseReportFilterRequest filter);

    SupplierPriceHistoryResponse supplierPriceHistory(SupplierPriceHistoryFilterRequest filter);

    ProductPurchasePriceHistoryResponse productPriceHistory(ProductPurchasePriceHistoryFilterRequest filter);

    SupplierPerformanceResponse supplierPerformance(PurchaseReportFilterRequest filter);
}
