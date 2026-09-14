package com.example.erp.service.impl;

import com.example.erp.dto.CreateStockTransferRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ShipStockTransferLineRequest;
import com.example.erp.dto.ShipStockTransferRequest;
import com.example.erp.dto.StockTransferFilterRequest;
import com.example.erp.dto.StockTransferLineRequest;
import com.example.erp.dto.StockTransferLineResponse;
import com.example.erp.dto.StockTransferResponse;
import com.example.erp.entity.Company;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductBatch;
import com.example.erp.entity.ProductTrackingType;
import com.example.erp.entity.SerialNumber;
import com.example.erp.entity.SerialNumberStatus;
import com.example.erp.entity.StockLevel;
import com.example.erp.entity.StockMovement;
import com.example.erp.entity.StockMovementType;
import com.example.erp.entity.StockTransfer;
import com.example.erp.entity.StockTransferLine;
import com.example.erp.entity.StockTransferStatus;
import com.example.erp.entity.Warehouse;
import com.example.erp.entity.WarehouseBin;
import com.example.erp.entity.WarehouseZone;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.ProductBatchRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.SerialNumberRepository;
import com.example.erp.repository.StockLevelRepository;
import com.example.erp.repository.StockMovementRepository;
import com.example.erp.repository.StockTransferLineRepository;
import com.example.erp.repository.StockTransferRepository;
import com.example.erp.repository.WarehouseBinRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.repository.WarehouseZoneRepository;
import com.example.erp.service.StockTransferService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockTransferServiceImpl implements StockTransferService {

    private final StockTransferRepository stockTransferRepository;
    private final StockTransferLineRepository stockTransferLineRepository;
    private final CompanyRepository companyRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseZoneRepository warehouseZoneRepository;
    private final WarehouseBinRepository warehouseBinRepository;
    private final ProductRepository productRepository;
    private final ProductBatchRepository productBatchRepository;
    private final SerialNumberRepository serialNumberRepository;
    private final StockLevelRepository stockLevelRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockTransferResponse> listStockTransfers(StockTransferFilterRequest filter) {
        List<Specification<StockTransfer>> conditions = new ArrayList<>();
        if (filter.getTransferNumber() != null && !filter.getTransferNumber().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("transferNumber")), "%" + filter.getTransferNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getSourceWarehouseId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("sourceWarehouseId"), filter.getSourceWarehouseId()));
        }
        if (filter.getDestinationWarehouseId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("destinationWarehouseId"), filter.getDestinationWarehouseId()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        Specification<StockTransfer> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<StockTransfer> page = stockTransferRepository.findAll(spec, pageable);
        List<StockTransfer> content = page.getContent();

        Map<Long, String> warehouseNames = warehouseRepository.findAllById(
                java.util.stream.Stream.concat(
                        content.stream().map(StockTransfer::getSourceWarehouseId),
                        content.stream().map(StockTransfer::getDestinationWarehouseId)
                ).distinct().toList()
        ).stream().collect(Collectors.toMap(Warehouse::getId, Warehouse::getName));

        return PageResponse.of(page.map(st -> toSummaryResponse(st,
                warehouseNames.get(st.getSourceWarehouseId()), warehouseNames.get(st.getDestinationWarehouseId()))));
    }

    @Override
    public StockTransferResponse getStockTransfer(Long id) {
        StockTransfer transfer = find(id);
        return toFullResponse(transfer, stockTransferLineRepository.findByStockTransferId(id));
    }

    @Override
    @Transactional
    public StockTransferResponse createStockTransfer(CreateStockTransferRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());
        requireWarehouse(request.getSourceWarehouseId(), request.getCompanyId());
        requireWarehouse(request.getDestinationWarehouseId(), request.getCompanyId());
        if (request.getSourceWarehouseId().equals(request.getDestinationWarehouseId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Source and destination warehouses must be different");
        }

        for (StockTransferLineRequest lineRequest : request.getLines()) {
            Product product = productRepository.findById(lineRequest.getProductId())
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Product not found with id: " + lineRequest.getProductId()));
            if (!product.getCompanyId().equals(request.getCompanyId())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Product does not belong to the selected company: " + product.getName());
            }
            if (lineRequest.getSourceBinId() != null) {
                requireBinInWarehouse(lineRequest.getSourceBinId(), request.getSourceWarehouseId());
            }
            if (lineRequest.getDestinationBinId() != null) {
                requireBinInWarehouse(lineRequest.getDestinationBinId(), request.getDestinationWarehouseId());
            }
            ProductTrackingType trackingType = product.getTrackingType() == null ? ProductTrackingType.NONE : product.getTrackingType();
            if (trackingType == ProductTrackingType.BATCH
                    && (lineRequest.getBatchNumber() == null || lineRequest.getBatchNumber().isBlank())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Batch/lot number is required for " + product.getName());
            }
        }

        StockTransfer transfer = StockTransfer.builder()
                .companyId(request.getCompanyId())
                .sourceWarehouseId(request.getSourceWarehouseId())
                .destinationWarehouseId(request.getDestinationWarehouseId())
                .requestDate(request.getRequestDate())
                .notes(request.getNotes())
                .requestedBy(actingUsername)
                .build();
        stockTransferRepository.save(transfer);
        transfer.setTransferNumber("TR-" + String.format("%06d", transfer.getId()));
        stockTransferRepository.save(transfer);

        List<StockTransferLine> lines = new ArrayList<>();
        for (StockTransferLineRequest lineRequest : request.getLines()) {
            ProductBatch batch = lineRequest.getBatchNumber() == null || lineRequest.getBatchNumber().isBlank()
                    ? null
                    : productBatchRepository.findByProductIdAndBatchNumber(lineRequest.getProductId(), lineRequest.getBatchNumber())
                            .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST,
                                    "Batch/lot " + lineRequest.getBatchNumber() + " not found for product id: " + lineRequest.getProductId()));
            lines.add(StockTransferLine.builder()
                    .stockTransferId(transfer.getId())
                    .productId(lineRequest.getProductId())
                    .quantityRequested(lineRequest.getQuantityRequested())
                    .sourceBinId(lineRequest.getSourceBinId())
                    .destinationBinId(lineRequest.getDestinationBinId())
                    .batchId(batch == null ? null : batch.getId())
                    .build());
        }
        lines = stockTransferLineRepository.saveAll(lines);

        return toFullResponse(transfer, lines);
    }

    @Override
    @Transactional
    public StockTransferResponse approveStockTransfer(Long id, String actingUsername) {
        StockTransfer transfer = find(id);
        if (transfer.getStatus() != StockTransferStatus.REQUESTED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only requested transfers can be approved");
        }
        List<StockTransferLine> lines = stockTransferLineRepository.findByStockTransferId(id);

        // Soft check — confirms enough stock exists right now, but doesn't
        // reserve or move anything. Stock on hand can still change before
        // shipping, which re-validates for real.
        for (StockTransferLine line : lines) {
            BigDecimal available = availableQuantity(line.getProductId(), transfer.getSourceWarehouseId(), line.getSourceBinId());
            if (line.getQuantityRequested().compareTo(available) > 0) {
                Product product = productRepository.findById(line.getProductId()).orElse(null);
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Insufficient stock on hand (" + available + ") to approve transfer for "
                                + (product == null ? "product " + line.getProductId() : product.getName()));
            }
        }

        transfer.setStatus(StockTransferStatus.APPROVED);
        transfer.setApprovedBy(actingUsername);
        stockTransferRepository.save(transfer);
        return toFullResponse(transfer, lines);
    }

    @Override
    @Transactional
    public StockTransferResponse rejectStockTransfer(Long id) {
        StockTransfer transfer = find(id);
        if (transfer.getStatus() != StockTransferStatus.REQUESTED && transfer.getStatus() != StockTransferStatus.APPROVED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only requested or approved transfers can be rejected");
        }
        transfer.setStatus(StockTransferStatus.REJECTED);
        stockTransferRepository.save(transfer);
        return toFullResponse(transfer, stockTransferLineRepository.findByStockTransferId(id));
    }

    @Override
    @Transactional
    public StockTransferResponse shipStockTransfer(Long id, ShipStockTransferRequest request, String actingUsername) {
        StockTransfer transfer = find(id);
        if (transfer.getStatus() != StockTransferStatus.APPROVED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only approved transfers can be shipped");
        }
        List<StockTransferLine> lines = stockTransferLineRepository.findByStockTransferId(id);

        Map<Long, List<String>> serialsByLineId = new HashMap<>();
        if (request.getLines() != null) {
            for (ShipStockTransferLineRequest lr : request.getLines()) {
                serialsByLineId.put(lr.getStockTransferLineId(), lr.getSerialNumbers());
            }
        }

        // ── Stock validation ────────────────────────────────────────────────
        // Every line is checked before anything is written — a transfer is
        // shipped in full or not at all (no partial shipping).
        Set<String> serialsInThisRequest = new HashSet<>();
        Map<Long, List<SerialNumber>> resolvedSerialsByLine = new HashMap<>();
        for (StockTransferLine line : lines) {
            Product product = productRepository.findById(line.getProductId()).orElse(null);
            ProductTrackingType trackingType = product == null || product.getTrackingType() == null
                    ? ProductTrackingType.NONE : product.getTrackingType();

            BigDecimal available = availableQuantity(line.getProductId(), transfer.getSourceWarehouseId(), line.getSourceBinId());
            if (line.getQuantityRequested().compareTo(available) > 0) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Insufficient stock on hand (" + available + ") to ship "
                                + (product == null ? "product " + line.getProductId() : product.getName()));
            }

            if (trackingType == ProductTrackingType.SERIAL) {
                List<String> serials = serialsByLineId.get(line.getId());
                int expected = line.getQuantityRequested().stripTrailingZeros().intValueExact();
                if (serials == null || serials.size() != expected) {
                    throw new AppException(HttpStatus.BAD_REQUEST,
                            product.getName() + " is serial-tracked — expected exactly " + expected + " serial number(s)");
                }
                List<SerialNumber> matched = new ArrayList<>();
                for (String serial : serials) {
                    if (serial == null || serial.isBlank()) {
                        throw new AppException(HttpStatus.BAD_REQUEST, "Serial numbers cannot be blank (" + product.getName() + ")");
                    }
                    if (!serialsInThisRequest.add(serial)) {
                        throw new AppException(HttpStatus.BAD_REQUEST, "Duplicate serial number in this shipment: " + serial);
                    }
                    SerialNumber sn = serialNumberRepository.findBySerialNumber(serial)
                            .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Serial number not found in stock: " + serial));
                    if (sn.getStatus() != SerialNumberStatus.IN_STOCK
                            || !Objects.equals(sn.getProductId(), line.getProductId())
                            || !Objects.equals(sn.getWarehouseId(), transfer.getSourceWarehouseId())) {
                        throw new AppException(HttpStatus.BAD_REQUEST, "Serial number " + serial + " is not available in stock at the source warehouse");
                    }
                    matched.add(sn);
                }
                resolvedSerialsByLine.put(line.getId(), matched);
            }
        }

        for (StockTransferLine line : lines) {
            decreaseStock(line.getProductId(), transfer.getSourceWarehouseId(), line.getSourceBinId(), line.getQuantityRequested());
            stockMovementRepository.save(StockMovement.builder()
                    .companyId(transfer.getCompanyId())
                    .productId(line.getProductId())
                    .warehouseId(transfer.getSourceWarehouseId())
                    .binId(line.getSourceBinId())
                    .type(StockMovementType.TRANSFER_OUT)
                    .quantityDelta(line.getQuantityRequested().negate())
                    .referenceType("STOCK_TRANSFER")
                    .referenceId(transfer.getId())
                    .createdBy(actingUsername)
                    .build());

            List<SerialNumber> serials = resolvedSerialsByLine.get(line.getId());
            if (serials != null) {
                for (SerialNumber sn : serials) {
                    sn.setStatus(SerialNumberStatus.IN_TRANSIT);
                    sn.setTransferLineId(line.getId());
                    serialNumberRepository.save(sn);
                }
            }

            line.setQuantityShipped(line.getQuantityRequested());
            stockTransferLineRepository.save(line);
        }

        transfer.setStatus(StockTransferStatus.SHIPPED);
        transfer.setShipDate(LocalDate.now());
        transfer.setShippedBy(actingUsername);
        stockTransferRepository.save(transfer);
        return toFullResponse(transfer, lines);
    }

    @Override
    @Transactional
    public StockTransferResponse receiveStockTransfer(Long id, String actingUsername) {
        StockTransfer transfer = find(id);
        if (transfer.getStatus() != StockTransferStatus.SHIPPED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only shipped transfers can be received");
        }
        List<StockTransferLine> lines = stockTransferLineRepository.findByStockTransferId(id);

        for (StockTransferLine line : lines) {
            increaseStock(transfer.getCompanyId(), line.getProductId(), transfer.getDestinationWarehouseId(),
                    line.getDestinationBinId(), line.getQuantityShipped());
            stockMovementRepository.save(StockMovement.builder()
                    .companyId(transfer.getCompanyId())
                    .productId(line.getProductId())
                    .warehouseId(transfer.getDestinationWarehouseId())
                    .binId(line.getDestinationBinId())
                    .type(StockMovementType.TRANSFER_IN)
                    .quantityDelta(line.getQuantityShipped())
                    .referenceType("STOCK_TRANSFER")
                    .referenceId(transfer.getId())
                    .createdBy(actingUsername)
                    .build());

            for (SerialNumber sn : serialNumberRepository.findByTransferLineId(line.getId())) {
                sn.setStatus(SerialNumberStatus.IN_STOCK);
                sn.setWarehouseId(transfer.getDestinationWarehouseId());
                sn.setBinId(line.getDestinationBinId());
                sn.setTransferLineId(null);
                serialNumberRepository.save(sn);
            }

            line.setQuantityReceived(line.getQuantityShipped());
            stockTransferLineRepository.save(line);
        }

        transfer.setStatus(StockTransferStatus.RECEIVED);
        transfer.setReceiveDate(LocalDate.now());
        transfer.setReceivedBy(actingUsername);
        stockTransferRepository.save(transfer);
        return toFullResponse(transfer, lines);
    }

    @Override
    @Transactional
    public StockTransferResponse cancelStockTransfer(Long id) {
        StockTransfer transfer = find(id);
        if (transfer.getStatus() != StockTransferStatus.REQUESTED && transfer.getStatus() != StockTransferStatus.APPROVED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only requested or approved transfers can be cancelled");
        }
        transfer.setStatus(StockTransferStatus.CANCELLED);
        stockTransferRepository.save(transfer);
        return toFullResponse(transfer, stockTransferLineRepository.findByStockTransferId(id));
    }

    @Override
    @Transactional
    public void deleteStockTransfer(Long id) {
        StockTransfer transfer = find(id);
        if (transfer.getStatus() != StockTransferStatus.REQUESTED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only requested transfers can be deleted");
        }
        stockTransferLineRepository.deleteByStockTransferId(id);
        stockTransferRepository.deleteById(id);
    }

    private BigDecimal availableQuantity(Long productId, Long warehouseId, Long binId) {
        StockLevel stockLevel = (binId == null
                ? stockLevelRepository.findByProductIdAndWarehouseIdAndBinIdIsNull(productId, warehouseId)
                : stockLevelRepository.findByProductIdAndWarehouseIdAndBinId(productId, warehouseId, binId))
                .orElse(null);
        return stockLevel == null ? BigDecimal.ZERO : stockLevel.getQuantityOnHand();
    }

    private void decreaseStock(Long productId, Long warehouseId, Long binId, BigDecimal quantity) {
        StockLevel stockLevel = (binId == null
                ? stockLevelRepository.findByProductIdAndWarehouseIdAndBinIdIsNull(productId, warehouseId)
                : stockLevelRepository.findByProductIdAndWarehouseIdAndBinId(productId, warehouseId, binId))
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "No stock on hand for this product at the source location"));
        BigDecimal updated = stockLevel.getQuantityOnHand().subtract(quantity);
        if (updated.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Insufficient stock on hand for this product at the source location");
        }
        stockLevel.setQuantityOnHand(updated);
        stockLevelRepository.save(stockLevel);
    }

    private void increaseStock(Long companyId, Long productId, Long warehouseId, Long binId, BigDecimal quantity) {
        StockLevel stockLevel = (binId == null
                ? stockLevelRepository.findByProductIdAndWarehouseIdAndBinIdIsNull(productId, warehouseId)
                : stockLevelRepository.findByProductIdAndWarehouseIdAndBinId(productId, warehouseId, binId))
                .orElseGet(() -> StockLevel.builder()
                        .companyId(companyId)
                        .productId(productId)
                        .warehouseId(warehouseId)
                        .binId(binId)
                        .build());
        stockLevel.setQuantityOnHand(stockLevel.getQuantityOnHand().add(quantity));
        stockLevelRepository.save(stockLevel);
    }

    private void requireBinInWarehouse(Long binId, Long warehouseId) {
        WarehouseBin bin = warehouseBinRepository.findById(binId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Bin not found with id: " + binId));
        WarehouseZone zone = warehouseZoneRepository.findById(bin.getZoneId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Zone not found for bin: " + binId));
        if (!zone.getWarehouseId().equals(warehouseId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Bin does not belong to the selected warehouse");
        }
    }

    private Company requireCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private void requireWarehouse(Long warehouseId, Long companyId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Warehouse not found with id: " + warehouseId));
        if (!warehouse.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Warehouse does not belong to the selected company");
        }
    }

    private StockTransfer find(Long id) {
        return stockTransferRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Stock transfer not found with id: " + id));
    }

    private StockTransferResponse toSummaryResponse(StockTransfer transfer, String sourceWarehouseName, String destinationWarehouseName) {
        return baseResponseBuilder(transfer, sourceWarehouseName, destinationWarehouseName).lines(null).build();
    }

    private StockTransferResponse toFullResponse(StockTransfer transfer, List<StockTransferLine> lines) {
        String sourceWarehouseName = warehouseRepository.findById(transfer.getSourceWarehouseId()).map(Warehouse::getName).orElse(null);
        String destinationWarehouseName = warehouseRepository.findById(transfer.getDestinationWarehouseId()).map(Warehouse::getName).orElse(null);

        Map<Long, Product> products = lines.isEmpty() ? Map.of() : productRepository.findAllById(
                lines.stream().map(StockTransferLine::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, WarehouseBin> bins = lines.isEmpty() ? Map.of() : warehouseBinRepository.findAllById(
                java.util.stream.Stream.concat(
                        lines.stream().map(StockTransferLine::getSourceBinId),
                        lines.stream().map(StockTransferLine::getDestinationBinId)
                ).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(WarehouseBin::getId, b -> b));
        Map<Long, ProductBatch> batches = lines.isEmpty() ? Map.of() : productBatchRepository.findAllById(
                lines.stream().map(StockTransferLine::getBatchId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(ProductBatch::getId, b -> b));
        Map<Long, List<String>> serialsByLineId = lines.isEmpty() ? Map.of() : lines.stream()
                .collect(Collectors.toMap(StockTransferLine::getId,
                        line -> serialNumberRepository.findByTransferLineId(line.getId()).stream()
                                .map(SerialNumber::getSerialNumber).toList()));

        List<StockTransferLineResponse> lineResponses = lines.stream()
                .map(line -> {
                    Product product = products.get(line.getProductId());
                    WarehouseBin sourceBin = line.getSourceBinId() == null ? null : bins.get(line.getSourceBinId());
                    WarehouseBin destinationBin = line.getDestinationBinId() == null ? null : bins.get(line.getDestinationBinId());
                    ProductBatch batch = line.getBatchId() == null ? null : batches.get(line.getBatchId());
                    return StockTransferLineResponse.builder()
                            .id(line.getId())
                            .productId(line.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productSku(product == null ? null : product.getSku())
                            .quantityRequested(line.getQuantityRequested())
                            .sourceBinId(line.getSourceBinId())
                            .sourceBinName(sourceBin == null ? null : sourceBin.getName())
                            .destinationBinId(line.getDestinationBinId())
                            .destinationBinName(destinationBin == null ? null : destinationBin.getName())
                            .batchId(line.getBatchId())
                            .batchNumber(batch == null ? null : batch.getBatchNumber())
                            .quantityShipped(line.getQuantityShipped())
                            .quantityReceived(line.getQuantityReceived())
                            .serialNumbers(serialsByLineId.getOrDefault(line.getId(), List.of()))
                            .build();
                })
                .toList();

        return baseResponseBuilder(transfer, sourceWarehouseName, destinationWarehouseName)
                .lines(lineResponses)
                .build();
    }

    private StockTransferResponse.StockTransferResponseBuilder baseResponseBuilder(
            StockTransfer transfer, String sourceWarehouseName, String destinationWarehouseName) {
        return StockTransferResponse.builder()
                .id(transfer.getId())
                .companyId(transfer.getCompanyId())
                .sourceWarehouseId(transfer.getSourceWarehouseId())
                .sourceWarehouseName(sourceWarehouseName)
                .destinationWarehouseId(transfer.getDestinationWarehouseId())
                .destinationWarehouseName(destinationWarehouseName)
                .transferNumber(transfer.getTransferNumber())
                .requestDate(transfer.getRequestDate())
                .shipDate(transfer.getShipDate())
                .receiveDate(transfer.getReceiveDate())
                .status(transfer.getStatus().name())
                .notes(transfer.getNotes())
                .requestedBy(transfer.getRequestedBy())
                .approvedBy(transfer.getApprovedBy())
                .shippedBy(transfer.getShippedBy())
                .receivedBy(transfer.getReceivedBy());
    }
}
