package com.example.erp.service;

import com.example.erp.dto.SalesByBrandResponse;
import com.example.erp.dto.SalesByCategoryResponse;
import com.example.erp.dto.SalesByCustomerGroupResponse;
import com.example.erp.dto.SalesByCustomerResponse;
import com.example.erp.dto.SalesByCustomerTypeResponse;
import com.example.erp.dto.SalesByDateResponse;
import com.example.erp.dto.SalesByProductResponse;
import com.example.erp.dto.SalesBySalespersonResponse;
import com.example.erp.dto.SalesByWarehouseResponse;
import com.example.erp.dto.SalesCancellationResponse;
import com.example.erp.dto.SalesDetailResponse;
import com.example.erp.dto.SalesDiscountResponse;
import com.example.erp.dto.SalesGrowthResponse;
import com.example.erp.dto.SalesByPeriodResponse;
import com.example.erp.dto.SalesOutstandingInvoicesResponse;
import com.example.erp.dto.SalesPendingDeliveriesResponse;
import com.example.erp.dto.SalesPendingOrdersResponse;
import com.example.erp.dto.SalesPeriodReportRequest;
import com.example.erp.dto.SalesReportFilterRequest;
import com.example.erp.dto.SalesSummaryResponse;
import com.example.erp.dto.SalesOutstandingResponse;

public interface SalesReportService {

    SalesSummaryResponse summary(SalesReportFilterRequest filter);

    SalesByProductResponse byProduct(SalesReportFilterRequest filter);

    SalesByCustomerResponse byCustomer(SalesReportFilterRequest filter);

    SalesBySalespersonResponse bySalesperson(SalesReportFilterRequest filter);

    SalesDetailResponse detail(SalesReportFilterRequest filter);

    SalesByDateResponse byDate(SalesReportFilterRequest filter);

    SalesByCategoryResponse byCategory(SalesReportFilterRequest filter);

    SalesByCustomerGroupResponse byCustomerGroup(SalesReportFilterRequest filter);

    SalesByWarehouseResponse byWarehouse(SalesReportFilterRequest filter);

    SalesCancellationResponse cancellations(SalesReportFilterRequest filter);

    SalesDiscountResponse discounts(SalesReportFilterRequest filter);

    SalesOutstandingResponse outstanding(SalesReportFilterRequest filter);

    SalesByBrandResponse byBrand(SalesReportFilterRequest filter);

    SalesByCustomerTypeResponse byCustomerType(SalesReportFilterRequest filter);

    SalesPendingOrdersResponse pendingOrders(SalesReportFilterRequest filter);

    SalesPendingDeliveriesResponse pendingDeliveries(SalesReportFilterRequest filter);

    SalesOutstandingInvoicesResponse outstandingInvoices(SalesReportFilterRequest filter);

    SalesByPeriodResponse monthly(SalesReportFilterRequest filter);

    SalesByPeriodResponse yearly(SalesReportFilterRequest filter);

    SalesGrowthResponse growth(SalesPeriodReportRequest filter);
}
