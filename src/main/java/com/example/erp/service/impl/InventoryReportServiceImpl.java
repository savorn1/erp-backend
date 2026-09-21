package com.example.erp.service.impl;

import com.example.erp.dto.BatchLotFilterRequest;
import com.example.erp.dto.BatchLotStockResponse;
import com.example.erp.dto.BatchLotStockRowResponse;
import com.example.erp.dto.InventoryOverviewFilterRequest;
import com.example.erp.dto.InventoryOverviewResponse;
import com.example.erp.dto.LowStockResponse;
import com.example.erp.dto.PartyStockFilterRequest;
import com.example.erp.dto.StockAgingBucketResponse;
import com.example.erp.dto.StockAgingResponse;
import com.example.erp.dto.StockAgingRowResponse;
import com.example.erp.dto.StockByPartyResponse;
import com.example.erp.dto.StockByPartyRowResponse;
import com.example.erp.dto.StockCountVarianceFilterRequest;
import com.example.erp.dto.StockCountVarianceResponse;
import com.example.erp.dto.StockCountVarianceRowResponse;
import com.example.erp.dto.StockDetailResponse;
import com.example.erp.dto.StockDetailRowResponse;
import com.example.erp.dto.StockInOutResponse;
import com.example.erp.dto.StockInOutRowResponse;
import com.example.erp.dto.StockLedgerFilterRequest;
import com.example.erp.dto.StockLedgerResponse;
import com.example.erp.dto.StockLedgerRowResponse;
import com.example.erp.dto.StockMovementReportFilterRequest;
import com.example.erp.dto.StockOpeningClosingResponse;
import com.example.erp.dto.StockOpeningClosingRowResponse;
import com.example.erp.dto.StockProfitabilityResponse;
import com.example.erp.dto.StockProfitabilityRowResponse;
import com.example.erp.dto.StockThresholdResponse;
import com.example.erp.dto.StockTurnoverFilterRequest;
import com.example.erp.dto.StockTurnoverResponse;
import com.example.erp.dto.StockTurnoverRowResponse;
import com.example.erp.dto.StockValuationResponse;
import com.example.erp.dto.StockValuationRowResponse;
import com.example.erp.entity.Customer;
import com.example.erp.entity.Delivery;
import com.example.erp.entity.DeliveryLine;
import com.example.erp.entity.GoodsReceiptLine;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductBatch;
import com.example.erp.entity.PurchaseOrder;
import com.example.erp.entity.PurchaseOrderLine;
import com.example.erp.entity.PurchaseOrderStatus;
import com.example.erp.entity.QualityCheckStatus;
import com.example.erp.entity.SalesOrder;
import com.example.erp.entity.SalesOrderLine;
import com.example.erp.entity.SalesOrderStatus;
import com.example.erp.entity.StockAdjustment;
import com.example.erp.entity.StockAdjustmentLine;
import com.example.erp.entity.StockAdjustmentReason;
import com.example.erp.entity.StockAdjustmentStatus;
import com.example.erp.entity.StockCount;
import com.example.erp.entity.StockCountLine;
import com.example.erp.entity.StockLevel;
import com.example.erp.entity.StockMovement;
import com.example.erp.entity.StockMovementType;
import com.example.erp.entity.Supplier;
import com.example.erp.entity.Warehouse;
import com.example.erp.entity.WarehouseBin;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.DeliveryLineRepository;
import com.example.erp.repository.DeliveryRepository;
import com.example.erp.repository.GoodsReceiptLineRepository;
import com.example.erp.repository.ProductBatchRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.PurchaseOrderLineRepository;
import com.example.erp.repository.PurchaseOrderRepository;
import com.example.erp.repository.SalesOrderLineRepository;
import com.example.erp.repository.SalesOrderRepository;
import com.example.erp.repository.StockAdjustmentLineRepository;
import com.example.erp.repository.StockAdjustmentRepository;
import com.example.erp.repository.StockCountLineRepository;
import com.example.erp.repository.StockCountRepository;
import com.example.erp.repository.StockLevelRepository;
import com.example.erp.repository.StockMovementRepository;
import com.example.erp.repository.SupplierRepository;
import com.example.erp.repository.WarehouseBinRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.service.InventoryOverviewService;
import com.example.erp.service.InventoryReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryReportServiceImpl implements InventoryReportService {

    private final InventoryOverviewService inventoryOverviewService;
    private final StockLevelRepository stockLevelRepository;
    private final WarehouseBinRepository warehouseBinRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductBatchRepository productBatchRepository;
    private final GoodsReceiptLineRepository goodsReceiptLineRepository;
    private final DeliveryLineRepository deliveryLineRepository;
    private final DeliveryRepository deliveryRepository;
    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final StockAdjustmentLineRepository stockAdjustmentLineRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final CustomerRepository customerRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final SupplierRepository supplierRepository;
    private final StockCountRepository stockCountRepository;
    private final StockCountLineRepository stockCountLineRepository;

    private record Key(Long productId, Long warehouseId) {
    }

    @Override
    @Transactional(readOnly = true)
    public StockValuationResponse stockValuation(InventoryOverviewFilterRequest filter) {
        List<InventoryOverviewResponse> rows = fetchAll(filter);

        Map<Long, StockValuationRowResponse> byWarehouse = new LinkedHashMap<>();
        Map<Long, Integer> productCounts = new LinkedHashMap<>();
        for (InventoryOverviewResponse row : rows) {
            StockValuationRowResponse bucket = byWarehouse.computeIfAbsent(row.getWarehouseId(), id -> StockValuationRowResponse.builder()
                    .warehouseId(row.getWarehouseId())
                    .warehouseName(row.getWarehouseName())
                    .totalQuantity(BigDecimal.ZERO)
                    .totalValue(BigDecimal.ZERO)
                    .build());
            bucket.setTotalQuantity(bucket.getTotalQuantity().add(row.getCurrentStock()));
            bucket.setTotalValue(bucket.getTotalValue().add(row.getValuationValue()));
            productCounts.merge(row.getWarehouseId(), 1, Integer::sum);
        }
        byWarehouse.values().forEach(bucket -> bucket.setProductCount(productCounts.getOrDefault(bucket.getWarehouseId(), 0)));

        List<StockValuationRowResponse> sortedRows = new ArrayList<>(byWarehouse.values());
        sortedRows.sort(Comparator.comparing(StockValuationRowResponse::getTotalValue).reversed());

        BigDecimal totalQuantity = sortedRows.stream().map(StockValuationRowResponse::getTotalQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalValue = sortedRows.stream().map(StockValuationRowResponse::getTotalValue).reduce(BigDecimal.ZERO, BigDecimal::add);

        return StockValuationResponse.builder()
                .rows(sortedRows)
                .totalQuantity(totalQuantity)
                .totalValue(totalValue)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LowStockResponse lowStock(InventoryOverviewFilterRequest filter) {
        List<InventoryOverviewResponse> rows = fetchAll(filter).stream()
                .filter(row -> row.getReorderPoint() != null && row.getReorderPoint().compareTo(BigDecimal.ZERO) > 0)
                .filter(row -> row.getAvailableStock().compareTo(row.getReorderPoint()) < 0)
                .sorted(Comparator.comparing((InventoryOverviewResponse row) -> row.getAvailableStock().subtract(row.getReorderPoint())))
                .toList();

        return LowStockResponse.builder()
                .rows(rows)
                .count(rows.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StockThresholdResponse outOfStock(InventoryOverviewFilterRequest filter) {
        List<InventoryOverviewResponse> rows = fetchAll(filter).stream()
                .filter(row -> row.getCurrentStock().compareTo(BigDecimal.ZERO) <= 0)
                .sorted(Comparator.comparing(row -> row.getProductName() == null ? "" : row.getProductName()))
                .toList();
        return StockThresholdResponse.builder().rows(rows).count(rows.size()).build();
    }

    @Override
    @Transactional(readOnly = true)
    public StockThresholdResponse overstock(InventoryOverviewFilterRequest filter) {
        List<InventoryOverviewResponse> rows = fetchAll(filter).stream()
                .filter(row -> row.getMaxStock() != null && row.getMaxStock().compareTo(BigDecimal.ZERO) > 0)
                .filter(row -> row.getCurrentStock().compareTo(row.getMaxStock()) > 0)
                .sorted(Comparator.comparing((InventoryOverviewResponse row) -> row.getCurrentStock().subtract(row.getMaxStock())).reversed())
                .toList();
        return StockThresholdResponse.builder().rows(rows).count(rows.size()).build();
    }

    @Override
    @Transactional(readOnly = true)
    public StockThresholdResponse negativeStock(InventoryOverviewFilterRequest filter) {
        List<InventoryOverviewResponse> rows = fetchAll(filter).stream()
                .filter(row -> row.getCurrentStock().compareTo(BigDecimal.ZERO) < 0)
                .sorted(Comparator.comparing(InventoryOverviewResponse::getCurrentStock))
                .toList();
        return StockThresholdResponse.builder().rows(rows).count(rows.size()).build();
    }

    @Override
    @Transactional(readOnly = true)
    public StockDetailResponse stockDetail(InventoryOverviewFilterRequest filter) {
        List<StockLevel> levels = stockLevelRepository.findAll().stream()
                .filter(s -> filter.getCompanyId() == null || filter.getCompanyId().equals(s.getCompanyId()))
                .filter(s -> filter.getWarehouseId() == null || filter.getWarehouseId().equals(s.getWarehouseId()))
                .filter(s -> filter.getProductId() == null || filter.getProductId().equals(s.getProductId()))
                .toList();

        Map<Long, Product> productsById = productRepository.findAllById(levels.stream().map(StockLevel::getProductId).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));
        Map<Long, Warehouse> warehousesById = warehouseRepository.findAllById(levels.stream().map(StockLevel::getWarehouseId).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(Warehouse::getId, w -> w));
        List<Long> binIds = levels.stream().map(StockLevel::getBinId).filter(java.util.Objects::nonNull).distinct().toList();
        Map<Long, WarehouseBin> binsById = warehouseBinRepository.findAllById(binIds)
                .stream().collect(java.util.stream.Collectors.toMap(WarehouseBin::getId, b -> b));

        String search = filter.getSearch() == null ? null : filter.getSearch().toLowerCase();
        List<StockDetailRowResponse> rows = new ArrayList<>();
        for (StockLevel level : levels) {
            Product product = productsById.get(level.getProductId());
            Warehouse warehouse = warehousesById.get(level.getWarehouseId());
            if (product == null || warehouse == null) continue;
            if (search != null && !search.isBlank()
                    && !product.getName().toLowerCase().contains(search)
                    && !(product.getSku() != null && product.getSku().toLowerCase().contains(search))) {
                continue;
            }
            WarehouseBin bin = level.getBinId() == null ? null : binsById.get(level.getBinId());
            rows.add(StockDetailRowResponse.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .warehouseId(warehouse.getId())
                    .warehouseName(warehouse.getName())
                    .binId(level.getBinId())
                    .binName(bin == null ? null : bin.getName())
                    .quantityOnHand(level.getQuantityOnHand())
                    .build());
        }
        rows.sort(Comparator.comparing((StockDetailRowResponse r) -> r.getProductName() == null ? "" : r.getProductName())
                .thenComparing(r -> r.getWarehouseName() == null ? "" : r.getWarehouseName()));
        return StockDetailResponse.builder().rows(rows).build();
    }

    @Override
    @Transactional(readOnly = true)
    public StockLedgerResponse stockLedger(StockLedgerFilterRequest filter) {
        LocalDateTime fromDateTime = filter.getDateFrom() == null ? null : filter.getDateFrom().atStartOfDay();
        LocalDateTime toDateTime = filter.getDateTo() == null ? null : filter.getDateTo().plusDays(1).atStartOfDay();

        List<StockMovement> all = stockMovementRepository.findAll().stream()
                .filter(m -> filter.getCompanyId() == null || filter.getCompanyId().equals(m.getCompanyId()))
                .filter(m -> filter.getWarehouseId() == null || filter.getWarehouseId().equals(m.getWarehouseId()))
                .filter(m -> filter.getProductId() == null || filter.getProductId().equals(m.getProductId()))
                .sorted(Comparator.comparing(StockMovement::getCreatedAt).thenComparing(StockMovement::getId))
                .toList();

        Map<Key, BigDecimal> runningBalance = new LinkedHashMap<>();
        BigDecimal openingBalance = BigDecimal.ZERO;
        for (StockMovement m : all) {
            if (fromDateTime != null && m.getCreatedAt().isBefore(fromDateTime)) {
                Key key = new Key(m.getProductId(), m.getWarehouseId());
                runningBalance.merge(key, m.getQuantityDelta(), BigDecimal::add);
                openingBalance = openingBalance.add(m.getQuantityDelta());
            }
        }

        Map<Long, Product> productsById = productRepository.findAllById(all.stream().map(StockMovement::getProductId).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));
        Map<Long, Warehouse> warehousesById = warehouseRepository.findAllById(all.stream().map(StockMovement::getWarehouseId).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(Warehouse::getId, w -> w));

        List<StockLedgerRowResponse> rows = new ArrayList<>();
        BigDecimal closingBalance = openingBalance;
        for (StockMovement m : all) {
            boolean afterFrom = fromDateTime == null || !m.getCreatedAt().isBefore(fromDateTime);
            boolean beforeTo = toDateTime == null || m.getCreatedAt().isBefore(toDateTime);
            if (!afterFrom || !beforeTo) continue;

            Key key = new Key(m.getProductId(), m.getWarehouseId());
            BigDecimal newBalance = runningBalance.merge(key, m.getQuantityDelta(), BigDecimal::add);
            closingBalance = closingBalance.add(m.getQuantityDelta());

            Product product = productsById.get(m.getProductId());
            Warehouse warehouse = warehousesById.get(m.getWarehouseId());
            rows.add(StockLedgerRowResponse.builder()
                    .date(m.getCreatedAt())
                    .productId(m.getProductId())
                    .productName(product == null ? null : product.getName())
                    .productSku(product == null ? null : product.getSku())
                    .warehouseId(m.getWarehouseId())
                    .warehouseName(warehouse == null ? null : warehouse.getName())
                    .type(m.getType().name())
                    .referenceType(m.getReferenceType())
                    .referenceId(m.getReferenceId())
                    .quantityDelta(m.getQuantityDelta())
                    .balance(newBalance)
                    .build());
        }

        return StockLedgerResponse.builder().rows(rows).openingBalance(openingBalance).closingBalance(closingBalance).build();
    }

    private static final java.util.Set<StockMovementType> INBOUND_TYPES =
            java.util.Set.of(StockMovementType.RECEIPT, StockMovementType.TRANSFER_IN, StockMovementType.PRODUCTION_OUTPUT);
    private static final java.util.Set<StockMovementType> OUTBOUND_TYPES =
            java.util.Set.of(StockMovementType.ISSUE, StockMovementType.TRANSFER_OUT, StockMovementType.MATERIAL_CONSUMPTION);

    @Override
    @Transactional(readOnly = true)
    public StockInOutResponse stockIn(StockMovementReportFilterRequest filter) {
        return movementDirectionReport(filter, INBOUND_TYPES);
    }

    @Override
    @Transactional(readOnly = true)
    public StockInOutResponse stockOut(StockMovementReportFilterRequest filter) {
        return movementDirectionReport(filter, OUTBOUND_TYPES);
    }

    private StockInOutResponse movementDirectionReport(StockMovementReportFilterRequest filter, java.util.Set<StockMovementType> types) {
        LocalDateTime fromDateTime = filter.getDateFrom() == null ? null : filter.getDateFrom().atStartOfDay();
        LocalDateTime toDateTime = filter.getDateTo() == null ? null : filter.getDateTo().plusDays(1).atStartOfDay();

        List<StockMovement> matched = stockMovementRepository.findAll().stream()
                .filter(m -> types.contains(m.getType()))
                .filter(m -> filter.getCompanyId() == null || filter.getCompanyId().equals(m.getCompanyId()))
                .filter(m -> filter.getWarehouseId() == null || filter.getWarehouseId().equals(m.getWarehouseId()))
                .filter(m -> filter.getProductId() == null || filter.getProductId().equals(m.getProductId()))
                .filter(m -> fromDateTime == null || !m.getCreatedAt().isBefore(fromDateTime))
                .filter(m -> toDateTime == null || m.getCreatedAt().isBefore(toDateTime))
                .toList();

        Map<Key, BigDecimal> quantityByKey = new LinkedHashMap<>();
        Map<Key, Integer> countByKey = new LinkedHashMap<>();
        for (StockMovement m : matched) {
            Key key = new Key(m.getProductId(), m.getWarehouseId());
            quantityByKey.merge(key, m.getQuantityDelta().abs(), BigDecimal::add);
            countByKey.merge(key, 1, Integer::sum);
        }

        Map<Long, Product> productsById = productRepository.findAllById(matched.stream().map(StockMovement::getProductId).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));
        Map<Long, Warehouse> warehousesById = warehouseRepository.findAllById(matched.stream().map(StockMovement::getWarehouseId).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(Warehouse::getId, w -> w));

        List<StockInOutRowResponse> rows = new ArrayList<>();
        for (Map.Entry<Key, BigDecimal> entry : quantityByKey.entrySet()) {
            Product product = productsById.get(entry.getKey().productId());
            Warehouse warehouse = warehousesById.get(entry.getKey().warehouseId());
            rows.add(StockInOutRowResponse.builder()
                    .productId(entry.getKey().productId())
                    .productName(product == null ? null : product.getName())
                    .productSku(product == null ? null : product.getSku())
                    .warehouseId(entry.getKey().warehouseId())
                    .warehouseName(warehouse == null ? null : warehouse.getName())
                    .quantity(entry.getValue())
                    .movementCount(countByKey.getOrDefault(entry.getKey(), 0))
                    .build());
        }
        rows.sort(Comparator.comparing(StockInOutRowResponse::getQuantity).reversed());

        BigDecimal total = rows.stream().map(StockInOutRowResponse::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        return StockInOutResponse.builder().rows(rows).totalQuantity(total).build();
    }

    @Override
    @Transactional(readOnly = true)
    public StockOpeningClosingResponse openingClosingStock(StockMovementReportFilterRequest filter) {
        LocalDateTime fromDateTime = filter.getDateFrom() == null ? null : filter.getDateFrom().atStartOfDay();
        LocalDateTime toDateTime = filter.getDateTo() == null ? null : filter.getDateTo().plusDays(1).atStartOfDay();

        List<StockMovement> matched = stockMovementRepository.findAll().stream()
                .filter(m -> filter.getCompanyId() == null || filter.getCompanyId().equals(m.getCompanyId()))
                .filter(m -> filter.getWarehouseId() == null || filter.getWarehouseId().equals(m.getWarehouseId()))
                .filter(m -> filter.getProductId() == null || filter.getProductId().equals(m.getProductId()))
                .filter(m -> toDateTime == null || m.getCreatedAt().isBefore(toDateTime))
                .toList();

        Map<Key, BigDecimal> opening = new LinkedHashMap<>();
        Map<Key, BigDecimal> closing = new LinkedHashMap<>();
        for (StockMovement m : matched) {
            Key key = new Key(m.getProductId(), m.getWarehouseId());
            closing.merge(key, m.getQuantityDelta(), BigDecimal::add);
            if (fromDateTime == null || m.getCreatedAt().isBefore(fromDateTime)) {
                opening.merge(key, m.getQuantityDelta(), BigDecimal::add);
            }
        }

        Map<Long, Product> productsById = productRepository.findAllById(matched.stream().map(StockMovement::getProductId).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));
        Map<Long, Warehouse> warehousesById = warehouseRepository.findAllById(matched.stream().map(StockMovement::getWarehouseId).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(Warehouse::getId, w -> w));

        List<StockOpeningClosingRowResponse> rows = new ArrayList<>();
        for (Key key : closing.keySet()) {
            Product product = productsById.get(key.productId());
            Warehouse warehouse = warehousesById.get(key.warehouseId());
            rows.add(StockOpeningClosingRowResponse.builder()
                    .productId(key.productId())
                    .productName(product == null ? null : product.getName())
                    .productSku(product == null ? null : product.getSku())
                    .warehouseId(key.warehouseId())
                    .warehouseName(warehouse == null ? null : warehouse.getName())
                    .openingQuantity(opening.getOrDefault(key, BigDecimal.ZERO))
                    .closingQuantity(closing.get(key))
                    .build());
        }
        rows.sort(Comparator.comparing(r -> r.getProductName() == null ? "" : r.getProductName()));
        return StockOpeningClosingResponse.builder().rows(rows).build();
    }

    @Override
    @Transactional(readOnly = true)
    public StockAgingResponse stockAging(InventoryOverviewFilterRequest filter) {
        List<InventoryOverviewResponse> overview = fetchAll(filter).stream()
                .filter(row -> row.getCurrentStock().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        Map<Key, LocalDateTime> lastInboundByKey = new LinkedHashMap<>();
        for (StockMovement m : stockMovementRepository.findAll()) {
            if (!INBOUND_TYPES.contains(m.getType())) continue;
            Key key = new Key(m.getProductId(), m.getWarehouseId());
            lastInboundByKey.merge(key, m.getCreatedAt(), (a, b) -> a.isAfter(b) ? a : b);
        }

        LocalDateTime now = LocalDateTime.now();
        List<StockAgingRowResponse> rows = new ArrayList<>();
        Map<String, BigDecimal[]> bucketTotals = new LinkedHashMap<>();
        for (String bucket : List.of("0-30", "31-60", "61-90", "90+", "Unknown")) {
            bucketTotals.put(bucket, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        }

        for (InventoryOverviewResponse row : overview) {
            LocalDateTime lastInbound = lastInboundByKey.get(new Key(row.getProductId(), row.getWarehouseId()));
            Integer days = lastInbound == null ? null : (int) ChronoUnit.DAYS.between(lastInbound, now);
            String bucket = days == null ? "Unknown" : days <= 30 ? "0-30" : days <= 60 ? "31-60" : days <= 90 ? "61-90" : "90+";
            rows.add(StockAgingRowResponse.builder()
                    .productId(row.getProductId())
                    .productName(row.getProductName())
                    .productSku(row.getProductSku())
                    .warehouseId(row.getWarehouseId())
                    .warehouseName(row.getWarehouseName())
                    .currentStock(row.getCurrentStock())
                    .value(row.getValuationValue())
                    .lastInboundDate(lastInbound)
                    .daysSinceInbound(days)
                    .ageBucket(bucket)
                    .build());
            BigDecimal[] totals = bucketTotals.get(bucket);
            totals[0] = totals[0].add(row.getCurrentStock());
            totals[1] = totals[1].add(row.getValuationValue());
        }
        rows.sort(Comparator.comparing((StockAgingRowResponse r) -> r.getDaysSinceInbound() == null ? Integer.MAX_VALUE : r.getDaysSinceInbound())
                .reversed());

        List<StockAgingBucketResponse> buckets = bucketTotals.entrySet().stream()
                .map(e -> StockAgingBucketResponse.builder().bucket(e.getKey()).totalQuantity(e.getValue()[0]).totalValue(e.getValue()[1]).build())
                .toList();

        return StockAgingResponse.builder().rows(rows).buckets(buckets).build();
    }

    @Override
    @Transactional(readOnly = true)
    public StockTurnoverResponse stockTurnover(StockTurnoverFilterRequest filter) {
        InventoryOverviewFilterRequest overviewFilter = new InventoryOverviewFilterRequest();
        overviewFilter.setCompanyId(filter.getCompanyId());
        overviewFilter.setWarehouseId(filter.getWarehouseId());
        overviewFilter.setProductId(filter.getProductId());
        List<InventoryOverviewResponse> overview = fetchAll(overviewFilter).stream()
                .filter(row -> row.getCurrentStock().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        LocalDateTime windowStart = LocalDateTime.now().minusDays(Math.max(filter.getDays(), 0));
        Map<Key, BigDecimal> outboundByKey = new LinkedHashMap<>();
        Map<Key, LocalDateTime> lastOutboundByKey = new LinkedHashMap<>();
        for (StockMovement m : stockMovementRepository.findAll()) {
            if (!OUTBOUND_TYPES.contains(m.getType())) continue;
            Key key = new Key(m.getProductId(), m.getWarehouseId());
            lastOutboundByKey.merge(key, m.getCreatedAt(), (a, b) -> a.isAfter(b) ? a : b);
            if (!m.getCreatedAt().isBefore(windowStart)) {
                outboundByKey.merge(key, m.getQuantityDelta().abs(), BigDecimal::add);
            }
        }

        List<StockTurnoverRowResponse> rows = new ArrayList<>();
        for (InventoryOverviewResponse row : overview) {
            Key key = new Key(row.getProductId(), row.getWarehouseId());
            rows.add(StockTurnoverRowResponse.builder()
                    .productId(row.getProductId())
                    .productName(row.getProductName())
                    .productSku(row.getProductSku())
                    .warehouseId(row.getWarehouseId())
                    .warehouseName(row.getWarehouseName())
                    .currentStock(row.getCurrentStock())
                    .outboundQuantityInWindow(outboundByKey.getOrDefault(key, BigDecimal.ZERO))
                    .lastOutboundDate(lastOutboundByKey.get(key))
                    .build());
        }
        rows.sort(Comparator.comparing(StockTurnoverRowResponse::getOutboundQuantityInWindow).reversed());

        return StockTurnoverResponse.builder().rows(rows).windowDays(filter.getDays()).build();
    }

    @Override
    @Transactional(readOnly = true)
    public BatchLotStockResponse batchLotStock(BatchLotFilterRequest filter) {
        List<ProductBatch> batches = productBatchRepository.findAll().stream()
                .filter(b -> filter.getCompanyId() == null || filter.getCompanyId().equals(b.getCompanyId()))
                .filter(b -> filter.getProductId() == null || filter.getProductId().equals(b.getProductId()))
                .toList();
        if (batches.isEmpty()) {
            return BatchLotStockResponse.builder().rows(List.of()).build();
        }

        Map<Long, BigDecimal> receivedByBatch = new LinkedHashMap<>();
        for (GoodsReceiptLine line : goodsReceiptLineRepository.findAll()) {
            if (line.getBatchId() == null || line.getQualityStatus() != QualityCheckStatus.PASSED) continue;
            // Batch balances are held in the product's inventory unit, but receipt and
            // delivery lines are in the purchase/sales unit of their source order line
            // (e.g. BOX). Both sides convert through their snapshotted factor before
            // being netted, otherwise a batch ordered in boxes reconciles to nonsense.
            receivedByBatch.merge(line.getBatchId(), inBaseUnits(line.getQuantityReceived(), line.getConversionFactor()), BigDecimal::add);
        }

        Map<Long, BigDecimal> issuedByBatch = new LinkedHashMap<>();
        for (DeliveryLine line : deliveryLineRepository.findAll()) {
            if (line.getBatchId() == null) continue;
            issuedByBatch.merge(line.getBatchId(), inBaseUnits(line.getQuantityDelivered(), line.getConversionFactor()), BigDecimal::add);
        }

        Map<Long, StockAdjustmentStatus> adjustmentStatusById = stockAdjustmentRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(StockAdjustment::getId, StockAdjustment::getStatus));
        Map<Long, BigDecimal> adjustedByBatch = new LinkedHashMap<>();
        for (StockAdjustmentLine line : stockAdjustmentLineRepository.findAll()) {
            if (line.getBatchId() == null) continue;
            if (adjustmentStatusById.get(line.getStockAdjustmentId()) != StockAdjustmentStatus.APPROVED) continue;
            boolean isIncrease = line.getReason() == StockAdjustmentReason.STOCK_INCREASE || line.getReason() == StockAdjustmentReason.OPENING_BALANCE;
            BigDecimal signed = isIncrease ? line.getQuantity() : line.getQuantity().negate();
            adjustedByBatch.merge(line.getBatchId(), signed, BigDecimal::add);
        }

        Map<Long, Product> productsById = productRepository.findAllById(batches.stream().map(ProductBatch::getProductId).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));

        List<BatchLotStockRowResponse> rows = new ArrayList<>();
        for (ProductBatch batch : batches) {
            BigDecimal current = receivedByBatch.getOrDefault(batch.getId(), BigDecimal.ZERO)
                    .subtract(issuedByBatch.getOrDefault(batch.getId(), BigDecimal.ZERO))
                    .add(adjustedByBatch.getOrDefault(batch.getId(), BigDecimal.ZERO));
            if (current.compareTo(BigDecimal.ZERO) <= 0 && !filter.isIncludeDepleted()) continue;
            Product product = productsById.get(batch.getProductId());
            rows.add(BatchLotStockRowResponse.builder()
                    .batchId(batch.getId())
                    .batchNumber(batch.getBatchNumber())
                    .productId(batch.getProductId())
                    .productName(product == null ? null : product.getName())
                    .productSku(product == null ? null : product.getSku())
                    .expirationDate(batch.getExpirationDate())
                    .currentQuantity(current)
                    .build());
        }
        rows.sort(Comparator.comparing((BatchLotStockRowResponse r) -> r.getExpirationDate() == null ? LocalDate.MAX : r.getExpirationDate()));
        return BatchLotStockResponse.builder().rows(rows).build();
    }

    @Override
    @Transactional(readOnly = true)
    public StockByPartyResponse stockByCustomer(PartyStockFilterRequest filter) {
        Map<Long, SalesOrder> openOrders = salesOrderRepository.findAll().stream()
                .filter(so -> filter.getCompanyId() == null || filter.getCompanyId().equals(so.getCompanyId()))
                .filter(so -> filter.getWarehouseId() == null || filter.getWarehouseId().equals(so.getWarehouseId()))
                .filter(so -> filter.getPartyId() == null || filter.getPartyId().equals(so.getCustomerId()))
                .filter(so -> so.getStatus() == SalesOrderStatus.SUBMITTED || so.getStatus() == SalesOrderStatus.PARTIALLY_DELIVERED)
                .collect(java.util.stream.Collectors.toMap(SalesOrder::getId, so -> so));

        record PartyProductKey(Long customerId, Long productId) {
        }
        Map<PartyProductKey, BigDecimal> quantityByKey = new LinkedHashMap<>();
        if (!openOrders.isEmpty()) {
            for (SalesOrderLine line : salesOrderLineRepository.findAll()) {
                SalesOrder so = openOrders.get(line.getSalesOrderId());
                if (so == null) continue;
                BigDecimal remaining = line.getQuantityOrdered().subtract(line.getQuantityDelivered());
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) continue;
                quantityByKey.merge(new PartyProductKey(so.getCustomerId(), line.getProductId()), remaining, BigDecimal::add);
            }
        }

        Map<Long, Customer> customersById = customerRepository.findAllById(quantityByKey.keySet().stream().map(PartyProductKey::customerId).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(Customer::getId, c -> c));
        Map<Long, Product> productsById = productRepository.findAllById(quantityByKey.keySet().stream().map(PartyProductKey::productId).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));

        List<StockByPartyRowResponse> rows = new ArrayList<>();
        for (Map.Entry<PartyProductKey, BigDecimal> entry : quantityByKey.entrySet()) {
            Customer customer = customersById.get(entry.getKey().customerId());
            Product product = productsById.get(entry.getKey().productId());
            rows.add(StockByPartyRowResponse.builder()
                    .partyId(entry.getKey().customerId())
                    .partyName(customer == null ? null : customer.getName())
                    .productId(entry.getKey().productId())
                    .productName(product == null ? null : product.getName())
                    .productSku(product == null ? null : product.getSku())
                    .quantity(entry.getValue())
                    .build());
        }
        rows.sort(Comparator.comparing((StockByPartyRowResponse r) -> r.getPartyName() == null ? "" : r.getPartyName())
                .thenComparing(r -> r.getProductName() == null ? "" : r.getProductName()));
        return StockByPartyResponse.builder().rows(rows).build();
    }

    @Override
    @Transactional(readOnly = true)
    public StockByPartyResponse stockBySupplier(PartyStockFilterRequest filter) {
        Map<Long, PurchaseOrder> openOrders = purchaseOrderRepository.findAll().stream()
                .filter(po -> filter.getCompanyId() == null || filter.getCompanyId().equals(po.getCompanyId()))
                .filter(po -> filter.getWarehouseId() == null || filter.getWarehouseId().equals(po.getWarehouseId()))
                .filter(po -> filter.getPartyId() == null || filter.getPartyId().equals(po.getSupplierId()))
                .filter(po -> po.getStatus() == PurchaseOrderStatus.SENT || po.getStatus() == PurchaseOrderStatus.PARTIALLY_RECEIVED)
                .collect(java.util.stream.Collectors.toMap(PurchaseOrder::getId, po -> po));

        record PartyProductKey(Long supplierId, Long productId) {
        }
        Map<PartyProductKey, BigDecimal> quantityByKey = new LinkedHashMap<>();
        if (!openOrders.isEmpty()) {
            for (PurchaseOrderLine line : purchaseOrderLineRepository.findAll()) {
                PurchaseOrder po = openOrders.get(line.getPurchaseOrderId());
                if (po == null) continue;
                BigDecimal remaining = line.getQuantityOrdered().subtract(line.getQuantityReceived());
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) continue;
                quantityByKey.merge(new PartyProductKey(po.getSupplierId(), line.getProductId()), remaining, BigDecimal::add);
            }
        }

        Map<Long, Supplier> suppliersById = supplierRepository.findAllById(quantityByKey.keySet().stream().map(PartyProductKey::supplierId).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(Supplier::getId, s -> s));
        Map<Long, Product> productsById = productRepository.findAllById(quantityByKey.keySet().stream().map(PartyProductKey::productId).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));

        List<StockByPartyRowResponse> rows = new ArrayList<>();
        for (Map.Entry<PartyProductKey, BigDecimal> entry : quantityByKey.entrySet()) {
            Supplier supplier = suppliersById.get(entry.getKey().supplierId());
            Product product = productsById.get(entry.getKey().productId());
            rows.add(StockByPartyRowResponse.builder()
                    .partyId(entry.getKey().supplierId())
                    .partyName(supplier == null ? null : supplier.getName())
                    .productId(entry.getKey().productId())
                    .productName(product == null ? null : product.getName())
                    .productSku(product == null ? null : product.getSku())
                    .quantity(entry.getValue())
                    .build());
        }
        rows.sort(Comparator.comparing((StockByPartyRowResponse r) -> r.getPartyName() == null ? "" : r.getPartyName())
                .thenComparing(r -> r.getProductName() == null ? "" : r.getProductName()));
        return StockByPartyResponse.builder().rows(rows).build();
    }

    @Override
    @Transactional(readOnly = true)
    public StockProfitabilityResponse stockProfitability(StockMovementReportFilterRequest filter) {
        LocalDate from = filter.getDateFrom();
        LocalDate to = filter.getDateTo();

        Map<Long, Delivery> deliveriesById = deliveryRepository.findAll().stream()
                .filter(d -> filter.getCompanyId() == null || filter.getCompanyId().equals(d.getCompanyId()))
                .filter(d -> filter.getWarehouseId() == null || filter.getWarehouseId().equals(d.getWarehouseId()))
                .filter(d -> from == null || (d.getDeliveryDate() != null && !d.getDeliveryDate().isBefore(from)))
                .filter(d -> to == null || (d.getDeliveryDate() != null && !d.getDeliveryDate().isAfter(to)))
                .collect(java.util.stream.Collectors.toMap(Delivery::getId, d -> d));

        Map<Long, BigDecimal> soldByProduct = new LinkedHashMap<>();
        if (!deliveriesById.isEmpty()) {
            for (DeliveryLine line : deliveryLineRepository.findAll()) {
                if (!deliveriesById.containsKey(line.getDeliveryId())) continue;
                if (filter.getProductId() != null && !filter.getProductId().equals(line.getProductId())) continue;
                // Compared against stock further down, so it has to be in base units.
                soldByProduct.merge(line.getProductId(), inBaseUnits(line.getQuantityDelivered(), line.getConversionFactor()), BigDecimal::add);
            }
        }

        InventoryOverviewFilterRequest overviewFilter = new InventoryOverviewFilterRequest();
        overviewFilter.setCompanyId(filter.getCompanyId());
        overviewFilter.setWarehouseId(filter.getWarehouseId());
        overviewFilter.setProductId(filter.getProductId());
        Map<Long, BigDecimal> stockValueByProduct = new LinkedHashMap<>();
        for (InventoryOverviewResponse row : fetchAll(overviewFilter)) {
            stockValueByProduct.merge(row.getProductId(), row.getValuationValue(), BigDecimal::add);
        }

        Map<Long, Product> productsById = productRepository.findAllById(soldByProduct.keySet().stream().toList())
                .stream().collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));

        List<StockProfitabilityRowResponse> rows = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCogs = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal> entry : soldByProduct.entrySet()) {
            Product product = productsById.get(entry.getKey());
            if (product == null) continue;
            BigDecimal qty = entry.getValue();
            BigDecimal revenue = qty.multiply(product.getSellingPrice());
            BigDecimal cogs = qty.multiply(product.getCostPrice());
            BigDecimal grossProfit = revenue.subtract(cogs);
            BigDecimal margin = revenue.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : grossProfit.divide(revenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            BigDecimal stockValue = stockValueByProduct.getOrDefault(product.getId(), BigDecimal.ZERO);
            BigDecimal turnover = stockValue.compareTo(BigDecimal.ZERO) == 0 ? null : cogs.divide(stockValue, 4, RoundingMode.HALF_UP);

            totalRevenue = totalRevenue.add(revenue);
            totalCogs = totalCogs.add(cogs);
            rows.add(StockProfitabilityRowResponse.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .quantitySold(qty)
                    .revenue(revenue)
                    .cogs(cogs)
                    .grossProfit(grossProfit)
                    .marginPercent(margin)
                    .currentStockValue(stockValue)
                    .turnoverRatio(turnover)
                    .build());
        }
        rows.sort(Comparator.comparing(StockProfitabilityRowResponse::getGrossProfit).reversed());

        return StockProfitabilityResponse.builder()
                .rows(rows)
                .totalRevenue(totalRevenue)
                .totalCogs(totalCogs)
                .totalGrossProfit(totalRevenue.subtract(totalCogs))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StockCountVarianceResponse stockCountVariance(StockCountVarianceFilterRequest filter) {
        List<StockCount> counts = stockCountRepository.findAll().stream()
                .filter(c -> filter.getCompanyId() == null || filter.getCompanyId().equals(c.getCompanyId()))
                .filter(c -> filter.getWarehouseId() == null || filter.getWarehouseId().equals(c.getWarehouseId()))
                .filter(c -> filter.getDateFrom() == null || !c.getCountDate().isBefore(filter.getDateFrom()))
                .filter(c -> filter.getDateTo() == null || !c.getCountDate().isAfter(filter.getDateTo()))
                .toList();
        if (counts.isEmpty()) {
            return StockCountVarianceResponse.builder().rows(List.of()).build();
        }

        Map<Long, StockCount> countsById = counts.stream().collect(java.util.stream.Collectors.toMap(StockCount::getId, c -> c));
        List<StockCountLine> lines = stockCountLineRepository.findAll().stream()
                .filter(l -> countsById.containsKey(l.getStockCountId()))
                .toList();

        Map<Long, Warehouse> warehousesById = warehouseRepository.findAllById(counts.stream().map(StockCount::getWarehouseId).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(Warehouse::getId, w -> w));
        Map<Long, Product> productsById = productRepository.findAllById(lines.stream().map(StockCountLine::getProductId).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));
        Map<Long, WarehouseBin> binsById = warehouseBinRepository.findAllById(
                        lines.stream().map(StockCountLine::getBinId).filter(java.util.Objects::nonNull).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(WarehouseBin::getId, b -> b));

        List<StockCountVarianceRowResponse> rows = new ArrayList<>();
        for (StockCountLine line : lines) {
            StockCount count = countsById.get(line.getStockCountId());
            Warehouse warehouse = warehousesById.get(count.getWarehouseId());
            Product product = productsById.get(line.getProductId());
            WarehouseBin bin = line.getBinId() == null ? null : binsById.get(line.getBinId());
            rows.add(StockCountVarianceRowResponse.builder()
                    .stockCountId(count.getId())
                    .countNumber(count.getCountNumber())
                    .countDate(count.getCountDate())
                    .status(count.getStatus().name())
                    .warehouseId(count.getWarehouseId())
                    .warehouseName(warehouse == null ? null : warehouse.getName())
                    .productId(line.getProductId())
                    .productName(product == null ? null : product.getName())
                    .productSku(product == null ? null : product.getSku())
                    .binId(line.getBinId())
                    .binName(bin == null ? null : bin.getName())
                    .systemQuantity(line.getSystemQuantity())
                    .countedQuantity(line.getCountedQuantity())
                    .varianceQuantity(line.getVarianceQuantity())
                    .build());
        }
        rows.sort(Comparator.comparing(StockCountVarianceRowResponse::getCountDate).reversed());
        return StockCountVarianceResponse.builder().rows(rows).build();
    }

    private List<InventoryOverviewResponse> fetchAll(InventoryOverviewFilterRequest filter) {
        filter.setSize(1_000_000);
        filter.setPage(1);
        return inventoryOverviewService.getOverview(filter).getData();
    }

    // Receipt and delivery lines are stored in their source order line's unit with
    // the conversion factor snapshotted alongside. A null factor means the row
    // predates UoM support and is already in the product's inventory unit.
    private BigDecimal inBaseUnits(BigDecimal quantity, BigDecimal conversionFactor) {
        return conversionFactor == null ? quantity : quantity.multiply(conversionFactor);
    }
}
