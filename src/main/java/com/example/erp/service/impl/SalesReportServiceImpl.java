package com.example.erp.service.impl;

import com.example.erp.dto.SalesByBrandResponse;
import com.example.erp.dto.SalesByBrandRowResponse;
import com.example.erp.dto.SalesByCategoryResponse;
import com.example.erp.dto.SalesByCategoryRowResponse;
import com.example.erp.dto.SalesByCustomerGroupResponse;
import com.example.erp.dto.SalesByCustomerGroupRowResponse;
import com.example.erp.dto.SalesByCustomerResponse;
import com.example.erp.dto.SalesByCustomerRowResponse;
import com.example.erp.dto.SalesByCustomerTypeResponse;
import com.example.erp.dto.SalesByCustomerTypeRowResponse;
import com.example.erp.dto.SalesByDateResponse;
import com.example.erp.dto.SalesByDateRowResponse;
import com.example.erp.dto.SalesByPeriodResponse;
import com.example.erp.dto.SalesByPeriodRowResponse;
import com.example.erp.dto.SalesByProductResponse;
import com.example.erp.dto.SalesByProductRowResponse;
import com.example.erp.dto.SalesBySalespersonResponse;
import com.example.erp.dto.SalesBySalespersonRowResponse;
import com.example.erp.dto.SalesByWarehouseResponse;
import com.example.erp.dto.SalesByWarehouseRowResponse;
import com.example.erp.dto.SalesCancellationResponse;
import com.example.erp.dto.SalesCancellationRowResponse;
import com.example.erp.dto.SalesDetailResponse;
import com.example.erp.dto.SalesDetailRowResponse;
import com.example.erp.dto.SalesDiscountResponse;
import com.example.erp.dto.SalesDiscountRowResponse;
import com.example.erp.dto.SalesGrowthResponse;
import com.example.erp.dto.SalesOutstandingInvoiceRowResponse;
import com.example.erp.dto.SalesOutstandingInvoicesResponse;
import com.example.erp.dto.SalesOutstandingResponse;
import com.example.erp.dto.SalesOutstandingRowResponse;
import com.example.erp.dto.SalesPendingDeliveriesResponse;
import com.example.erp.dto.SalesPendingDeliveryRowResponse;
import com.example.erp.dto.SalesPendingOrderRowResponse;
import com.example.erp.dto.SalesPendingOrdersResponse;
import com.example.erp.dto.SalesPeriodReportRequest;
import com.example.erp.dto.SalesReportFilterRequest;
import com.example.erp.dto.SalesStatusBreakdownResponse;
import com.example.erp.dto.SalesSummaryResponse;
import com.example.erp.entity.CreditNote;
import com.example.erp.entity.Customer;
import com.example.erp.entity.CustomerGroup;
import com.example.erp.entity.CustomerType;
import com.example.erp.entity.Delivery;
import com.example.erp.entity.DeliveryStatus;
import com.example.erp.entity.Invoice;
import com.example.erp.entity.InvoiceLine;
import com.example.erp.entity.InvoiceStatus;
import com.example.erp.entity.PaymentAllocation;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductBrand;
import com.example.erp.entity.ProductCategory;
import com.example.erp.entity.SalesOrder;
import com.example.erp.entity.SalesOrderLine;
import com.example.erp.entity.SalesOrderStatus;
import com.example.erp.entity.Warehouse;
import com.example.erp.repository.CreditNoteRepository;
import com.example.erp.repository.CustomerGroupRepository;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.CustomerTypeRepository;
import com.example.erp.repository.DeliveryLineRepository;
import com.example.erp.repository.DeliveryRepository;
import com.example.erp.repository.InvoiceLineRepository;
import com.example.erp.repository.InvoiceRepository;
import com.example.erp.repository.PaymentAllocationRepository;
import com.example.erp.repository.ProductBrandRepository;
import com.example.erp.repository.ProductCategoryRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.SalesOrderLineRepository;
import com.example.erp.repository.SalesOrderRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.service.SalesReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SalesReportServiceImpl implements SalesReportService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    // Sales Detail is a flat per-line export across every matching order —
    // capped so an unfiltered query on a large dataset can't return millions
    // of rows in one response.
    private static final int DETAIL_MAX_ROWS = 2000;

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final CustomerGroupRepository customerGroupRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductBrandRepository productBrandRepository;
    private final CustomerTypeRepository customerTypeRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryLineRepository deliveryLineRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final CreditNoteRepository creditNoteRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;

    @Override
    @Transactional(readOnly = true)
    public SalesSummaryResponse summary(SalesReportFilterRequest filter) {
        List<SalesOrder> orders = matchingOrders(filter);

        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountTotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<SalesOrderStatus, long[]> countByStatus = new HashMap<>();
        Map<SalesOrderStatus, BigDecimal> amountByStatus = new HashMap<>();

        for (SalesOrder order : orders) {
            List<SalesOrderLine> lines = salesOrderLineRepository.findBySalesOrderId(order.getId());
            BigDecimal[] totals = lineTotals(lines);
            totalQuantity = totalQuantity.add(totals[0]);
            subtotal = subtotal.add(totals[1]);
            discountTotal = discountTotal.add(totals[2]);
            taxTotal = taxTotal.add(totals[3]);
            totalAmount = totalAmount.add(totals[4]);

            countByStatus.computeIfAbsent(order.getStatus(), s -> new long[]{0})[0]++;
            amountByStatus.merge(order.getStatus(), totals[4], BigDecimal::add);
        }

        List<SalesStatusBreakdownResponse> byStatus = new ArrayList<>();
        for (SalesOrderStatus status : countByStatus.keySet()) {
            byStatus.add(SalesStatusBreakdownResponse.builder()
                    .status(status.name())
                    .orderCount(countByStatus.get(status)[0])
                    .totalAmount(amountByStatus.getOrDefault(status, BigDecimal.ZERO))
                    .build());
        }
        byStatus.sort(Comparator.comparing(SalesStatusBreakdownResponse::getStatus));

        BigDecimal averageOrderValue = orders.isEmpty() ? BigDecimal.ZERO : totalAmount.divide(BigDecimal.valueOf(orders.size()), 4, RoundingMode.HALF_UP);

        return SalesSummaryResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .orderCount(orders.size())
                .totalQuantity(totalQuantity)
                .subtotal(subtotal)
                .discountTotal(discountTotal)
                .taxTotal(taxTotal)
                .totalAmount(totalAmount)
                .averageOrderValue(averageOrderValue)
                .byStatus(byStatus)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesByProductResponse byProduct(SalesReportFilterRequest filter) {
        List<SalesOrder> orders = matchingOrders(filter);

        Map<Long, BigDecimal[]> byProduct = new HashMap<>(); // [quantity, revenue]
        for (SalesOrder order : orders) {
            for (SalesOrderLine line : salesOrderLineRepository.findBySalesOrderId(order.getId())) {
                BigDecimal[] bucket = byProduct.computeIfAbsent(line.getProductId(), id -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                bucket[0] = bucket[0].add(line.getQuantityOrdered());
                bucket[1] = bucket[1].add(lineRevenue(line.getQuantityOrdered(), line.getUnitPrice(), line.getDiscountPercent(), line.getTaxRate()));
            }
        }

        Map<Long, Product> products = productRepository.findAllById(byProduct.keySet()).stream()
                .collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));

        List<SalesByProductRowResponse> rows = new ArrayList<>();
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal[]> entry : byProduct.entrySet()) {
            Product product = products.get(entry.getKey());
            rows.add(SalesByProductRowResponse.builder()
                    .productId(entry.getKey())
                    .productSku(product == null ? null : product.getSku())
                    .productName(product == null ? null : product.getName())
                    .quantity(entry.getValue()[0])
                    .revenue(entry.getValue()[1])
                    .build());
            totalQuantity = totalQuantity.add(entry.getValue()[0]);
            totalRevenue = totalRevenue.add(entry.getValue()[1]);
        }
        rows.sort(Comparator.comparing(SalesByProductRowResponse::getRevenue).reversed());

        return SalesByProductResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalQuantity(totalQuantity)
                .totalRevenue(totalRevenue)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesByCustomerResponse byCustomer(SalesReportFilterRequest filter) {
        List<SalesOrder> orders = matchingOrders(filter);

        Map<Long, long[]> countByCustomer = new HashMap<>();
        Map<Long, BigDecimal> revenueByCustomer = new HashMap<>();
        for (SalesOrder order : orders) {
            BigDecimal orderTotal = lineTotals(salesOrderLineRepository.findBySalesOrderId(order.getId()))[4];
            countByCustomer.computeIfAbsent(order.getCustomerId(), id -> new long[]{0})[0]++;
            revenueByCustomer.merge(order.getCustomerId(), orderTotal, BigDecimal::add);
        }

        Map<Long, Customer> customers = customerRepository.findAllById(countByCustomer.keySet()).stream()
                .collect(java.util.stream.Collectors.toMap(Customer::getId, c -> c));

        List<SalesByCustomerRowResponse> rows = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Long customerId : countByCustomer.keySet()) {
            Customer customer = customers.get(customerId);
            BigDecimal revenue = revenueByCustomer.getOrDefault(customerId, BigDecimal.ZERO);
            rows.add(SalesByCustomerRowResponse.builder()
                    .customerId(customerId)
                    .customerName(customer == null ? null : customer.getName())
                    .orderCount(countByCustomer.get(customerId)[0])
                    .revenue(revenue)
                    .build());
            totalRevenue = totalRevenue.add(revenue);
        }
        rows.sort(Comparator.comparing(SalesByCustomerRowResponse::getRevenue).reversed());

        return SalesByCustomerResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalRevenue(totalRevenue)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesBySalespersonResponse bySalesperson(SalesReportFilterRequest filter) {
        List<SalesOrder> orders = matchingOrders(filter);

        Map<String, long[]> countBySalesperson = new HashMap<>();
        Map<String, BigDecimal> revenueBySalesperson = new HashMap<>();
        for (SalesOrder order : orders) {
            String salesperson = order.getCreatedBy() == null ? "Unknown" : order.getCreatedBy();
            BigDecimal orderTotal = lineTotals(salesOrderLineRepository.findBySalesOrderId(order.getId()))[4];
            countBySalesperson.computeIfAbsent(salesperson, s -> new long[]{0})[0]++;
            revenueBySalesperson.merge(salesperson, orderTotal, BigDecimal::add);
        }

        List<SalesBySalespersonRowResponse> rows = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (String salesperson : countBySalesperson.keySet()) {
            BigDecimal revenue = revenueBySalesperson.getOrDefault(salesperson, BigDecimal.ZERO);
            rows.add(SalesBySalespersonRowResponse.builder()
                    .salesperson(salesperson)
                    .orderCount(countBySalesperson.get(salesperson)[0])
                    .revenue(revenue)
                    .build());
            totalRevenue = totalRevenue.add(revenue);
        }
        rows.sort(Comparator.comparing(SalesBySalespersonRowResponse::getRevenue).reversed());

        return SalesBySalespersonResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalRevenue(totalRevenue)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesDetailResponse detail(SalesReportFilterRequest filter) {
        List<SalesOrder> orders = matchingOrders(filter);
        orders.sort(Comparator.comparing(SalesOrder::getOrderDate).reversed());

        Map<Long, Customer> customers = customerRepository.findAllById(
                orders.stream().map(SalesOrder::getCustomerId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Customer::getId, c -> c));

        List<SalesDetailRowResponse> rows = new ArrayList<>();
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        boolean truncated = false;
        outer:
        for (SalesOrder order : orders) {
            Customer customer = customers.get(order.getCustomerId());
            for (SalesOrderLine line : salesOrderLineRepository.findBySalesOrderId(order.getId())) {
                if (rows.size() >= DETAIL_MAX_ROWS) {
                    truncated = true;
                    break outer;
                }
                Product product = productRepository.findById(line.getProductId()).orElse(null);
                BigDecimal lineSubtotal = line.getQuantityOrdered().multiply(line.getUnitPrice());
                BigDecimal discountAmount = lineSubtotal.multiply(line.getDiscountPercent()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
                BigDecimal afterDiscount = lineSubtotal.subtract(discountAmount);
                BigDecimal taxAmount = afterDiscount.multiply(line.getTaxRate()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
                BigDecimal lineTotal = afterDiscount.add(taxAmount);
                rows.add(SalesDetailRowResponse.builder()
                        .orderId(order.getId())
                        .soNumber(order.getSoNumber())
                        .orderDate(order.getOrderDate())
                        .status(order.getStatus().name())
                        .customerId(order.getCustomerId())
                        .customerName(customer == null ? null : customer.getName())
                        .productId(line.getProductId())
                        .productName(product == null ? null : product.getName())
                        .productSku(product == null ? null : product.getSku())
                        .quantity(line.getQuantityOrdered())
                        .unitPrice(line.getUnitPrice())
                        .discountPercent(line.getDiscountPercent())
                        .discountAmount(discountAmount)
                        .taxRate(line.getTaxRate())
                        .taxAmount(taxAmount)
                        .lineTotal(lineTotal)
                        .build());
                totalQuantity = totalQuantity.add(line.getQuantityOrdered());
                totalAmount = totalAmount.add(lineTotal);
            }
        }

        return SalesDetailResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .truncated(truncated)
                .totalQuantity(totalQuantity)
                .totalAmount(totalAmount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesByDateResponse byDate(SalesReportFilterRequest filter) {
        List<SalesOrder> orders = matchingOrders(filter);

        Map<java.time.LocalDate, long[]> countByDate = new HashMap<>();
        Map<java.time.LocalDate, BigDecimal> revenueByDate = new HashMap<>();
        for (SalesOrder order : orders) {
            BigDecimal orderTotal = lineTotals(salesOrderLineRepository.findBySalesOrderId(order.getId()))[4];
            countByDate.computeIfAbsent(order.getOrderDate(), d -> new long[]{0})[0]++;
            revenueByDate.merge(order.getOrderDate(), orderTotal, BigDecimal::add);
        }

        List<SalesByDateRowResponse> rows = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (java.time.LocalDate date : countByDate.keySet()) {
            BigDecimal revenue = revenueByDate.getOrDefault(date, BigDecimal.ZERO);
            rows.add(SalesByDateRowResponse.builder()
                    .date(date)
                    .orderCount(countByDate.get(date)[0])
                    .revenue(revenue)
                    .build());
            totalRevenue = totalRevenue.add(revenue);
        }
        rows.sort(Comparator.comparing(SalesByDateRowResponse::getDate));

        return SalesByDateResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalRevenue(totalRevenue)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesByCategoryResponse byCategory(SalesReportFilterRequest filter) {
        List<SalesOrder> orders = matchingOrders(filter);

        List<SalesOrderLine> lines = new ArrayList<>();
        for (SalesOrder order : orders) {
            lines.addAll(salesOrderLineRepository.findBySalesOrderId(order.getId()));
        }
        Map<Long, Product> products = productRepository.findAllById(
                lines.stream().map(SalesOrderLine::getProductId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));

        Map<Long, BigDecimal[]> byCategory = new HashMap<>(); // key: categoryId or 0 for uncategorized — [quantity, revenue]
        for (SalesOrderLine line : lines) {
            Product product = products.get(line.getProductId());
            Long categoryId = product == null ? null : product.getCategoryId();
            BigDecimal[] bucket = byCategory.computeIfAbsent(categoryId == null ? 0L : categoryId, id -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            bucket[0] = bucket[0].add(line.getQuantityOrdered());
            bucket[1] = bucket[1].add(lineRevenue(line.getQuantityOrdered(), line.getUnitPrice(), line.getDiscountPercent(), line.getTaxRate()));
        }

        Map<Long, ProductCategory> categories = productCategoryRepository.findAllById(
                byCategory.keySet().stream().filter(id -> id != 0L).toList()
        ).stream().collect(java.util.stream.Collectors.toMap(ProductCategory::getId, c -> c));

        List<SalesByCategoryRowResponse> rows = new ArrayList<>();
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal[]> entry : byCategory.entrySet()) {
            ProductCategory category = entry.getKey() == 0L ? null : categories.get(entry.getKey());
            rows.add(SalesByCategoryRowResponse.builder()
                    .categoryId(entry.getKey() == 0L ? null : entry.getKey())
                    .categoryName(category == null ? "Uncategorized" : category.getName())
                    .quantity(entry.getValue()[0])
                    .revenue(entry.getValue()[1])
                    .build());
            totalQuantity = totalQuantity.add(entry.getValue()[0]);
            totalRevenue = totalRevenue.add(entry.getValue()[1]);
        }
        rows.sort(Comparator.comparing(SalesByCategoryRowResponse::getRevenue).reversed());

        return SalesByCategoryResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalQuantity(totalQuantity)
                .totalRevenue(totalRevenue)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesByCustomerGroupResponse byCustomerGroup(SalesReportFilterRequest filter) {
        List<SalesOrder> orders = matchingOrders(filter);

        Map<Long, Customer> customers = customerRepository.findAllById(
                orders.stream().map(SalesOrder::getCustomerId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Customer::getId, c -> c));

        Map<Long, long[]> countByGroup = new HashMap<>(); // key: customerGroupId or 0 for ungrouped
        Map<Long, BigDecimal> revenueByGroup = new HashMap<>();
        for (SalesOrder order : orders) {
            Customer customer = customers.get(order.getCustomerId());
            Long groupId = customer == null ? null : customer.getCustomerGroupId();
            long key = groupId == null ? 0L : groupId;
            BigDecimal orderTotal = lineTotals(salesOrderLineRepository.findBySalesOrderId(order.getId()))[4];
            countByGroup.computeIfAbsent(key, id -> new long[]{0})[0]++;
            revenueByGroup.merge(key, orderTotal, BigDecimal::add);
        }

        Map<Long, CustomerGroup> groups = customerGroupRepository.findAllById(
                countByGroup.keySet().stream().filter(id -> id != 0L).toList()
        ).stream().collect(java.util.stream.Collectors.toMap(CustomerGroup::getId, g -> g));

        List<SalesByCustomerGroupRowResponse> rows = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Long key : countByGroup.keySet()) {
            CustomerGroup group = key == 0L ? null : groups.get(key);
            BigDecimal revenue = revenueByGroup.getOrDefault(key, BigDecimal.ZERO);
            rows.add(SalesByCustomerGroupRowResponse.builder()
                    .customerGroupId(key == 0L ? null : key)
                    .customerGroupName(group == null ? "Ungrouped" : group.getName())
                    .orderCount(countByGroup.get(key)[0])
                    .revenue(revenue)
                    .build());
            totalRevenue = totalRevenue.add(revenue);
        }
        rows.sort(Comparator.comparing(SalesByCustomerGroupRowResponse::getRevenue).reversed());

        return SalesByCustomerGroupResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalRevenue(totalRevenue)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesByWarehouseResponse byWarehouse(SalesReportFilterRequest filter) {
        List<SalesOrder> orders = matchingOrders(filter);

        Map<Long, long[]> countByWarehouse = new HashMap<>();
        Map<Long, BigDecimal> revenueByWarehouse = new HashMap<>();
        for (SalesOrder order : orders) {
            BigDecimal orderTotal = lineTotals(salesOrderLineRepository.findBySalesOrderId(order.getId()))[4];
            countByWarehouse.computeIfAbsent(order.getWarehouseId(), id -> new long[]{0})[0]++;
            revenueByWarehouse.merge(order.getWarehouseId(), orderTotal, BigDecimal::add);
        }

        Map<Long, Warehouse> warehouses = warehouseRepository.findAllById(countByWarehouse.keySet()).stream()
                .collect(java.util.stream.Collectors.toMap(Warehouse::getId, w -> w));

        List<SalesByWarehouseRowResponse> rows = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Long warehouseId : countByWarehouse.keySet()) {
            Warehouse warehouse = warehouses.get(warehouseId);
            BigDecimal revenue = revenueByWarehouse.getOrDefault(warehouseId, BigDecimal.ZERO);
            rows.add(SalesByWarehouseRowResponse.builder()
                    .warehouseId(warehouseId)
                    .warehouseName(warehouse == null ? null : warehouse.getName())
                    .orderCount(countByWarehouse.get(warehouseId)[0])
                    .revenue(revenue)
                    .build());
            totalRevenue = totalRevenue.add(revenue);
        }
        rows.sort(Comparator.comparing(SalesByWarehouseRowResponse::getRevenue).reversed());

        return SalesByWarehouseResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalRevenue(totalRevenue)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesCancellationResponse cancellations(SalesReportFilterRequest filter) {
        List<Specification<SalesOrder>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> cb.equal(root.get("status"), SalesOrderStatus.CANCELLED));
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getDateFrom() != null) {
            conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("orderDate"), filter.getDateFrom()));
        }
        if (filter.getDateTo() != null) {
            conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("orderDate"), filter.getDateTo()));
        }
        List<SalesOrder> orders = salesOrderRepository.findAll(Specification.allOf(conditions));

        Map<Long, Customer> customers = customerRepository.findAllById(
                orders.stream().map(SalesOrder::getCustomerId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Customer::getId, c -> c));

        List<SalesCancellationRowResponse> rows = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SalesOrder order : orders) {
            Customer customer = customers.get(order.getCustomerId());
            BigDecimal amount = lineTotals(salesOrderLineRepository.findBySalesOrderId(order.getId()))[4];
            rows.add(SalesCancellationRowResponse.builder()
                    .orderId(order.getId())
                    .soNumber(order.getSoNumber())
                    .orderDate(order.getOrderDate())
                    .customerId(order.getCustomerId())
                    .customerName(customer == null ? null : customer.getName())
                    .amount(amount)
                    .build());
            totalAmount = totalAmount.add(amount);
        }
        rows.sort(Comparator.comparing(SalesCancellationRowResponse::getOrderDate).reversed());

        return SalesCancellationResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .orderCount(rows.size())
                .totalAmount(totalAmount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesDiscountResponse discounts(SalesReportFilterRequest filter) {
        List<SalesOrder> orders = matchingOrders(filter);

        Map<Long, Customer> customers = customerRepository.findAllById(
                orders.stream().map(SalesOrder::getCustomerId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Customer::getId, c -> c));

        List<SalesDiscountRowResponse> rows = new ArrayList<>();
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;
        for (SalesOrder order : orders) {
            Customer customer = customers.get(order.getCustomerId());
            for (SalesOrderLine line : salesOrderLineRepository.findBySalesOrderId(order.getId())) {
                if (line.getDiscountPercent() == null || line.getDiscountPercent().signum() <= 0) continue;
                Product product = productRepository.findById(line.getProductId()).orElse(null);
                BigDecimal lineSubtotal = line.getQuantityOrdered().multiply(line.getUnitPrice());
                BigDecimal discountAmount = lineSubtotal.multiply(line.getDiscountPercent()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
                rows.add(SalesDiscountRowResponse.builder()
                        .orderId(order.getId())
                        .soNumber(order.getSoNumber())
                        .orderDate(order.getOrderDate())
                        .customerId(order.getCustomerId())
                        .customerName(customer == null ? null : customer.getName())
                        .productId(line.getProductId())
                        .productName(product == null ? null : product.getName())
                        .productSku(product == null ? null : product.getSku())
                        .quantity(line.getQuantityOrdered())
                        .unitPrice(line.getUnitPrice())
                        .discountPercent(line.getDiscountPercent())
                        .discountAmount(discountAmount)
                        .build());
                totalDiscountAmount = totalDiscountAmount.add(discountAmount);
            }
        }
        rows.sort(Comparator.comparing(SalesDiscountRowResponse::getDiscountAmount).reversed());

        return SalesDiscountResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalDiscountAmount(totalDiscountAmount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesOutstandingResponse outstanding(SalesReportFilterRequest filter) {
        List<SalesOrder> orders = matchingOrders(filter);

        Map<Long, Customer> customers = customerRepository.findAllById(
                orders.stream().map(SalesOrder::getCustomerId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Customer::getId, c -> c));

        List<SalesOutstandingRowResponse> rows = new ArrayList<>();
        BigDecimal totalOutstandingValue = BigDecimal.ZERO;
        for (SalesOrder order : orders) {
            BigDecimal outstandingQuantity = BigDecimal.ZERO;
            BigDecimal outstandingValue = BigDecimal.ZERO;
            for (SalesOrderLine line : salesOrderLineRepository.findBySalesOrderId(order.getId())) {
                BigDecimal remaining = line.getQuantityOrdered().subtract(line.getQuantityDelivered());
                if (remaining.signum() <= 0) continue;
                outstandingQuantity = outstandingQuantity.add(remaining);
                outstandingValue = outstandingValue.add(lineRevenue(remaining, line.getUnitPrice(), line.getDiscountPercent(), line.getTaxRate()));
            }
            if (outstandingQuantity.signum() <= 0) continue;
            Customer customer = customers.get(order.getCustomerId());
            rows.add(SalesOutstandingRowResponse.builder()
                    .orderId(order.getId())
                    .soNumber(order.getSoNumber())
                    .orderDate(order.getOrderDate())
                    .expectedDate(order.getExpectedDate())
                    .status(order.getStatus().name())
                    .customerId(order.getCustomerId())
                    .customerName(customer == null ? null : customer.getName())
                    .outstandingQuantity(outstandingQuantity)
                    .outstandingValue(outstandingValue)
                    .build());
            totalOutstandingValue = totalOutstandingValue.add(outstandingValue);
        }
        rows.sort(Comparator.comparing(SalesOutstandingRowResponse::getOrderDate));

        return SalesOutstandingResponse.builder()
                .rows(rows)
                .orderCount(rows.size())
                .totalOutstandingValue(totalOutstandingValue)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesByBrandResponse byBrand(SalesReportFilterRequest filter) {
        List<SalesOrder> orders = matchingOrders(filter);

        List<SalesOrderLine> lines = new ArrayList<>();
        for (SalesOrder order : orders) {
            lines.addAll(salesOrderLineRepository.findBySalesOrderId(order.getId()));
        }
        Map<Long, Product> products = productRepository.findAllById(
                lines.stream().map(SalesOrderLine::getProductId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));

        Map<Long, BigDecimal[]> byBrand = new HashMap<>(); // key: brandId or 0 for unbranded — [quantity, revenue]
        for (SalesOrderLine line : lines) {
            Product product = products.get(line.getProductId());
            Long brandId = product == null ? null : product.getBrandId();
            BigDecimal[] bucket = byBrand.computeIfAbsent(brandId == null ? 0L : brandId, id -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            bucket[0] = bucket[0].add(line.getQuantityOrdered());
            bucket[1] = bucket[1].add(lineRevenue(line.getQuantityOrdered(), line.getUnitPrice(), line.getDiscountPercent(), line.getTaxRate()));
        }

        Map<Long, ProductBrand> brands = productBrandRepository.findAllById(
                byBrand.keySet().stream().filter(id -> id != 0L).toList()
        ).stream().collect(java.util.stream.Collectors.toMap(ProductBrand::getId, b -> b));

        List<SalesByBrandRowResponse> rows = new ArrayList<>();
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal[]> entry : byBrand.entrySet()) {
            ProductBrand brand = entry.getKey() == 0L ? null : brands.get(entry.getKey());
            rows.add(SalesByBrandRowResponse.builder()
                    .brandId(entry.getKey() == 0L ? null : entry.getKey())
                    .brandName(brand == null ? "Unbranded" : brand.getName())
                    .quantity(entry.getValue()[0])
                    .revenue(entry.getValue()[1])
                    .build());
            totalQuantity = totalQuantity.add(entry.getValue()[0]);
            totalRevenue = totalRevenue.add(entry.getValue()[1]);
        }
        rows.sort(Comparator.comparing(SalesByBrandRowResponse::getRevenue).reversed());

        return SalesByBrandResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalQuantity(totalQuantity)
                .totalRevenue(totalRevenue)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesByCustomerTypeResponse byCustomerType(SalesReportFilterRequest filter) {
        List<SalesOrder> orders = matchingOrders(filter);

        Map<Long, Customer> customers = customerRepository.findAllById(
                orders.stream().map(SalesOrder::getCustomerId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Customer::getId, c -> c));

        Map<Long, long[]> countByType = new HashMap<>(); // key: customerTypeId or 0 for untyped
        Map<Long, BigDecimal> revenueByType = new HashMap<>();
        for (SalesOrder order : orders) {
            Customer customer = customers.get(order.getCustomerId());
            Long typeId = customer == null ? null : customer.getCustomerTypeId();
            long key = typeId == null ? 0L : typeId;
            BigDecimal orderTotal = lineTotals(salesOrderLineRepository.findBySalesOrderId(order.getId()))[4];
            countByType.computeIfAbsent(key, id -> new long[]{0})[0]++;
            revenueByType.merge(key, orderTotal, BigDecimal::add);
        }

        Map<Long, CustomerType> types = customerTypeRepository.findAllById(
                countByType.keySet().stream().filter(id -> id != 0L).toList()
        ).stream().collect(java.util.stream.Collectors.toMap(CustomerType::getId, t -> t));

        List<SalesByCustomerTypeRowResponse> rows = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Long key : countByType.keySet()) {
            CustomerType type = key == 0L ? null : types.get(key);
            BigDecimal revenue = revenueByType.getOrDefault(key, BigDecimal.ZERO);
            rows.add(SalesByCustomerTypeRowResponse.builder()
                    .customerTypeId(key == 0L ? null : key)
                    .customerTypeName(type == null ? "Untyped" : type.getName())
                    .orderCount(countByType.get(key)[0])
                    .revenue(revenue)
                    .build());
            totalRevenue = totalRevenue.add(revenue);
        }
        rows.sort(Comparator.comparing(SalesByCustomerTypeRowResponse::getRevenue).reversed());

        return SalesByCustomerTypeResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalRevenue(totalRevenue)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesPendingOrdersResponse pendingOrders(SalesReportFilterRequest filter) {
        List<Specification<SalesOrder>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> root.get("status").in(List.of(SalesOrderStatus.DRAFT, SalesOrderStatus.SUBMITTED)));
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getDateFrom() != null) conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("orderDate"), filter.getDateFrom()));
        if (filter.getDateTo() != null) conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("orderDate"), filter.getDateTo()));
        List<SalesOrder> orders = salesOrderRepository.findAll(Specification.allOf(conditions));

        Map<Long, Customer> customers = customerRepository.findAllById(
                orders.stream().map(SalesOrder::getCustomerId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Customer::getId, c -> c));

        List<SalesPendingOrderRowResponse> rows = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SalesOrder order : orders) {
            Customer customer = customers.get(order.getCustomerId());
            BigDecimal amount = lineTotals(salesOrderLineRepository.findBySalesOrderId(order.getId()))[4];
            rows.add(SalesPendingOrderRowResponse.builder()
                    .orderId(order.getId())
                    .soNumber(order.getSoNumber())
                    .orderDate(order.getOrderDate())
                    .status(order.getStatus().name())
                    .customerId(order.getCustomerId())
                    .customerName(customer == null ? null : customer.getName())
                    .amount(amount)
                    .build());
            totalAmount = totalAmount.add(amount);
        }
        rows.sort(Comparator.comparing(SalesPendingOrderRowResponse::getOrderDate));

        return SalesPendingOrdersResponse.builder()
                .rows(rows)
                .orderCount(rows.size())
                .totalAmount(totalAmount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesPendingDeliveriesResponse pendingDeliveries(SalesReportFilterRequest filter) {
        List<Specification<Delivery>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> root.get("status").in(List.of(DeliveryStatus.PENDING, DeliveryStatus.PICKED, DeliveryStatus.PACKED, DeliveryStatus.SHIPPED)));
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getDateFrom() != null) conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("deliveryDate"), filter.getDateFrom()));
        if (filter.getDateTo() != null) conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("deliveryDate"), filter.getDateTo()));
        List<Delivery> deliveries = deliveryRepository.findAll(Specification.allOf(conditions));

        Map<Long, SalesOrder> salesOrders = salesOrderRepository.findAllById(
                deliveries.stream().map(Delivery::getSalesOrderId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(SalesOrder::getId, o -> o));
        Map<Long, Customer> customers = customerRepository.findAllById(
                salesOrders.values().stream().map(SalesOrder::getCustomerId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Customer::getId, c -> c));
        Map<Long, Warehouse> warehouses = warehouseRepository.findAllById(
                deliveries.stream().map(Delivery::getWarehouseId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Warehouse::getId, w -> w));

        List<SalesPendingDeliveryRowResponse> rows = new ArrayList<>();
        for (Delivery delivery : deliveries) {
            SalesOrder order = salesOrders.get(delivery.getSalesOrderId());
            Customer customer = order == null ? null : customers.get(order.getCustomerId());
            Warehouse warehouse = warehouses.get(delivery.getWarehouseId());
            rows.add(SalesPendingDeliveryRowResponse.builder()
                    .deliveryId(delivery.getId())
                    .deliveryNumber(delivery.getDeliveryNumber())
                    .deliveryDate(delivery.getDeliveryDate())
                    .status(delivery.getStatus().name())
                    .salesOrderId(delivery.getSalesOrderId())
                    .soNumber(order == null ? null : order.getSoNumber())
                    .customerId(order == null ? null : order.getCustomerId())
                    .customerName(customer == null ? null : customer.getName())
                    .warehouseId(delivery.getWarehouseId())
                    .warehouseName(warehouse == null ? null : warehouse.getName())
                    .lineCount(deliveryLineRepository.findByDeliveryId(delivery.getId()).size())
                    .build());
        }
        rows.sort(Comparator.comparing(SalesPendingDeliveryRowResponse::getDeliveryDate));

        return SalesPendingDeliveriesResponse.builder()
                .rows(rows)
                .deliveryCount(rows.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesOutstandingInvoicesResponse outstandingInvoices(SalesReportFilterRequest filter) {
        List<Specification<Invoice>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> cb.equal(root.get("status"), InvoiceStatus.APPROVED));
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getDateFrom() != null) conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("invoiceDate"), filter.getDateFrom()));
        if (filter.getDateTo() != null) conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("invoiceDate"), filter.getDateTo()));
        List<Invoice> invoices = invoiceRepository.findAll(Specification.allOf(conditions));

        Map<Long, Customer> customers = customerRepository.findAllById(
                invoices.stream().map(Invoice::getCustomerId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Customer::getId, c -> c));

        LocalDate today = LocalDate.now();
        List<SalesOutstandingInvoiceRowResponse> rows = new ArrayList<>();
        BigDecimal totalOutstanding = BigDecimal.ZERO;
        for (Invoice invoice : invoices) {
            BigDecimal totalAmount = invoiceLineTotal(invoiceLineRepository.findByInvoiceId(invoice.getId()));
            BigDecimal credited = creditNoteRepository.findByInvoiceId(invoice.getId()).stream()
                    .map(CreditNote::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal paid = paymentAllocationRepository.findByInvoiceId(invoice.getId()).stream()
                    .map(PaymentAllocation::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal outstandingAmount = totalAmount.subtract(credited).subtract(paid);
            if (outstandingAmount.signum() <= 0) continue;

            Customer customer = customers.get(invoice.getCustomerId());
            rows.add(SalesOutstandingInvoiceRowResponse.builder()
                    .invoiceId(invoice.getId())
                    .invoiceNumber(invoice.getInvoiceNumber())
                    .invoiceDate(invoice.getInvoiceDate())
                    .dueDate(invoice.getDueDate())
                    .daysOverdue(invoice.getDueDate() == null ? 0 : ChronoUnit.DAYS.between(invoice.getDueDate(), today))
                    .customerId(invoice.getCustomerId())
                    .customerName(customer == null ? null : customer.getName())
                    .totalAmount(totalAmount)
                    .outstandingAmount(outstandingAmount)
                    .build());
            totalOutstanding = totalOutstanding.add(outstandingAmount);
        }
        rows.sort(Comparator.comparing(SalesOutstandingInvoiceRowResponse::getDaysOverdue).reversed());

        return SalesOutstandingInvoicesResponse.builder()
                .rows(rows)
                .invoiceCount(rows.size())
                .totalOutstanding(totalOutstanding)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesByPeriodResponse monthly(SalesReportFilterRequest filter) {
        List<SalesOrder> orders = matchingOrders(filter);

        Map<YearMonth, long[]> countByMonth = new HashMap<>();
        Map<YearMonth, BigDecimal> revenueByMonth = new HashMap<>();
        for (SalesOrder order : orders) {
            YearMonth month = YearMonth.from(order.getOrderDate());
            BigDecimal orderTotal = lineTotals(salesOrderLineRepository.findBySalesOrderId(order.getId()))[4];
            countByMonth.computeIfAbsent(month, m -> new long[]{0})[0]++;
            revenueByMonth.merge(month, orderTotal, BigDecimal::add);
        }

        List<SalesByPeriodRowResponse> rows = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (YearMonth month : countByMonth.keySet()) {
            BigDecimal revenue = revenueByMonth.getOrDefault(month, BigDecimal.ZERO);
            rows.add(SalesByPeriodRowResponse.builder().period(month.toString()).orderCount(countByMonth.get(month)[0]).revenue(revenue).build());
            totalRevenue = totalRevenue.add(revenue);
        }
        rows.sort(Comparator.comparing(SalesByPeriodRowResponse::getPeriod));

        return SalesByPeriodResponse.builder().dateFrom(filter.getDateFrom()).dateTo(filter.getDateTo()).rows(rows).totalRevenue(totalRevenue).build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesByPeriodResponse yearly(SalesReportFilterRequest filter) {
        List<SalesOrder> orders = matchingOrders(filter);

        Map<Year, long[]> countByYear = new HashMap<>();
        Map<Year, BigDecimal> revenueByYear = new HashMap<>();
        for (SalesOrder order : orders) {
            Year year = Year.from(order.getOrderDate());
            BigDecimal orderTotal = lineTotals(salesOrderLineRepository.findBySalesOrderId(order.getId()))[4];
            countByYear.computeIfAbsent(year, y -> new long[]{0})[0]++;
            revenueByYear.merge(year, orderTotal, BigDecimal::add);
        }

        List<SalesByPeriodRowResponse> rows = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Year year : countByYear.keySet()) {
            BigDecimal revenue = revenueByYear.getOrDefault(year, BigDecimal.ZERO);
            rows.add(SalesByPeriodRowResponse.builder().period(year.toString()).orderCount(countByYear.get(year)[0]).revenue(revenue).build());
            totalRevenue = totalRevenue.add(revenue);
        }
        rows.sort(Comparator.comparing(SalesByPeriodRowResponse::getPeriod));

        return SalesByPeriodResponse.builder().dateFrom(filter.getDateFrom()).dateTo(filter.getDateTo()).rows(rows).totalRevenue(totalRevenue).build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesGrowthResponse growth(SalesPeriodReportRequest filter) {
        LocalDate currentFrom = filter.getDateFrom();
        LocalDate currentTo = filter.getDateTo();
        long days = ChronoUnit.DAYS.between(currentFrom, currentTo) + 1;
        LocalDate previousTo = currentFrom.minusDays(1);
        LocalDate previousFrom = previousTo.minusDays(days - 1);

        BigDecimal[] current = revenueAndCountInRange(filter.getCompanyId(), currentFrom, currentTo);
        BigDecimal[] previous = revenueAndCountInRange(filter.getCompanyId(), previousFrom, previousTo);

        return SalesGrowthResponse.builder()
                .currentFrom(currentFrom)
                .currentTo(currentTo)
                .currentRevenue(current[0])
                .currentOrderCount(current[1].longValue())
                .previousFrom(previousFrom)
                .previousTo(previousTo)
                .previousRevenue(previous[0])
                .previousOrderCount(previous[1].longValue())
                .revenueGrowthPercent(percentChange(previous[0], current[0]))
                .orderGrowthPercent(percentChange(previous[1], current[1]))
                .build();
    }

    private BigDecimal[] revenueAndCountInRange(Long companyId, LocalDate from, LocalDate to) {
        List<Specification<SalesOrder>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> cb.notEqual(root.get("status"), SalesOrderStatus.CANCELLED));
        conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("orderDate"), from));
        conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("orderDate"), to));
        if (companyId != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), companyId));
        List<SalesOrder> orders = salesOrderRepository.findAll(Specification.allOf(conditions));

        BigDecimal revenue = BigDecimal.ZERO;
        for (SalesOrder order : orders) {
            revenue = revenue.add(lineTotals(salesOrderLineRepository.findBySalesOrderId(order.getId()))[4]);
        }
        return new BigDecimal[]{revenue, BigDecimal.valueOf(orders.size())};
    }

    // Null when the base is zero — percent change is undefined, not infinite/zero.
    private BigDecimal percentChange(BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.signum() == 0) return null;
        return current.subtract(previous).multiply(HUNDRED).divide(previous, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal invoiceLineTotal(List<InvoiceLine> lines) {
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceLine line : lines) {
            total = total.add(lineRevenue(line.getQuantity(), line.getUnitPrice(), line.getDiscountPercent(), line.getTaxRate()));
        }
        return total;
    }

    // Excludes CANCELLED unless the caller explicitly filters to it — a
    // cancelled order was never really a sale.
    private List<SalesOrder> matchingOrders(SalesReportFilterRequest filter) {
        List<Specification<SalesOrder>> conditions = new ArrayList<>();
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        } else {
            conditions.add((root, query, cb) -> cb.notEqual(root.get("status"), SalesOrderStatus.CANCELLED));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getDateFrom() != null) {
            conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("orderDate"), filter.getDateFrom()));
        }
        if (filter.getDateTo() != null) {
            conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("orderDate"), filter.getDateTo()));
        }
        return salesOrderRepository.findAll(Specification.allOf(conditions));
    }

    // Returns [quantity, subtotal, discountAmount, taxAmount, totalAmount].
    private BigDecimal[] lineTotals(List<SalesOrderLine> lines) {
        BigDecimal quantity = BigDecimal.ZERO;
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        for (SalesOrderLine line : lines) {
            BigDecimal lineSubtotal = line.getQuantityOrdered().multiply(line.getUnitPrice());
            BigDecimal lineDiscount = lineSubtotal.multiply(line.getDiscountPercent()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
            BigDecimal afterDiscount = lineSubtotal.subtract(lineDiscount);
            BigDecimal lineTax = afterDiscount.multiply(line.getTaxRate()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
            quantity = quantity.add(line.getQuantityOrdered());
            subtotal = subtotal.add(lineSubtotal);
            discountAmount = discountAmount.add(lineDiscount);
            taxAmount = taxAmount.add(lineTax);
        }
        BigDecimal totalAmount = subtotal.subtract(discountAmount).add(taxAmount);
        return new BigDecimal[]{quantity, subtotal, discountAmount, taxAmount, totalAmount};
    }

    private BigDecimal lineRevenue(BigDecimal quantity, BigDecimal unitPrice, BigDecimal discountPercent, BigDecimal taxRate) {
        BigDecimal lineSubtotal = quantity.multiply(unitPrice);
        BigDecimal discount = lineSubtotal.multiply(discountPercent).divide(HUNDRED, 4, RoundingMode.HALF_UP);
        BigDecimal afterDiscount = lineSubtotal.subtract(discount);
        BigDecimal tax = afterDiscount.multiply(taxRate).divide(HUNDRED, 4, RoundingMode.HALF_UP);
        return afterDiscount.add(tax);
    }
}
