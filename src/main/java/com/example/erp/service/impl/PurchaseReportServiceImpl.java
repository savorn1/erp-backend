package com.example.erp.service.impl;

import com.example.erp.dto.ProductPurchasePriceHistoryFilterRequest;
import com.example.erp.dto.ProductPurchasePriceHistoryResponse;
import com.example.erp.dto.ProductPurchasePriceHistoryRowResponse;
import com.example.erp.dto.PurchaseByBrandResponse;
import com.example.erp.dto.PurchaseByBrandRowResponse;
import com.example.erp.dto.PurchaseByCategoryResponse;
import com.example.erp.dto.PurchaseByCategoryRowResponse;
import com.example.erp.dto.PurchaseByDateResponse;
import com.example.erp.dto.PurchaseByDateRowResponse;
import com.example.erp.dto.PurchaseByPeriodResponse;
import com.example.erp.dto.PurchaseByPeriodRowResponse;
import com.example.erp.dto.PurchaseByProductResponse;
import com.example.erp.dto.PurchaseByProductRowResponse;
import com.example.erp.dto.PurchaseBySupplierResponse;
import com.example.erp.dto.PurchaseBySupplierRowResponse;
import com.example.erp.dto.PurchaseBySupplierTypeResponse;
import com.example.erp.dto.PurchaseBySupplierTypeRowResponse;
import com.example.erp.dto.PurchaseByUomResponse;
import com.example.erp.dto.PurchaseByUomRowResponse;
import com.example.erp.dto.PurchaseByWarehouseResponse;
import com.example.erp.dto.PurchaseByWarehouseRowResponse;
import com.example.erp.dto.PurchaseCancellationResponse;
import com.example.erp.dto.PurchaseCancellationRowResponse;
import com.example.erp.dto.PurchaseDetailResponse;
import com.example.erp.dto.PurchaseDetailRowResponse;
import com.example.erp.dto.PurchaseDiscountResponse;
import com.example.erp.dto.PurchaseDiscountRowResponse;
import com.example.erp.dto.PurchaseOutstandingInvoiceRowResponse;
import com.example.erp.dto.PurchaseOutstandingInvoicesResponse;
import com.example.erp.dto.PurchasePendingGoodsReceiptRowResponse;
import com.example.erp.dto.PurchasePendingGoodsReceiptsResponse;
import com.example.erp.dto.PurchasePendingOrderRowResponse;
import com.example.erp.dto.PurchasePendingOrdersResponse;
import com.example.erp.dto.PurchaseReportFilterRequest;
import com.example.erp.dto.PurchaseStatusBreakdownResponse;
import com.example.erp.dto.PurchaseSummaryResponse;
import com.example.erp.dto.SupplierPerformanceResponse;
import com.example.erp.dto.SupplierPerformanceRowResponse;
import com.example.erp.dto.SupplierPriceHistoryFilterRequest;
import com.example.erp.dto.SupplierPriceHistoryResponse;
import com.example.erp.dto.SupplierPriceHistoryRowResponse;
import com.example.erp.entity.GoodsReceipt;
import com.example.erp.entity.GoodsReceiptLine;
import com.example.erp.entity.GoodsReceiptStatus;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductBrand;
import com.example.erp.entity.ProductCategory;
import com.example.erp.entity.PurchaseCreditNote;
import com.example.erp.entity.PurchaseInvoice;
import com.example.erp.entity.PurchaseInvoiceLine;
import com.example.erp.entity.PurchaseInvoiceStatus;
import com.example.erp.entity.PurchaseOrder;
import com.example.erp.entity.PurchaseOrderLine;
import com.example.erp.entity.PurchaseOrderStatus;
import com.example.erp.entity.Supplier;
import com.example.erp.entity.SupplierPaymentAllocation;
import com.example.erp.entity.SupplierType;
import com.example.erp.entity.UnitOfMeasure;
import com.example.erp.entity.Warehouse;
import com.example.erp.repository.GoodsReceiptLineRepository;
import com.example.erp.repository.GoodsReceiptRepository;
import com.example.erp.repository.ProductBrandRepository;
import com.example.erp.repository.ProductCategoryRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.PurchaseCreditNoteRepository;
import com.example.erp.repository.PurchaseInvoiceLineRepository;
import com.example.erp.repository.PurchaseInvoiceRepository;
import com.example.erp.repository.PurchaseOrderLineRepository;
import com.example.erp.repository.PurchaseOrderRepository;
import com.example.erp.repository.SupplierPaymentAllocationRepository;
import com.example.erp.repository.SupplierRepository;
import com.example.erp.repository.SupplierTypeRepository;
import com.example.erp.repository.UnitOfMeasureRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.service.PurchaseReportService;
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
public class PurchaseReportServiceImpl implements PurchaseReportService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    // Purchase Detail is a flat per-line export across every matching order —
    // capped so an unfiltered query on a large dataset can't return millions
    // of rows in one response.
    private static final int DETAIL_MAX_ROWS = 2000;

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductBrandRepository productBrandRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final SupplierTypeRepository supplierTypeRepository;
    private final WarehouseRepository warehouseRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final GoodsReceiptLineRepository goodsReceiptLineRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final PurchaseInvoiceLineRepository purchaseInvoiceLineRepository;
    private final PurchaseCreditNoteRepository purchaseCreditNoteRepository;
    private final SupplierPaymentAllocationRepository supplierPaymentAllocationRepository;

    @Override
    @Transactional(readOnly = true)
    public PurchaseSummaryResponse summary(PurchaseReportFilterRequest filter) {
        List<PurchaseOrder> orders = matchingOrders(filter);

        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountTotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<PurchaseOrderStatus, long[]> countByStatus = new HashMap<>();
        Map<PurchaseOrderStatus, BigDecimal> amountByStatus = new HashMap<>();

        for (PurchaseOrder order : orders) {
            List<PurchaseOrderLine> lines = purchaseOrderLineRepository.findByPurchaseOrderId(order.getId());
            BigDecimal[] totals = lineTotals(lines);
            totalQuantity = totalQuantity.add(totals[0]);
            subtotal = subtotal.add(totals[1]);
            discountTotal = discountTotal.add(totals[2]);
            taxTotal = taxTotal.add(totals[3]);
            totalAmount = totalAmount.add(totals[4]);

            countByStatus.computeIfAbsent(order.getStatus(), s -> new long[]{0})[0]++;
            amountByStatus.merge(order.getStatus(), totals[4], BigDecimal::add);
        }

        List<PurchaseStatusBreakdownResponse> byStatus = new ArrayList<>();
        for (PurchaseOrderStatus status : countByStatus.keySet()) {
            byStatus.add(PurchaseStatusBreakdownResponse.builder()
                    .status(status.name())
                    .orderCount(countByStatus.get(status)[0])
                    .totalAmount(amountByStatus.getOrDefault(status, BigDecimal.ZERO))
                    .build());
        }
        byStatus.sort(Comparator.comparing(PurchaseStatusBreakdownResponse::getStatus));

        BigDecimal averageOrderValue = orders.isEmpty() ? BigDecimal.ZERO : totalAmount.divide(BigDecimal.valueOf(orders.size()), 4, RoundingMode.HALF_UP);

        return PurchaseSummaryResponse.builder()
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
    public PurchaseBySupplierResponse bySupplier(PurchaseReportFilterRequest filter) {
        List<PurchaseOrder> orders = matchingOrders(filter);

        Map<Long, long[]> countBySupplier = new HashMap<>();
        Map<Long, BigDecimal> amountBySupplier = new HashMap<>();
        for (PurchaseOrder order : orders) {
            BigDecimal orderTotal = lineTotals(purchaseOrderLineRepository.findByPurchaseOrderId(order.getId()))[4];
            countBySupplier.computeIfAbsent(order.getSupplierId(), id -> new long[]{0})[0]++;
            amountBySupplier.merge(order.getSupplierId(), orderTotal, BigDecimal::add);
        }

        Map<Long, Supplier> suppliers = supplierRepository.findAllById(countBySupplier.keySet()).stream()
                .collect(java.util.stream.Collectors.toMap(Supplier::getId, s -> s));

        List<PurchaseBySupplierRowResponse> rows = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Long supplierId : countBySupplier.keySet()) {
            Supplier supplier = suppliers.get(supplierId);
            BigDecimal amount = amountBySupplier.getOrDefault(supplierId, BigDecimal.ZERO);
            rows.add(PurchaseBySupplierRowResponse.builder()
                    .supplierId(supplierId)
                    .supplierName(supplier == null ? null : supplier.getName())
                    .orderCount(countBySupplier.get(supplierId)[0])
                    .amount(amount)
                    .build());
            totalAmount = totalAmount.add(amount);
        }
        rows.sort(Comparator.comparing(PurchaseBySupplierRowResponse::getAmount).reversed());

        return PurchaseBySupplierResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalAmount(totalAmount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseByProductResponse byProduct(PurchaseReportFilterRequest filter) {
        List<PurchaseOrder> orders = matchingOrders(filter);

        Map<Long, BigDecimal[]> byProduct = new HashMap<>(); // [quantity, amount]
        for (PurchaseOrder order : orders) {
            for (PurchaseOrderLine line : purchaseOrderLineRepository.findByPurchaseOrderId(order.getId())) {
                BigDecimal[] bucket = byProduct.computeIfAbsent(line.getProductId(), id -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                bucket[0] = bucket[0].add(line.getQuantityOrdered());
                bucket[1] = bucket[1].add(lineAmount(line.getQuantityOrdered(), line.getUnitCost(), line.getDiscountPercent(), line.getTaxRate()));
            }
        }

        Map<Long, Product> products = productRepository.findAllById(byProduct.keySet()).stream()
                .collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));

        List<PurchaseByProductRowResponse> rows = new ArrayList<>();
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal[]> entry : byProduct.entrySet()) {
            Product product = products.get(entry.getKey());
            rows.add(PurchaseByProductRowResponse.builder()
                    .productId(entry.getKey())
                    .productSku(product == null ? null : product.getSku())
                    .productName(product == null ? null : product.getName())
                    .quantity(entry.getValue()[0])
                    .amount(entry.getValue()[1])
                    .build());
            totalQuantity = totalQuantity.add(entry.getValue()[0]);
            totalAmount = totalAmount.add(entry.getValue()[1]);
        }
        rows.sort(Comparator.comparing(PurchaseByProductRowResponse::getAmount).reversed());

        return PurchaseByProductResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalQuantity(totalQuantity)
                .totalAmount(totalAmount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseDetailResponse detail(PurchaseReportFilterRequest filter) {
        List<PurchaseOrder> orders = matchingOrders(filter);
        orders.sort(Comparator.comparing(PurchaseOrder::getOrderDate).reversed());

        Map<Long, Supplier> suppliers = supplierRepository.findAllById(
                orders.stream().map(PurchaseOrder::getSupplierId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Supplier::getId, s -> s));

        List<PurchaseDetailRowResponse> rows = new ArrayList<>();
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        boolean truncated = false;
        outer:
        for (PurchaseOrder order : orders) {
            Supplier supplier = suppliers.get(order.getSupplierId());
            for (PurchaseOrderLine line : purchaseOrderLineRepository.findByPurchaseOrderId(order.getId())) {
                if (rows.size() >= DETAIL_MAX_ROWS) {
                    truncated = true;
                    break outer;
                }
                Product product = productRepository.findById(line.getProductId()).orElse(null);
                BigDecimal lineSubtotal = line.getQuantityOrdered().multiply(line.getUnitCost());
                BigDecimal discountAmount = lineSubtotal.multiply(line.getDiscountPercent()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
                BigDecimal afterDiscount = lineSubtotal.subtract(discountAmount);
                BigDecimal taxAmount = afterDiscount.multiply(line.getTaxRate()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
                BigDecimal lineTotal = afterDiscount.add(taxAmount);
                rows.add(PurchaseDetailRowResponse.builder()
                        .orderId(order.getId())
                        .poNumber(order.getPoNumber())
                        .orderDate(order.getOrderDate())
                        .status(order.getStatus().name())
                        .supplierId(order.getSupplierId())
                        .supplierName(supplier == null ? null : supplier.getName())
                        .productId(line.getProductId())
                        .productName(product == null ? null : product.getName())
                        .productSku(product == null ? null : product.getSku())
                        .quantity(line.getQuantityOrdered())
                        .unitCost(line.getUnitCost())
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

        return PurchaseDetailResponse.builder()
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
    public PurchaseByDateResponse byDate(PurchaseReportFilterRequest filter) {
        List<PurchaseOrder> orders = matchingOrders(filter);

        Map<LocalDate, long[]> countByDate = new HashMap<>();
        Map<LocalDate, BigDecimal> amountByDate = new HashMap<>();
        for (PurchaseOrder order : orders) {
            BigDecimal orderTotal = lineTotals(purchaseOrderLineRepository.findByPurchaseOrderId(order.getId()))[4];
            countByDate.computeIfAbsent(order.getOrderDate(), d -> new long[]{0})[0]++;
            amountByDate.merge(order.getOrderDate(), orderTotal, BigDecimal::add);
        }

        List<PurchaseByDateRowResponse> rows = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (LocalDate date : countByDate.keySet()) {
            BigDecimal amount = amountByDate.getOrDefault(date, BigDecimal.ZERO);
            rows.add(PurchaseByDateRowResponse.builder().date(date).orderCount(countByDate.get(date)[0]).amount(amount).build());
            totalAmount = totalAmount.add(amount);
        }
        rows.sort(Comparator.comparing(PurchaseByDateRowResponse::getDate));

        return PurchaseByDateResponse.builder().dateFrom(filter.getDateFrom()).dateTo(filter.getDateTo()).rows(rows).totalAmount(totalAmount).build();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseByCategoryResponse byCategory(PurchaseReportFilterRequest filter) {
        List<PurchaseOrder> orders = matchingOrders(filter);

        List<PurchaseOrderLine> lines = new ArrayList<>();
        for (PurchaseOrder order : orders) {
            lines.addAll(purchaseOrderLineRepository.findByPurchaseOrderId(order.getId()));
        }
        Map<Long, Product> products = productRepository.findAllById(
                lines.stream().map(PurchaseOrderLine::getProductId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));

        Map<Long, BigDecimal[]> byCategory = new HashMap<>(); // key: categoryId or 0 — [quantity, amount]
        for (PurchaseOrderLine line : lines) {
            Product product = products.get(line.getProductId());
            Long categoryId = product == null ? null : product.getCategoryId();
            BigDecimal[] bucket = byCategory.computeIfAbsent(categoryId == null ? 0L : categoryId, id -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            bucket[0] = bucket[0].add(line.getQuantityOrdered());
            bucket[1] = bucket[1].add(lineAmount(line.getQuantityOrdered(), line.getUnitCost(), line.getDiscountPercent(), line.getTaxRate()));
        }

        Map<Long, ProductCategory> categories = productCategoryRepository.findAllById(
                byCategory.keySet().stream().filter(id -> id != 0L).toList()
        ).stream().collect(java.util.stream.Collectors.toMap(ProductCategory::getId, c -> c));

        List<PurchaseByCategoryRowResponse> rows = new ArrayList<>();
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal[]> entry : byCategory.entrySet()) {
            ProductCategory category = entry.getKey() == 0L ? null : categories.get(entry.getKey());
            rows.add(PurchaseByCategoryRowResponse.builder()
                    .categoryId(entry.getKey() == 0L ? null : entry.getKey())
                    .categoryName(category == null ? "Uncategorized" : category.getName())
                    .quantity(entry.getValue()[0])
                    .amount(entry.getValue()[1])
                    .build());
            totalQuantity = totalQuantity.add(entry.getValue()[0]);
            totalAmount = totalAmount.add(entry.getValue()[1]);
        }
        rows.sort(Comparator.comparing(PurchaseByCategoryRowResponse::getAmount).reversed());

        return PurchaseByCategoryResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalQuantity(totalQuantity)
                .totalAmount(totalAmount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseByBrandResponse byBrand(PurchaseReportFilterRequest filter) {
        List<PurchaseOrder> orders = matchingOrders(filter);

        List<PurchaseOrderLine> lines = new ArrayList<>();
        for (PurchaseOrder order : orders) {
            lines.addAll(purchaseOrderLineRepository.findByPurchaseOrderId(order.getId()));
        }
        Map<Long, Product> products = productRepository.findAllById(
                lines.stream().map(PurchaseOrderLine::getProductId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));

        Map<Long, BigDecimal[]> byBrand = new HashMap<>(); // key: brandId or 0 — [quantity, amount]
        for (PurchaseOrderLine line : lines) {
            Product product = products.get(line.getProductId());
            Long brandId = product == null ? null : product.getBrandId();
            BigDecimal[] bucket = byBrand.computeIfAbsent(brandId == null ? 0L : brandId, id -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            bucket[0] = bucket[0].add(line.getQuantityOrdered());
            bucket[1] = bucket[1].add(lineAmount(line.getQuantityOrdered(), line.getUnitCost(), line.getDiscountPercent(), line.getTaxRate()));
        }

        Map<Long, ProductBrand> brands = productBrandRepository.findAllById(
                byBrand.keySet().stream().filter(id -> id != 0L).toList()
        ).stream().collect(java.util.stream.Collectors.toMap(ProductBrand::getId, b -> b));

        List<PurchaseByBrandRowResponse> rows = new ArrayList<>();
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal[]> entry : byBrand.entrySet()) {
            ProductBrand brand = entry.getKey() == 0L ? null : brands.get(entry.getKey());
            rows.add(PurchaseByBrandRowResponse.builder()
                    .brandId(entry.getKey() == 0L ? null : entry.getKey())
                    .brandName(brand == null ? "Unbranded" : brand.getName())
                    .quantity(entry.getValue()[0])
                    .amount(entry.getValue()[1])
                    .build());
            totalQuantity = totalQuantity.add(entry.getValue()[0]);
            totalAmount = totalAmount.add(entry.getValue()[1]);
        }
        rows.sort(Comparator.comparing(PurchaseByBrandRowResponse::getAmount).reversed());

        return PurchaseByBrandResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalQuantity(totalQuantity)
                .totalAmount(totalAmount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseByUomResponse byUom(PurchaseReportFilterRequest filter) {
        List<PurchaseOrder> orders = matchingOrders(filter);

        Map<Long, BigDecimal[]> byUom = new HashMap<>(); // key: uomId or 0 — [quantity, amount]
        for (PurchaseOrder order : orders) {
            for (PurchaseOrderLine line : purchaseOrderLineRepository.findByPurchaseOrderId(order.getId())) {
                Long uomId = line.getUnitOfMeasureId();
                BigDecimal[] bucket = byUom.computeIfAbsent(uomId == null ? 0L : uomId, id -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                bucket[0] = bucket[0].add(line.getQuantityOrdered());
                bucket[1] = bucket[1].add(lineAmount(line.getQuantityOrdered(), line.getUnitCost(), line.getDiscountPercent(), line.getTaxRate()));
            }
        }

        Map<Long, UnitOfMeasure> uoms = unitOfMeasureRepository.findAllById(
                byUom.keySet().stream().filter(id -> id != 0L).toList()
        ).stream().collect(java.util.stream.Collectors.toMap(UnitOfMeasure::getId, u -> u));

        List<PurchaseByUomRowResponse> rows = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal[]> entry : byUom.entrySet()) {
            UnitOfMeasure uom = entry.getKey() == 0L ? null : uoms.get(entry.getKey());
            rows.add(PurchaseByUomRowResponse.builder()
                    .uomId(entry.getKey() == 0L ? null : entry.getKey())
                    .uomName(uom == null ? "Unspecified" : uom.getName())
                    .quantity(entry.getValue()[0])
                    .amount(entry.getValue()[1])
                    .build());
            totalAmount = totalAmount.add(entry.getValue()[1]);
        }
        rows.sort(Comparator.comparing(PurchaseByUomRowResponse::getAmount).reversed());

        return PurchaseByUomResponse.builder().dateFrom(filter.getDateFrom()).dateTo(filter.getDateTo()).rows(rows).totalAmount(totalAmount).build();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseBySupplierTypeResponse bySupplierType(PurchaseReportFilterRequest filter) {
        List<PurchaseOrder> orders = matchingOrders(filter);

        Map<Long, Supplier> suppliers = supplierRepository.findAllById(
                orders.stream().map(PurchaseOrder::getSupplierId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Supplier::getId, s -> s));

        Map<Long, long[]> countByType = new HashMap<>(); // key: supplierTypeId or 0
        Map<Long, BigDecimal> amountByType = new HashMap<>();
        for (PurchaseOrder order : orders) {
            Supplier supplier = suppliers.get(order.getSupplierId());
            Long typeId = supplier == null ? null : supplier.getSupplierTypeId();
            long key = typeId == null ? 0L : typeId;
            BigDecimal orderTotal = lineTotals(purchaseOrderLineRepository.findByPurchaseOrderId(order.getId()))[4];
            countByType.computeIfAbsent(key, id -> new long[]{0})[0]++;
            amountByType.merge(key, orderTotal, BigDecimal::add);
        }

        Map<Long, SupplierType> types = supplierTypeRepository.findAllById(
                countByType.keySet().stream().filter(id -> id != 0L).toList()
        ).stream().collect(java.util.stream.Collectors.toMap(SupplierType::getId, t -> t));

        List<PurchaseBySupplierTypeRowResponse> rows = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Long key : countByType.keySet()) {
            SupplierType type = key == 0L ? null : types.get(key);
            BigDecimal amount = amountByType.getOrDefault(key, BigDecimal.ZERO);
            rows.add(PurchaseBySupplierTypeRowResponse.builder()
                    .supplierTypeId(key == 0L ? null : key)
                    .supplierTypeName(type == null ? "Untyped" : type.getName())
                    .orderCount(countByType.get(key)[0])
                    .amount(amount)
                    .build());
            totalAmount = totalAmount.add(amount);
        }
        rows.sort(Comparator.comparing(PurchaseBySupplierTypeRowResponse::getAmount).reversed());

        return PurchaseBySupplierTypeResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalAmount(totalAmount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseByWarehouseResponse byWarehouse(PurchaseReportFilterRequest filter) {
        List<PurchaseOrder> orders = matchingOrders(filter);

        Map<Long, long[]> countByWarehouse = new HashMap<>();
        Map<Long, BigDecimal> amountByWarehouse = new HashMap<>();
        for (PurchaseOrder order : orders) {
            BigDecimal orderTotal = lineTotals(purchaseOrderLineRepository.findByPurchaseOrderId(order.getId()))[4];
            countByWarehouse.computeIfAbsent(order.getWarehouseId(), id -> new long[]{0})[0]++;
            amountByWarehouse.merge(order.getWarehouseId(), orderTotal, BigDecimal::add);
        }

        Map<Long, Warehouse> warehouses = warehouseRepository.findAllById(countByWarehouse.keySet()).stream()
                .collect(java.util.stream.Collectors.toMap(Warehouse::getId, w -> w));

        List<PurchaseByWarehouseRowResponse> rows = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Long warehouseId : countByWarehouse.keySet()) {
            Warehouse warehouse = warehouses.get(warehouseId);
            BigDecimal amount = amountByWarehouse.getOrDefault(warehouseId, BigDecimal.ZERO);
            rows.add(PurchaseByWarehouseRowResponse.builder()
                    .warehouseId(warehouseId)
                    .warehouseName(warehouse == null ? null : warehouse.getName())
                    .orderCount(countByWarehouse.get(warehouseId)[0])
                    .amount(amount)
                    .build());
            totalAmount = totalAmount.add(amount);
        }
        rows.sort(Comparator.comparing(PurchaseByWarehouseRowResponse::getAmount).reversed());

        return PurchaseByWarehouseResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalAmount(totalAmount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchasePendingOrdersResponse pendingOrders(PurchaseReportFilterRequest filter) {
        List<Specification<PurchaseOrder>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> root.get("status").in(List.of(PurchaseOrderStatus.DRAFT, PurchaseOrderStatus.SUBMITTED)));
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getDateFrom() != null) conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("orderDate"), filter.getDateFrom()));
        if (filter.getDateTo() != null) conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("orderDate"), filter.getDateTo()));
        List<PurchaseOrder> orders = purchaseOrderRepository.findAll(Specification.allOf(conditions));

        Map<Long, Supplier> suppliers = supplierRepository.findAllById(
                orders.stream().map(PurchaseOrder::getSupplierId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Supplier::getId, s -> s));

        List<PurchasePendingOrderRowResponse> rows = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrder order : orders) {
            Supplier supplier = suppliers.get(order.getSupplierId());
            BigDecimal amount = lineTotals(purchaseOrderLineRepository.findByPurchaseOrderId(order.getId()))[4];
            rows.add(PurchasePendingOrderRowResponse.builder()
                    .orderId(order.getId())
                    .poNumber(order.getPoNumber())
                    .orderDate(order.getOrderDate())
                    .status(order.getStatus().name())
                    .supplierId(order.getSupplierId())
                    .supplierName(supplier == null ? null : supplier.getName())
                    .amount(amount)
                    .build());
            totalAmount = totalAmount.add(amount);
        }
        rows.sort(Comparator.comparing(PurchasePendingOrderRowResponse::getOrderDate));

        return PurchasePendingOrdersResponse.builder().rows(rows).orderCount(rows.size()).totalAmount(totalAmount).build();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchasePendingGoodsReceiptsResponse pendingGoodsReceipts(PurchaseReportFilterRequest filter) {
        List<Specification<GoodsReceipt>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> cb.equal(root.get("status"), GoodsReceiptStatus.PENDING_QC));
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getDateFrom() != null) conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("receiptDate"), filter.getDateFrom()));
        if (filter.getDateTo() != null) conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("receiptDate"), filter.getDateTo()));
        List<GoodsReceipt> receipts = goodsReceiptRepository.findAll(Specification.allOf(conditions));

        Map<Long, PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAllById(
                receipts.stream().map(GoodsReceipt::getPurchaseOrderId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(PurchaseOrder::getId, o -> o));
        Map<Long, Supplier> suppliers = supplierRepository.findAllById(
                purchaseOrders.values().stream().map(PurchaseOrder::getSupplierId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Supplier::getId, s -> s));
        Map<Long, Warehouse> warehouses = warehouseRepository.findAllById(
                receipts.stream().map(GoodsReceipt::getWarehouseId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Warehouse::getId, w -> w));

        List<PurchasePendingGoodsReceiptRowResponse> rows = new ArrayList<>();
        for (GoodsReceipt receipt : receipts) {
            PurchaseOrder order = purchaseOrders.get(receipt.getPurchaseOrderId());
            Supplier supplier = order == null ? null : suppliers.get(order.getSupplierId());
            Warehouse warehouse = warehouses.get(receipt.getWarehouseId());
            rows.add(PurchasePendingGoodsReceiptRowResponse.builder()
                    .receiptId(receipt.getId())
                    .receiptNumber(receipt.getReceiptNumber())
                    .receiptDate(receipt.getReceiptDate())
                    .purchaseOrderId(receipt.getPurchaseOrderId())
                    .poNumber(order == null ? null : order.getPoNumber())
                    .supplierId(order == null ? null : order.getSupplierId())
                    .supplierName(supplier == null ? null : supplier.getName())
                    .warehouseId(receipt.getWarehouseId())
                    .warehouseName(warehouse == null ? null : warehouse.getName())
                    .lineCount(goodsReceiptLineRepository.findByGoodsReceiptId(receipt.getId()).size())
                    .build());
        }
        rows.sort(Comparator.comparing(PurchasePendingGoodsReceiptRowResponse::getReceiptDate));

        return PurchasePendingGoodsReceiptsResponse.builder().rows(rows).receiptCount(rows.size()).build();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOutstandingInvoicesResponse outstandingInvoices(PurchaseReportFilterRequest filter) {
        List<Specification<PurchaseInvoice>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> cb.equal(root.get("status"), PurchaseInvoiceStatus.APPROVED));
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getDateFrom() != null) conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("invoiceDate"), filter.getDateFrom()));
        if (filter.getDateTo() != null) conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("invoiceDate"), filter.getDateTo()));
        List<PurchaseInvoice> invoices = purchaseInvoiceRepository.findAll(Specification.allOf(conditions));

        Map<Long, Supplier> suppliers = supplierRepository.findAllById(
                invoices.stream().map(PurchaseInvoice::getSupplierId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Supplier::getId, s -> s));

        LocalDate today = LocalDate.now();
        List<PurchaseOutstandingInvoiceRowResponse> rows = new ArrayList<>();
        BigDecimal totalOutstanding = BigDecimal.ZERO;
        for (PurchaseInvoice invoice : invoices) {
            BigDecimal totalAmount = invoiceLineTotal(purchaseInvoiceLineRepository.findByPurchaseInvoiceId(invoice.getId()));
            BigDecimal credited = purchaseCreditNoteRepository.findByPurchaseInvoiceId(invoice.getId()).stream()
                    .map(PurchaseCreditNote::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal paid = supplierPaymentAllocationRepository.findByPurchaseInvoiceId(invoice.getId()).stream()
                    .map(SupplierPaymentAllocation::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal outstandingAmount = totalAmount.subtract(credited).subtract(paid);
            if (outstandingAmount.signum() <= 0) continue;

            Supplier supplier = suppliers.get(invoice.getSupplierId());
            rows.add(PurchaseOutstandingInvoiceRowResponse.builder()
                    .invoiceId(invoice.getId())
                    .invoiceNumber(invoice.getInvoiceNumber())
                    .invoiceDate(invoice.getInvoiceDate())
                    .dueDate(invoice.getDueDate())
                    .daysOverdue(invoice.getDueDate() == null ? 0 : ChronoUnit.DAYS.between(invoice.getDueDate(), today))
                    .supplierId(invoice.getSupplierId())
                    .supplierName(supplier == null ? null : supplier.getName())
                    .totalAmount(totalAmount)
                    .outstandingAmount(outstandingAmount)
                    .build());
            totalOutstanding = totalOutstanding.add(outstandingAmount);
        }
        rows.sort(Comparator.comparing(PurchaseOutstandingInvoiceRowResponse::getDaysOverdue).reversed());

        return PurchaseOutstandingInvoicesResponse.builder().rows(rows).invoiceCount(rows.size()).totalOutstanding(totalOutstanding).build();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseCancellationResponse cancellations(PurchaseReportFilterRequest filter) {
        List<Specification<PurchaseOrder>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> cb.equal(root.get("status"), PurchaseOrderStatus.CANCELLED));
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getDateFrom() != null) conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("orderDate"), filter.getDateFrom()));
        if (filter.getDateTo() != null) conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("orderDate"), filter.getDateTo()));
        List<PurchaseOrder> orders = purchaseOrderRepository.findAll(Specification.allOf(conditions));

        Map<Long, Supplier> suppliers = supplierRepository.findAllById(
                orders.stream().map(PurchaseOrder::getSupplierId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Supplier::getId, s -> s));

        List<PurchaseCancellationRowResponse> rows = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrder order : orders) {
            Supplier supplier = suppliers.get(order.getSupplierId());
            BigDecimal amount = lineTotals(purchaseOrderLineRepository.findByPurchaseOrderId(order.getId()))[4];
            rows.add(PurchaseCancellationRowResponse.builder()
                    .orderId(order.getId())
                    .poNumber(order.getPoNumber())
                    .orderDate(order.getOrderDate())
                    .supplierId(order.getSupplierId())
                    .supplierName(supplier == null ? null : supplier.getName())
                    .amount(amount)
                    .build());
            totalAmount = totalAmount.add(amount);
        }
        rows.sort(Comparator.comparing(PurchaseCancellationRowResponse::getOrderDate).reversed());

        return PurchaseCancellationResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .orderCount(rows.size())
                .totalAmount(totalAmount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseDiscountResponse discounts(PurchaseReportFilterRequest filter) {
        List<PurchaseOrder> orders = matchingOrders(filter);

        Map<Long, Supplier> suppliers = supplierRepository.findAllById(
                orders.stream().map(PurchaseOrder::getSupplierId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Supplier::getId, s -> s));

        List<PurchaseDiscountRowResponse> rows = new ArrayList<>();
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;
        for (PurchaseOrder order : orders) {
            Supplier supplier = suppliers.get(order.getSupplierId());
            for (PurchaseOrderLine line : purchaseOrderLineRepository.findByPurchaseOrderId(order.getId())) {
                if (line.getDiscountPercent() == null || line.getDiscountPercent().signum() <= 0) continue;
                Product product = productRepository.findById(line.getProductId()).orElse(null);
                BigDecimal lineSubtotal = line.getQuantityOrdered().multiply(line.getUnitCost());
                BigDecimal discountAmount = lineSubtotal.multiply(line.getDiscountPercent()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
                rows.add(PurchaseDiscountRowResponse.builder()
                        .orderId(order.getId())
                        .poNumber(order.getPoNumber())
                        .orderDate(order.getOrderDate())
                        .supplierId(order.getSupplierId())
                        .supplierName(supplier == null ? null : supplier.getName())
                        .productId(line.getProductId())
                        .productName(product == null ? null : product.getName())
                        .productSku(product == null ? null : product.getSku())
                        .quantity(line.getQuantityOrdered())
                        .unitCost(line.getUnitCost())
                        .discountPercent(line.getDiscountPercent())
                        .discountAmount(discountAmount)
                        .build());
                totalDiscountAmount = totalDiscountAmount.add(discountAmount);
            }
        }
        rows.sort(Comparator.comparing(PurchaseDiscountRowResponse::getDiscountAmount).reversed());

        return PurchaseDiscountResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalDiscountAmount(totalDiscountAmount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseByPeriodResponse monthly(PurchaseReportFilterRequest filter) {
        List<PurchaseOrder> orders = matchingOrders(filter);

        Map<YearMonth, long[]> countByMonth = new HashMap<>();
        Map<YearMonth, BigDecimal> amountByMonth = new HashMap<>();
        for (PurchaseOrder order : orders) {
            YearMonth month = YearMonth.from(order.getOrderDate());
            BigDecimal orderTotal = lineTotals(purchaseOrderLineRepository.findByPurchaseOrderId(order.getId()))[4];
            countByMonth.computeIfAbsent(month, m -> new long[]{0})[0]++;
            amountByMonth.merge(month, orderTotal, BigDecimal::add);
        }

        List<PurchaseByPeriodRowResponse> rows = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (YearMonth month : countByMonth.keySet()) {
            BigDecimal amount = amountByMonth.getOrDefault(month, BigDecimal.ZERO);
            rows.add(PurchaseByPeriodRowResponse.builder().period(month.toString()).orderCount(countByMonth.get(month)[0]).amount(amount).build());
            totalAmount = totalAmount.add(amount);
        }
        rows.sort(Comparator.comparing(PurchaseByPeriodRowResponse::getPeriod));

        return PurchaseByPeriodResponse.builder().dateFrom(filter.getDateFrom()).dateTo(filter.getDateTo()).rows(rows).totalAmount(totalAmount).build();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseByPeriodResponse yearly(PurchaseReportFilterRequest filter) {
        List<PurchaseOrder> orders = matchingOrders(filter);

        Map<Year, long[]> countByYear = new HashMap<>();
        Map<Year, BigDecimal> amountByYear = new HashMap<>();
        for (PurchaseOrder order : orders) {
            Year year = Year.from(order.getOrderDate());
            BigDecimal orderTotal = lineTotals(purchaseOrderLineRepository.findByPurchaseOrderId(order.getId()))[4];
            countByYear.computeIfAbsent(year, y -> new long[]{0})[0]++;
            amountByYear.merge(year, orderTotal, BigDecimal::add);
        }

        List<PurchaseByPeriodRowResponse> rows = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Year year : countByYear.keySet()) {
            BigDecimal amount = amountByYear.getOrDefault(year, BigDecimal.ZERO);
            rows.add(PurchaseByPeriodRowResponse.builder().period(year.toString()).orderCount(countByYear.get(year)[0]).amount(amount).build());
            totalAmount = totalAmount.add(amount);
        }
        rows.sort(Comparator.comparing(PurchaseByPeriodRowResponse::getPeriod));

        return PurchaseByPeriodResponse.builder().dateFrom(filter.getDateFrom()).dateTo(filter.getDateTo()).rows(rows).totalAmount(totalAmount).build();
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierPriceHistoryResponse supplierPriceHistory(SupplierPriceHistoryFilterRequest filter) {
        Supplier supplier = filter.getSupplierId() == null ? null : supplierRepository.findById(filter.getSupplierId()).orElse(null);

        List<SupplierPriceHistoryRowResponse> rows = new ArrayList<>();
        if (filter.getSupplierId() != null) {
            List<Specification<PurchaseOrder>> conditions = new ArrayList<>();
            conditions.add((root, query, cb) -> cb.equal(root.get("supplierId"), filter.getSupplierId()));
            conditions.add((root, query, cb) -> cb.notEqual(root.get("status"), PurchaseOrderStatus.CANCELLED));
            if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
            if (filter.getDateFrom() != null) conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("orderDate"), filter.getDateFrom()));
            if (filter.getDateTo() != null) conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("orderDate"), filter.getDateTo()));
            List<PurchaseOrder> orders = purchaseOrderRepository.findAll(Specification.allOf(conditions));
            orders.sort(Comparator.comparing(PurchaseOrder::getOrderDate));

            Map<Long, Product> products = new HashMap<>();
            for (PurchaseOrder order : orders) {
                for (PurchaseOrderLine line : purchaseOrderLineRepository.findByPurchaseOrderId(order.getId())) {
                    Product product = products.computeIfAbsent(line.getProductId(), id -> productRepository.findById(id).orElse(null));
                    rows.add(SupplierPriceHistoryRowResponse.builder()
                            .orderDate(order.getOrderDate())
                            .poNumber(order.getPoNumber())
                            .productId(line.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productSku(product == null ? null : product.getSku())
                            .quantity(line.getQuantityOrdered())
                            .unitCost(line.getUnitCost())
                            .build());
                }
            }
        }

        return SupplierPriceHistoryResponse.builder()
                .supplierId(filter.getSupplierId())
                .supplierName(supplier == null ? null : supplier.getName())
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPurchasePriceHistoryResponse productPriceHistory(ProductPurchasePriceHistoryFilterRequest filter) {
        Product product = filter.getProductId() == null ? null : productRepository.findById(filter.getProductId()).orElse(null);

        List<ProductPurchasePriceHistoryRowResponse> rows = new ArrayList<>();
        if (filter.getProductId() != null) {
            List<Specification<PurchaseOrder>> conditions = new ArrayList<>();
            conditions.add((root, query, cb) -> cb.notEqual(root.get("status"), PurchaseOrderStatus.CANCELLED));
            if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
            if (filter.getDateFrom() != null) conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("orderDate"), filter.getDateFrom()));
            if (filter.getDateTo() != null) conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("orderDate"), filter.getDateTo()));
            List<PurchaseOrder> orders = purchaseOrderRepository.findAll(Specification.allOf(conditions));
            orders.sort(Comparator.comparing(PurchaseOrder::getOrderDate));

            Map<Long, Supplier> suppliers = new HashMap<>();
            for (PurchaseOrder order : orders) {
                for (PurchaseOrderLine line : purchaseOrderLineRepository.findByPurchaseOrderId(order.getId())) {
                    if (!line.getProductId().equals(filter.getProductId())) continue;
                    Supplier supplier = suppliers.computeIfAbsent(order.getSupplierId(), id -> supplierRepository.findById(id).orElse(null));
                    rows.add(ProductPurchasePriceHistoryRowResponse.builder()
                            .orderDate(order.getOrderDate())
                            .poNumber(order.getPoNumber())
                            .supplierId(order.getSupplierId())
                            .supplierName(supplier == null ? null : supplier.getName())
                            .quantity(line.getQuantityOrdered())
                            .unitCost(line.getUnitCost())
                            .build());
                }
            }
        }

        return ProductPurchasePriceHistoryResponse.builder()
                .productId(filter.getProductId())
                .productName(product == null ? null : product.getName())
                .productSku(product == null ? null : product.getSku())
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierPerformanceResponse supplierPerformance(PurchaseReportFilterRequest filter) {
        List<Specification<PurchaseOrder>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> cb.notEqual(root.get("status"), PurchaseOrderStatus.CANCELLED));
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getDateFrom() != null) conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("orderDate"), filter.getDateFrom()));
        if (filter.getDateTo() != null) conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("orderDate"), filter.getDateTo()));
        List<PurchaseOrder> orders = purchaseOrderRepository.findAll(Specification.allOf(conditions));

        Map<Long, Supplier> suppliers = supplierRepository.findAllById(
                orders.stream().map(PurchaseOrder::getSupplierId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Supplier::getId, s -> s));

        Map<Long, long[]> orderCountBySupplier = new HashMap<>(); // [orderCount, measurableCount, onTimeCount]
        Map<Long, Long> delaySumBySupplier = new HashMap<>();
        for (PurchaseOrder order : orders) {
            long[] counts = orderCountBySupplier.computeIfAbsent(order.getSupplierId(), id -> new long[]{0, 0, 0});
            counts[0]++;

            if (order.getExpectedDate() == null) continue;
            List<GoodsReceipt> receipts = goodsReceiptRepository.findAll(
                    (root, query, cb) -> cb.equal(root.get("purchaseOrderId"), order.getId()));
            if (receipts.isEmpty()) continue;
            LocalDate latestReceiptDate = receipts.stream().map(GoodsReceipt::getReceiptDate).max(LocalDate::compareTo).orElseThrow();

            counts[1]++;
            long delayDays = ChronoUnit.DAYS.between(order.getExpectedDate(), latestReceiptDate);
            if (delayDays <= 0) counts[2]++;
            delaySumBySupplier.merge(order.getSupplierId(), delayDays, Long::sum);
        }

        List<SupplierPerformanceRowResponse> rows = new ArrayList<>();
        for (Map.Entry<Long, long[]> entry : orderCountBySupplier.entrySet()) {
            Supplier supplier = suppliers.get(entry.getKey());
            long[] counts = entry.getValue();
            Double onTimePercent = counts[1] == 0 ? null : (counts[2] * 100.0) / counts[1];
            Double avgDelay = counts[1] == 0 ? null : delaySumBySupplier.getOrDefault(entry.getKey(), 0L) / (double) counts[1];
            rows.add(SupplierPerformanceRowResponse.builder()
                    .supplierId(entry.getKey())
                    .supplierName(supplier == null ? null : supplier.getName())
                    .orderCount(counts[0])
                    .measurableOrderCount(counts[1])
                    .onTimeOrderCount(counts[2])
                    .onTimePercent(onTimePercent)
                    .averageDelayDays(avgDelay)
                    .build());
        }
        rows.sort(Comparator.comparing((SupplierPerformanceRowResponse r) -> r.getOnTimePercent() == null ? -1.0 : r.getOnTimePercent()).reversed());

        return SupplierPerformanceResponse.builder().dateFrom(filter.getDateFrom()).dateTo(filter.getDateTo()).rows(rows).build();
    }

    // Excludes CANCELLED unless the caller explicitly filters to it — a
    // cancelled order was never really a purchase.
    private List<PurchaseOrder> matchingOrders(PurchaseReportFilterRequest filter) {
        List<Specification<PurchaseOrder>> conditions = new ArrayList<>();
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        } else {
            conditions.add((root, query, cb) -> cb.notEqual(root.get("status"), PurchaseOrderStatus.CANCELLED));
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
        return purchaseOrderRepository.findAll(Specification.allOf(conditions));
    }

    // Returns [quantity, subtotal, discountAmount, taxAmount, totalAmount].
    private BigDecimal[] lineTotals(List<PurchaseOrderLine> lines) {
        BigDecimal quantity = BigDecimal.ZERO;
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        for (PurchaseOrderLine line : lines) {
            BigDecimal lineSubtotal = line.getQuantityOrdered().multiply(line.getUnitCost());
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

    private BigDecimal lineAmount(BigDecimal quantity, BigDecimal unitCost, BigDecimal discountPercent, BigDecimal taxRate) {
        BigDecimal lineSubtotal = quantity.multiply(unitCost);
        BigDecimal discount = lineSubtotal.multiply(discountPercent).divide(HUNDRED, 4, RoundingMode.HALF_UP);
        BigDecimal afterDiscount = lineSubtotal.subtract(discount);
        BigDecimal tax = afterDiscount.multiply(taxRate).divide(HUNDRED, 4, RoundingMode.HALF_UP);
        return afterDiscount.add(tax);
    }

    private BigDecimal invoiceLineTotal(List<PurchaseInvoiceLine> lines) {
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseInvoiceLine line : lines) {
            total = total.add(lineAmount(line.getQuantity(), line.getUnitCost(), line.getDiscountPercent(), line.getTaxRate()));
        }
        return total;
    }
}
