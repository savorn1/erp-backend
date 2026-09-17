package com.example.erp.service.impl;

import com.example.erp.dto.CreateDeliveryRequest;
import com.example.erp.dto.DeliveryFilterRequest;
import com.example.erp.dto.DeliveryLineRequest;
import com.example.erp.dto.DeliveryLineResponse;
import com.example.erp.dto.DeliveryResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.entity.Delivery;
import com.example.erp.entity.DeliveryLine;
import com.example.erp.entity.DeliveryStatus;
import com.example.erp.entity.InventorySettings;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductBatch;
import com.example.erp.entity.ProductTrackingType;
import com.example.erp.entity.SalesOrder;
import com.example.erp.entity.SalesOrderLine;
import com.example.erp.entity.SalesOrderStatus;
import com.example.erp.entity.SerialNumber;
import com.example.erp.entity.SerialNumberStatus;
import com.example.erp.entity.StockLevel;
import com.example.erp.entity.StockMovement;
import com.example.erp.entity.StockMovementType;
import com.example.erp.entity.Warehouse;
import com.example.erp.entity.WarehouseBin;
import com.example.erp.entity.WarehouseZone;
import com.example.erp.exception.AppException;
import com.example.erp.repository.DeliveryLineRepository;
import com.example.erp.repository.DeliveryRepository;
import com.example.erp.repository.ProductBatchRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.SalesOrderLineRepository;
import com.example.erp.repository.SalesOrderRepository;
import com.example.erp.repository.SerialNumberRepository;
import com.example.erp.repository.StockLevelRepository;
import com.example.erp.repository.StockMovementRepository;
import com.example.erp.repository.WarehouseBinRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.repository.WarehouseZoneRepository;
import com.example.erp.service.DeliveryService;
import com.example.erp.service.InventorySettingsService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryLineRepository deliveryLineRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final StockLevelRepository stockLevelRepository;
    private final StockMovementRepository stockMovementRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseZoneRepository warehouseZoneRepository;
    private final WarehouseBinRepository warehouseBinRepository;
    private final ProductRepository productRepository;
    private final ProductBatchRepository productBatchRepository;
    private final SerialNumberRepository serialNumberRepository;
    private final InventorySettingsService inventorySettingsService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeliveryResponse> listDeliveries(DeliveryFilterRequest filter) {
        List<Specification<Delivery>> conditions = new ArrayList<>();
        if (filter.getDeliveryNumber() != null && !filter.getDeliveryNumber().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("deliveryNumber")), "%" + filter.getDeliveryNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getSalesOrderId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("salesOrderId"), filter.getSalesOrderId()));
        }
        if (filter.getWarehouseId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("warehouseId"), filter.getWarehouseId()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        Specification<Delivery> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<Delivery> page = deliveryRepository.findAll(spec, pageable);
        List<Delivery> content = page.getContent();

        Map<Long, String> soNumbers = salesOrderRepository.findAllById(
                content.stream().map(Delivery::getSalesOrderId).distinct().toList()
        ).stream().collect(Collectors.toMap(SalesOrder::getId, SalesOrder::getSoNumber));
        Map<Long, String> warehouseNames = warehouseRepository.findAllById(
                content.stream().map(Delivery::getWarehouseId).distinct().toList()
        ).stream().collect(Collectors.toMap(Warehouse::getId, Warehouse::getName));

        return PageResponse.of(page.map(d -> toResponse(d,
                soNumbers.get(d.getSalesOrderId()), warehouseNames.get(d.getWarehouseId()), List.of())));
    }

    @Override
    public DeliveryResponse getDelivery(Long id) {
        Delivery delivery = find(id);
        List<DeliveryLine> lines = deliveryLineRepository.findByDeliveryId(id);
        String soNumber = salesOrderRepository.findById(delivery.getSalesOrderId()).map(SalesOrder::getSoNumber).orElse(null);
        String warehouseName = warehouseRepository.findById(delivery.getWarehouseId()).map(Warehouse::getName).orElse(null);
        return toResponse(delivery, soNumber, warehouseName, lines);
    }

    @Override
    @Transactional
    public DeliveryResponse createDelivery(CreateDeliveryRequest request, String actingUsername) {
        SalesOrder so = salesOrderRepository.findById(request.getSalesOrderId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Sales order not found with id: " + request.getSalesOrderId()));
        if (so.getStatus() != SalesOrderStatus.CONFIRMED && so.getStatus() != SalesOrderStatus.PARTIALLY_DELIVERED) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Sales order must be confirmed before it can be delivered (current status: " + so.getStatus() + ")");
        }

        Map<Long, SalesOrderLine> soLines = salesOrderLineRepository.findBySalesOrderId(so.getId()).stream()
                .collect(Collectors.toMap(SalesOrderLine::getId, l -> l));
        Map<Long, Product> productsByLineId = new HashMap<>();
        for (DeliveryLineRequest lineRequest : request.getLines()) {
            SalesOrderLine soLine = soLines.get(lineRequest.getSalesOrderLineId());
            if (soLine != null) {
                productRepository.findById(soLine.getProductId()).ifPresent(p -> productsByLineId.put(lineRequest.getSalesOrderLineId(), p));
            }
        }

        // ── Structural validation only ──────────────────────────────────────
        // Stock existence/availability isn't checked here — that happens for
        // real at ship time (see shipDelivery). Creating a delivery just
        // records intent, same reasoning as StockAdjustment/StockTransfer's
        // request stage.
        Set<String> serialsInThisRequest = new HashSet<>();
        for (DeliveryLineRequest lineRequest : request.getLines()) {
            SalesOrderLine soLine = soLines.get(lineRequest.getSalesOrderLineId());
            if (soLine == null) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Sales order line not found on this order: " + lineRequest.getSalesOrderLineId());
            }
            BigDecimal remaining = soLine.getQuantityOrdered().subtract(soLine.getQuantityDelivered());
            if (lineRequest.getQuantityDelivered().compareTo(remaining) > 0) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Cannot deliver more than the remaining quantity (" + remaining + ") for line " + soLine.getId());
            }
            if (lineRequest.getBinId() != null) {
                requireBinInWarehouse(lineRequest.getBinId(), so.getWarehouseId());
            }

            Product product = productsByLineId.get(lineRequest.getSalesOrderLineId());
            ProductTrackingType trackingType = product == null || product.getTrackingType() == null
                    ? ProductTrackingType.NONE : product.getTrackingType();

            if (trackingType == ProductTrackingType.BATCH
                    && (lineRequest.getBatchNumber() == null || lineRequest.getBatchNumber().isBlank())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Batch/lot number is required for " + product.getName());
            }
            if (trackingType == ProductTrackingType.SERIAL) {
                List<String> serials = lineRequest.getSerialNumbers();
                int expected = lineRequest.getQuantityDelivered().stripTrailingZeros().intValueExact();
                if (serials == null || serials.size() != expected || serials.stream().anyMatch(s -> s == null || s.isBlank())) {
                    throw new AppException(HttpStatus.BAD_REQUEST,
                            product.getName() + " is serial-tracked — expected exactly " + expected + " serial number(s)");
                }
                for (String serial : serials) {
                    if (!serialsInThisRequest.add(serial)) {
                        throw new AppException(HttpStatus.BAD_REQUEST, "Duplicate serial number in this delivery: " + serial);
                    }
                }
            }
        }

        Delivery delivery = Delivery.builder()
                .companyId(so.getCompanyId())
                .salesOrderId(so.getId())
                .warehouseId(so.getWarehouseId())
                .deliveryDate(request.getDeliveryDate())
                .notes(request.getNotes())
                .createdBy(actingUsername)
                .build();
        deliveryRepository.save(delivery);
        delivery.setDeliveryNumber("DL-" + String.format("%06d", delivery.getId()));
        deliveryRepository.save(delivery);

        List<DeliveryLine> savedLines = new ArrayList<>();
        for (DeliveryLineRequest lineRequest : request.getLines()) {
            SalesOrderLine soLine = soLines.get(lineRequest.getSalesOrderLineId());
            DeliveryLine deliveryLine = DeliveryLine.builder()
                    .deliveryId(delivery.getId())
                    .salesOrderLineId(soLine.getId())
                    .productId(soLine.getProductId())
                    .quantityDelivered(lineRequest.getQuantityDelivered())
                    .binId(lineRequest.getBinId())
                    .batchNumber(lineRequest.getBatchNumber())
                    .serialNumbersRaw(lineRequest.getSerialNumbers() == null ? null : String.join(",", lineRequest.getSerialNumbers()))
                    .build();
            deliveryLineRepository.save(deliveryLine);
            savedLines.add(deliveryLine);
        }

        return toResponse(delivery, so.getSoNumber(), warehouseNameOf(so.getWarehouseId()), savedLines);
    }

    @Override
    @Transactional
    public DeliveryResponse pickDelivery(Long id, String actingUsername) {
        Delivery delivery = find(id);
        if (delivery.getStatus() != DeliveryStatus.PENDING) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only pending deliveries can be picked");
        }
        delivery.setStatus(DeliveryStatus.PICKED);
        delivery.setPickedBy(actingUsername);
        delivery.setPickedAt(LocalDateTime.now());
        deliveryRepository.save(delivery);
        return getDelivery(id);
    }

    @Override
    @Transactional
    public DeliveryResponse packDelivery(Long id, String actingUsername) {
        Delivery delivery = find(id);
        if (delivery.getStatus() != DeliveryStatus.PICKED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only picked deliveries can be packed");
        }
        delivery.setStatus(DeliveryStatus.PACKED);
        delivery.setPackedBy(actingUsername);
        delivery.setPackedAt(LocalDateTime.now());
        deliveryRepository.save(delivery);
        return getDelivery(id);
    }

    @Override
    @Transactional
    public DeliveryResponse shipDelivery(Long id, String actingUsername) {
        Delivery delivery = find(id);
        if (delivery.getStatus() != DeliveryStatus.PACKED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only packed deliveries can be shipped");
        }
        SalesOrder so = salesOrderRepository.findById(delivery.getSalesOrderId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Sales order not found with id: " + delivery.getSalesOrderId()));
        List<DeliveryLine> lines = deliveryLineRepository.findByDeliveryId(id);
        Map<Long, SalesOrderLine> soLines = salesOrderLineRepository.findBySalesOrderId(so.getId()).stream()
                .collect(Collectors.toMap(SalesOrderLine::getId, l -> l));
        InventorySettings settings = inventorySettingsService.resolveForCompany(so.getCompanyId());

        // ── Stock validation ────────────────────────────────────────────────
        // Every line is resolved and checked before anything is written — the
        // real check, now that stock is actually about to move.
        Set<String> serialsInThisRequest = new HashSet<>();
        Map<Long, ProductBatch> resolvedBatchByLine = new HashMap<>();
        Map<Long, List<SerialNumber>> resolvedSerialsByLine = new HashMap<>();
        for (DeliveryLine line : lines) {
            Product product = productRepository.findById(line.getProductId()).orElse(null);
            String productLabel = product == null ? "product id " + line.getProductId() : product.getName();
            ProductTrackingType trackingType = product == null || product.getTrackingType() == null
                    ? ProductTrackingType.NONE : product.getTrackingType();

            ProductBatch batch = null;
            if (trackingType == ProductTrackingType.BATCH) {
                batch = productBatchRepository.findByProductIdAndBatchNumber(line.getProductId(), line.getBatchNumber())
                        .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST,
                                "Batch/lot " + line.getBatchNumber() + " not found for " + productLabel));
                resolvedBatchByLine.put(line.getId(), batch);
            }

            BigDecimal available = availableQuantity(line.getProductId(), so.getWarehouseId(), line.getBinId());
            if (line.getQuantityDelivered().compareTo(available) > 0 && !settings.isAllowNegativeStock()) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Insufficient stock on hand (" + available + ") for " + productLabel);
            }

            if (trackingType == ProductTrackingType.SERIAL) {
                List<String> serials = parseSerials(line.getSerialNumbersRaw());
                List<SerialNumber> matched = new ArrayList<>();
                for (String serial : serials) {
                    if (!serialsInThisRequest.add(serial)) {
                        throw new AppException(HttpStatus.BAD_REQUEST, "Duplicate serial number in this delivery: " + serial);
                    }
                    SerialNumber sn = serialNumberRepository.findBySerialNumber(serial)
                            .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Serial number not found in stock: " + serial));
                    if (sn.getStatus() != SerialNumberStatus.IN_STOCK
                            || !Objects.equals(sn.getProductId(), line.getProductId())
                            || !Objects.equals(sn.getWarehouseId(), so.getWarehouseId())) {
                        throw new AppException(HttpStatus.BAD_REQUEST, "Serial number " + serial + " is not available in stock at this warehouse");
                    }
                    matched.add(sn);
                }
                resolvedSerialsByLine.put(line.getId(), matched);
            }
        }

        for (DeliveryLine line : lines) {
            ProductBatch batch = resolvedBatchByLine.get(line.getId());
            if (batch != null) {
                line.setBatchId(batch.getId());
                deliveryLineRepository.save(line);
            }

            List<SerialNumber> serials = resolvedSerialsByLine.get(line.getId());
            if (serials != null) {
                for (SerialNumber sn : serials) {
                    sn.setStatus(SerialNumberStatus.ISSUED);
                    sn.setDeliveryLineId(line.getId());
                    serialNumberRepository.save(sn);
                }
            }

            SalesOrderLine soLine = soLines.get(line.getSalesOrderLineId());
            soLine.setQuantityDelivered(soLine.getQuantityDelivered().add(line.getQuantityDelivered()));
            salesOrderLineRepository.save(soLine);

            decreaseStock(so.getCompanyId(), line.getProductId(), so.getWarehouseId(), line.getBinId(), line.getQuantityDelivered(), settings.isAllowNegativeStock());
            if (settings.isReserveStock()) {
                releaseReservation(line.getProductId(), so.getCompanyId(), so.getWarehouseId(), line.getQuantityDelivered());
            }
            stockMovementRepository.save(StockMovement.builder()
                    .companyId(so.getCompanyId())
                    .productId(line.getProductId())
                    .warehouseId(so.getWarehouseId())
                    .binId(line.getBinId())
                    .type(StockMovementType.ISSUE)
                    .quantityDelta(line.getQuantityDelivered().negate())
                    .referenceType("DELIVERY")
                    .referenceId(delivery.getId())
                    .createdBy(actingUsername)
                    .build());
        }

        boolean fullyDelivered = soLines.values().stream()
                .allMatch(l -> l.getQuantityDelivered().compareTo(l.getQuantityOrdered()) >= 0);
        so.setStatus(fullyDelivered ? SalesOrderStatus.DELIVERED : SalesOrderStatus.PARTIALLY_DELIVERED);
        salesOrderRepository.save(so);

        delivery.setStatus(DeliveryStatus.SHIPPED);
        delivery.setShippedBy(actingUsername);
        delivery.setShippedAt(LocalDateTime.now());
        deliveryRepository.save(delivery);
        return getDelivery(id);
    }

    @Override
    @Transactional
    public DeliveryResponse completeDelivery(Long id, String actingUsername) {
        Delivery delivery = find(id);
        if (delivery.getStatus() != DeliveryStatus.SHIPPED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only shipped deliveries can be marked delivered");
        }
        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredBy(actingUsername);
        delivery.setDeliveredAt(LocalDateTime.now());
        deliveryRepository.save(delivery);
        return getDelivery(id);
    }

    @Override
    @Transactional
    public DeliveryResponse cancelDelivery(Long id) {
        Delivery delivery = find(id);
        if (delivery.getStatus() == DeliveryStatus.SHIPPED || delivery.getStatus() == DeliveryStatus.DELIVERED
                || delivery.getStatus() == DeliveryStatus.CANCELLED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only pending, picked, or packed deliveries can be cancelled");
        }
        delivery.setStatus(DeliveryStatus.CANCELLED);
        deliveryRepository.save(delivery);
        return getDelivery(id);
    }

    private List<String> parseSerials(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split("[,\\n]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    // When binId is null, the delivery line isn't pinned to one specific bin
    // — sum every bin (plus the unbinned row, if any) this product holds at
    // the warehouse, rather than only the unbinned row. Without this, a
    // product received into a bin via GoodsReceipt looks out of stock
    // whenever a delivery line leaves "bin" unset.
    private BigDecimal availableQuantity(Long productId, Long warehouseId, Long binId) {
        if (binId != null) {
            return stockLevelRepository.findByProductIdAndWarehouseIdAndBinId(productId, warehouseId, binId)
                    .map(StockLevel::getQuantityOnHand)
                    .orElse(BigDecimal.ZERO);
        }
        return stockLevelRepository.findByProductIdAndWarehouseId(productId, warehouseId).stream()
                .map(StockLevel::getQuantityOnHand)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Mirrors availableQuantity's "no specific bin" semantics: pulls from
    // whichever bin(s) actually hold the stock, largest first, until the
    // requested quantity is satisfied. allowNegativeStock (from
    // InventorySettings) lets the final shortfall push the targeted row
    // below zero instead of throwing — the pre-check in shipDelivery has
    // already decided whether that's permitted.
    private void decreaseStock(Long companyId, Long productId, Long warehouseId, Long binId, BigDecimal quantity, boolean allowNegativeStock) {
        if (binId != null) {
            StockLevel stockLevel = stockLevelRepository.findByProductIdAndWarehouseIdAndBinId(productId, warehouseId, binId)
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "No stock on hand for this product at the source location"));
            BigDecimal updated = stockLevel.getQuantityOnHand().subtract(quantity);
            if (updated.compareTo(BigDecimal.ZERO) < 0 && !allowNegativeStock) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Insufficient stock on hand for this product at the source location");
            }
            stockLevel.setQuantityOnHand(updated);
            stockLevelRepository.save(stockLevel);
            return;
        }

        List<StockLevel> stockLevels = stockLevelRepository.findByProductIdAndWarehouseId(productId, warehouseId).stream()
                .sorted(Comparator.comparing(StockLevel::getQuantityOnHand).reversed())
                .toList();
        BigDecimal remaining = quantity;
        for (StockLevel stockLevel : stockLevels) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal take = stockLevel.getQuantityOnHand().min(remaining);
            stockLevel.setQuantityOnHand(stockLevel.getQuantityOnHand().subtract(take));
            stockLevelRepository.save(stockLevel);
            remaining = remaining.subtract(take);
        }
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            if (!allowNegativeStock) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Insufficient stock on hand for this product at the source location");
            }
            StockLevel unbinned = stockLevelRepository.findByProductIdAndWarehouseIdAndBinIdIsNull(productId, warehouseId)
                    .orElseGet(() -> StockLevel.builder()
                            .companyId(companyId)
                            .productId(productId)
                            .warehouseId(warehouseId)
                            .build());
            unbinned.setQuantityOnHand(unbinned.getQuantityOnHand().subtract(remaining));
            stockLevelRepository.save(unbinned);
        }
    }

    // Releases (or, if reserveStock was toggled on after some already-shipped
    // deliveries, no-ops harmlessly at zero) the portion of a reservation
    // this delivery just fulfilled — see SalesOrderServiceImpl's own
    // reserve/release for the same unbinned-row convention.
    private void releaseReservation(Long productId, Long companyId, Long warehouseId, BigDecimal quantity) {
        StockLevel unbinned = stockLevelRepository.findByProductIdAndWarehouseIdAndBinIdIsNull(productId, warehouseId)
                .orElseGet(() -> StockLevel.builder()
                        .companyId(companyId)
                        .productId(productId)
                        .warehouseId(warehouseId)
                        .build());
        BigDecimal updated = unbinned.getReservedQuantity().subtract(quantity);
        unbinned.setReservedQuantity(updated.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : updated);
        stockLevelRepository.save(unbinned);
    }

    private void requireBinInWarehouse(Long binId, Long warehouseId) {
        WarehouseBin bin = warehouseBinRepository.findById(binId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Bin not found with id: " + binId));
        WarehouseZone zone = warehouseZoneRepository.findById(bin.getZoneId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Zone not found for bin: " + binId));
        if (!zone.getWarehouseId().equals(warehouseId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Bin does not belong to the shipping warehouse");
        }
    }

    private String warehouseNameOf(Long warehouseId) {
        return warehouseRepository.findById(warehouseId).map(Warehouse::getName).orElse(null);
    }

    private Delivery find(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Delivery not found with id: " + id));
    }

    private DeliveryResponse toResponse(Delivery delivery, String soNumber, String warehouseName, List<DeliveryLine> lines) {
        Map<Long, Product> products = lines.isEmpty() ? Map.of() : productRepository.findAllById(
                lines.stream().map(DeliveryLine::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, WarehouseBin> bins = lines.isEmpty() ? Map.of() : warehouseBinRepository.findAllById(
                lines.stream().map(DeliveryLine::getBinId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(WarehouseBin::getId, b -> b));
        Map<Long, List<String>> serialsByLineId = lines.isEmpty() ? Map.of() : serialNumberRepository
                .findByDeliveryLineIdIn(lines.stream().map(DeliveryLine::getId).toList()).stream()
                .collect(Collectors.groupingBy(SerialNumber::getDeliveryLineId,
                        Collectors.mapping(SerialNumber::getSerialNumber, Collectors.toList())));

        List<DeliveryLineResponse> lineResponses = lines.stream()
                .map(line -> {
                    Product product = products.get(line.getProductId());
                    WarehouseBin bin = line.getBinId() == null ? null : bins.get(line.getBinId());
                    List<String> serialNumbers = serialsByLineId.getOrDefault(line.getId(), List.of());
                    if (serialNumbers.isEmpty()) {
                        serialNumbers = parseSerials(line.getSerialNumbersRaw());
                    }
                    return DeliveryLineResponse.builder()
                            .id(line.getId())
                            .salesOrderLineId(line.getSalesOrderLineId())
                            .productId(line.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productSku(product == null ? null : product.getSku())
                            .quantityDelivered(line.getQuantityDelivered())
                            .binId(line.getBinId())
                            .binName(bin == null ? null : bin.getName())
                            .batchId(line.getBatchId())
                            .batchNumber(line.getBatchNumber())
                            .serialNumbers(serialNumbers)
                            .build();
                })
                .toList();

        return DeliveryResponse.builder()
                .id(delivery.getId())
                .companyId(delivery.getCompanyId())
                .salesOrderId(delivery.getSalesOrderId())
                .soNumber(soNumber)
                .warehouseId(delivery.getWarehouseId())
                .warehouseName(warehouseName)
                .deliveryNumber(delivery.getDeliveryNumber())
                .deliveryDate(delivery.getDeliveryDate())
                .status((delivery.getStatus() == null ? DeliveryStatus.DELIVERED : delivery.getStatus()).name())
                .notes(delivery.getNotes())
                .createdBy(delivery.getCreatedBy())
                .pickedBy(delivery.getPickedBy())
                .pickedAt(delivery.getPickedAt())
                .packedBy(delivery.getPackedBy())
                .packedAt(delivery.getPackedAt())
                .shippedBy(delivery.getShippedBy())
                .shippedAt(delivery.getShippedAt())
                .deliveredBy(delivery.getDeliveredBy())
                .deliveredAt(delivery.getDeliveredAt())
                .lines(lineResponses)
                .build();
    }
}
