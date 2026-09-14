package com.example.erp.service.impl;

import com.example.erp.dto.InventoryOverviewFilterRequest;
import com.example.erp.dto.InventoryOverviewResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.entity.Product;
import com.example.erp.entity.PurchaseOrder;
import com.example.erp.entity.PurchaseOrderLine;
import com.example.erp.entity.PurchaseOrderStatus;
import com.example.erp.entity.SalesOrder;
import com.example.erp.entity.SalesOrderLine;
import com.example.erp.entity.SalesOrderStatus;
import com.example.erp.entity.StockLevel;
import com.example.erp.entity.StockTransfer;
import com.example.erp.entity.StockTransferLine;
import com.example.erp.entity.StockTransferStatus;
import com.example.erp.entity.Warehouse;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.PurchaseOrderLineRepository;
import com.example.erp.repository.PurchaseOrderRepository;
import com.example.erp.repository.SalesOrderLineRepository;
import com.example.erp.repository.SalesOrderRepository;
import com.example.erp.repository.StockLevelRepository;
import com.example.erp.repository.StockTransferLineRepository;
import com.example.erp.repository.StockTransferRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.service.InventoryOverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// A computed, read-only cross-module report — nothing here is persisted.
// "Current" comes from StockLevel; "reserved"/"incoming"/"outgoing" are
// derived on the fly from the other modules' own outstanding-quantity
// bookkeeping (SalesOrderLine/PurchaseOrderLine's cumulative fields, and
// StockTransferLine.quantityShipped for in-flight transfers) rather than
// duplicated into new columns.
@Service
@RequiredArgsConstructor
public class InventoryOverviewServiceImpl implements InventoryOverviewService {

    private final StockLevelRepository stockLevelRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final StockTransferRepository stockTransferRepository;
    private final StockTransferLineRepository stockTransferLineRepository;

    private record Key(Long productId, Long warehouseId) {
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InventoryOverviewResponse> getOverview(InventoryOverviewFilterRequest filter) {
        List<StockLevel> stockLevels = stockLevelRepository.findAll().stream()
                .filter(s -> filter.getCompanyId() == null || filter.getCompanyId().equals(s.getCompanyId()))
                .filter(s -> filter.getWarehouseId() == null || filter.getWarehouseId().equals(s.getWarehouseId()))
                .filter(s -> filter.getProductId() == null || filter.getProductId().equals(s.getProductId()))
                .toList();

        Map<Key, BigDecimal> currentByKey = new HashMap<>();
        for (StockLevel sl : stockLevels) {
            currentByKey.merge(new Key(sl.getProductId(), sl.getWarehouseId()), sl.getQuantityOnHand(), BigDecimal::add);
        }

        Map<Long, SalesOrder> openSalesOrders = salesOrderRepository.findAll().stream()
                .filter(so -> so.getStatus() == SalesOrderStatus.SUBMITTED || so.getStatus() == SalesOrderStatus.PARTIALLY_DELIVERED)
                .collect(Collectors.toMap(SalesOrder::getId, so -> so));
        Map<Key, BigDecimal> reservedByKey = new HashMap<>();
        if (!openSalesOrders.isEmpty()) {
            for (SalesOrderLine line : salesOrderLineRepository.findAll()) {
                SalesOrder so = openSalesOrders.get(line.getSalesOrderId());
                if (so == null) continue;
                BigDecimal remaining = line.getQuantityOrdered().subtract(line.getQuantityDelivered());
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) continue;
                reservedByKey.merge(new Key(line.getProductId(), so.getWarehouseId()), remaining, BigDecimal::add);
            }
        }

        Map<Long, PurchaseOrder> openPurchaseOrders = purchaseOrderRepository.findAll().stream()
                .filter(po -> po.getStatus() == PurchaseOrderStatus.SENT || po.getStatus() == PurchaseOrderStatus.PARTIALLY_RECEIVED)
                .collect(Collectors.toMap(PurchaseOrder::getId, po -> po));
        Map<Key, BigDecimal> incomingByKey = new HashMap<>();
        if (!openPurchaseOrders.isEmpty()) {
            for (PurchaseOrderLine line : purchaseOrderLineRepository.findAll()) {
                PurchaseOrder po = openPurchaseOrders.get(line.getPurchaseOrderId());
                if (po == null) continue;
                BigDecimal remaining = line.getQuantityOrdered().subtract(line.getQuantityReceived());
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) continue;
                incomingByKey.merge(new Key(line.getProductId(), po.getWarehouseId()), remaining, BigDecimal::add);
            }
        }

        Map<Long, StockTransfer> shippedTransfers = stockTransferRepository.findAll().stream()
                .filter(t -> t.getStatus() == StockTransferStatus.SHIPPED)
                .collect(Collectors.toMap(StockTransfer::getId, t -> t));
        Map<Key, BigDecimal> outgoingByKey = new HashMap<>();
        if (!shippedTransfers.isEmpty()) {
            for (StockTransferLine line : stockTransferLineRepository.findAll()) {
                StockTransfer transfer = shippedTransfers.get(line.getStockTransferId());
                if (transfer == null || line.getQuantityShipped() == null) continue;
                outgoingByKey.merge(new Key(line.getProductId(), transfer.getSourceWarehouseId()), line.getQuantityShipped(), BigDecimal::add);
                incomingByKey.merge(new Key(line.getProductId(), transfer.getDestinationWarehouseId()), line.getQuantityShipped(), BigDecimal::add);
            }
        }

        Set<Key> allKeys = new HashSet<>();
        allKeys.addAll(currentByKey.keySet());
        allKeys.addAll(reservedByKey.keySet());
        allKeys.addAll(incomingByKey.keySet());
        allKeys.addAll(outgoingByKey.keySet());

        Map<Long, Product> productsById = productRepository.findAllById(
                allKeys.stream().map(Key::productId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, Warehouse> warehousesById = warehouseRepository.findAllById(
                allKeys.stream().map(Key::warehouseId).distinct().toList()
        ).stream().collect(Collectors.toMap(Warehouse::getId, w -> w));

        String search = filter.getSearch() == null ? null : filter.getSearch().toLowerCase();
        List<InventoryOverviewResponse> rows = new ArrayList<>();
        for (Key key : allKeys) {
            Product product = productsById.get(key.productId());
            Warehouse warehouse = warehousesById.get(key.warehouseId());
            if (product == null || warehouse == null) continue;
            if (filter.getCompanyId() != null && !filter.getCompanyId().equals(product.getCompanyId())) continue;
            if (search != null && !search.isBlank()
                    && !product.getName().toLowerCase().contains(search)
                    && !(product.getSku() != null && product.getSku().toLowerCase().contains(search))) {
                continue;
            }

            BigDecimal current = currentByKey.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal reserved = reservedByKey.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal incoming = incomingByKey.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal outgoing = outgoingByKey.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal unitCost = product.getCostPrice() == null ? BigDecimal.ZERO : product.getCostPrice();

            rows.add(InventoryOverviewResponse.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .warehouseId(warehouse.getId())
                    .warehouseName(warehouse.getName())
                    .currentStock(current)
                    .reservedStock(reserved)
                    .availableStock(current.subtract(reserved))
                    .incomingStock(incoming)
                    .outgoingStock(outgoing)
                    .unitCost(unitCost)
                    .valuationValue(current.multiply(unitCost))
                    .reorderPoint(product.getReorderPoint() == null ? BigDecimal.ZERO : product.getReorderPoint())
                    .build());
        }

        Comparator<InventoryOverviewResponse> comparator = switch (filter.getSortBy() == null ? "" : filter.getSortBy()) {
            case "currentStock" -> Comparator.comparing(InventoryOverviewResponse::getCurrentStock);
            case "valuationValue" -> Comparator.comparing(InventoryOverviewResponse::getValuationValue);
            case "warehouseName" -> Comparator.comparing(r -> r.getWarehouseName() == null ? "" : r.getWarehouseName());
            default -> Comparator.comparing(r -> r.getProductName() == null ? "" : r.getProductName());
        };
        if (!"asc".equalsIgnoreCase(filter.getSortOrder())) {
            comparator = comparator.reversed();
        }
        rows.sort(comparator);

        int pageNumber = Math.max(filter.getPage() - 1, 0);
        int size = filter.getSize() <= 0 ? 10 : filter.getSize();
        int from = Math.min(pageNumber * size, rows.size());
        int to = Math.min(from + size, rows.size());

        Pageable pageable = PageRequest.of(pageNumber, size);
        Page<InventoryOverviewResponse> pageResult = new PageImpl<>(rows.subList(from, to), pageable, rows.size());
        return PageResponse.of(pageResult);
    }
}
