package com.example.erp.service.impl;

import com.example.erp.dto.CreateStockAdjustmentRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.StockAdjustmentFilterRequest;
import com.example.erp.dto.StockAdjustmentLineRequest;
import com.example.erp.dto.StockAdjustmentLineResponse;
import com.example.erp.dto.StockAdjustmentResponse;
import com.example.erp.entity.Company;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductBatch;
import com.example.erp.entity.ProductTrackingType;
import com.example.erp.entity.SerialNumber;
import com.example.erp.entity.SerialNumberStatus;
import com.example.erp.entity.StockAdjustment;
import com.example.erp.entity.StockAdjustmentLine;
import com.example.erp.entity.StockAdjustmentReason;
import com.example.erp.entity.StockAdjustmentStatus;
import com.example.erp.entity.StockLevel;
import com.example.erp.entity.StockMovement;
import com.example.erp.entity.StockMovementType;
import com.example.erp.entity.Warehouse;
import com.example.erp.entity.WarehouseBin;
import com.example.erp.entity.WarehouseZone;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.ProductBatchRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.SerialNumberRepository;
import com.example.erp.repository.StockAdjustmentLineRepository;
import com.example.erp.repository.StockAdjustmentRepository;
import com.example.erp.repository.StockLevelRepository;
import com.example.erp.repository.StockMovementRepository;
import com.example.erp.repository.WarehouseBinRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.repository.WarehouseZoneRepository;
import com.example.erp.service.StockAdjustmentService;
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
public class StockAdjustmentServiceImpl implements StockAdjustmentService {

    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final StockAdjustmentLineRepository stockAdjustmentLineRepository;
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
    public PageResponse<StockAdjustmentResponse> listStockAdjustments(StockAdjustmentFilterRequest filter) {
        List<Specification<StockAdjustment>> conditions = new ArrayList<>();
        if (filter.getAdjustmentNumber() != null && !filter.getAdjustmentNumber().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("adjustmentNumber")), "%" + filter.getAdjustmentNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getWarehouseId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("warehouseId"), filter.getWarehouseId()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        Specification<StockAdjustment> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<StockAdjustment> page = stockAdjustmentRepository.findAll(spec, pageable);
        List<StockAdjustment> content = page.getContent();

        Map<Long, String> warehouseNames = warehouseRepository.findAllById(
                content.stream().map(StockAdjustment::getWarehouseId).distinct().toList()
        ).stream().collect(Collectors.toMap(Warehouse::getId, Warehouse::getName));

        return PageResponse.of(page.map(a -> toSummaryResponse(a, warehouseNames.get(a.getWarehouseId()))));
    }

    @Override
    public StockAdjustmentResponse getStockAdjustment(Long id) {
        StockAdjustment adjustment = find(id);
        return toFullResponse(adjustment, stockAdjustmentLineRepository.findByStockAdjustmentId(id));
    }

    @Override
    @Transactional
    public StockAdjustmentResponse createStockAdjustment(CreateStockAdjustmentRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());
        requireWarehouse(request.getWarehouseId(), request.getCompanyId());

        for (StockAdjustmentLineRequest lineRequest : request.getLines()) {
            Product product = productRepository.findById(lineRequest.getProductId())
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Product not found with id: " + lineRequest.getProductId()));
            if (!product.getCompanyId().equals(request.getCompanyId())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Product does not belong to the selected company: " + product.getName());
            }
            if (lineRequest.getBinId() != null) {
                requireBinInWarehouse(lineRequest.getBinId(), request.getWarehouseId());
            }
            ProductTrackingType trackingType = product.getTrackingType() == null ? ProductTrackingType.NONE : product.getTrackingType();
            if (trackingType == ProductTrackingType.BATCH
                    && (lineRequest.getBatchNumber() == null || lineRequest.getBatchNumber().isBlank())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Batch/lot number is required for " + product.getName());
            }
            if (trackingType == ProductTrackingType.SERIAL) {
                int expected = lineRequest.getQuantity().stripTrailingZeros().intValueExact();
                List<String> serials = lineRequest.getSerialNumbers();
                if (serials == null || serials.size() != expected || serials.stream().anyMatch(s -> s == null || s.isBlank())) {
                    throw new AppException(HttpStatus.BAD_REQUEST,
                            product.getName() + " is serial-tracked — expected exactly " + expected + " serial number(s)");
                }
            }
        }

        StockAdjustment adjustment = StockAdjustment.builder()
                .companyId(request.getCompanyId())
                .warehouseId(request.getWarehouseId())
                .adjustmentDate(request.getAdjustmentDate())
                .notes(request.getNotes())
                .requestedBy(actingUsername)
                .build();
        stockAdjustmentRepository.save(adjustment);
        adjustment.setAdjustmentNumber("ADJ-" + String.format("%06d", adjustment.getId()));
        stockAdjustmentRepository.save(adjustment);

        List<StockAdjustmentLine> lines = request.getLines().stream()
                .map(r -> StockAdjustmentLine.builder()
                        .stockAdjustmentId(adjustment.getId())
                        .productId(r.getProductId())
                        .binId(r.getBinId())
                        .reason(r.getReason())
                        .quantity(r.getQuantity())
                        .batchNumber(r.getBatchNumber())
                        .expirationDate(r.getExpirationDate())
                        .serialNumbersRaw(r.getSerialNumbers() == null ? null : String.join(",", r.getSerialNumbers()))
                        .build())
                .toList();
        lines = stockAdjustmentLineRepository.saveAll(lines);

        return toFullResponse(adjustment, lines);
    }

    @Override
    @Transactional
    public StockAdjustmentResponse approveStockAdjustment(Long id, String actingUsername) {
        StockAdjustment adjustment = find(id);
        if (adjustment.getStatus() != StockAdjustmentStatus.PENDING) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only pending adjustments can be approved");
        }
        List<StockAdjustmentLine> lines = stockAdjustmentLineRepository.findByStockAdjustmentId(id);

        // ── Stock validation ────────────────────────────────────────────────
        // Every line is resolved and checked before anything is written, so
        // an approval is either fully applied or rejected outright.
        Set<String> serialsInThisRequest = new HashSet<>();
        Map<Long, ProductBatch> resolvedBatchByLine = new HashMap<>();
        Map<Long, List<String>> newSerialsByLine = new HashMap<>();
        Map<Long, List<SerialNumber>> existingSerialsByLine = new HashMap<>();
        for (StockAdjustmentLine line : lines) {
            Product product = productRepository.findById(line.getProductId()).orElse(null);
            String productLabel = product == null ? "product id " + line.getProductId() : product.getName();
            ProductTrackingType trackingType = product == null || product.getTrackingType() == null
                    ? ProductTrackingType.NONE : product.getTrackingType();
            boolean isIncrease = line.getReason() == StockAdjustmentReason.STOCK_INCREASE;

            if (trackingType == ProductTrackingType.BATCH) {
                ProductBatch batch = isIncrease
                        ? findOrCreateBatch(adjustment.getCompanyId(), line.getProductId(), line.getBatchNumber(), line.getExpirationDate())
                        : productBatchRepository.findByProductIdAndBatchNumber(line.getProductId(), line.getBatchNumber())
                                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST,
                                        "Batch/lot " + line.getBatchNumber() + " not found for " + productLabel));
                resolvedBatchByLine.put(line.getId(), batch);
            }

            if (!isIncrease) {
                BigDecimal available = availableQuantity(line.getProductId(), adjustment.getWarehouseId(), line.getBinId());
                if (line.getQuantity().compareTo(available) > 0) {
                    throw new AppException(HttpStatus.BAD_REQUEST,
                            "Insufficient stock on hand (" + available + ") to adjust out " + productLabel);
                }
            }

            if (trackingType == ProductTrackingType.SERIAL) {
                List<String> serials = parseSerials(line.getSerialNumbersRaw());
                if (isIncrease) {
                    for (String serial : serials) {
                        if (!serialsInThisRequest.add(serial)) {
                            throw new AppException(HttpStatus.BAD_REQUEST, "Duplicate serial number in this adjustment: " + serial);
                        }
                        if (serialNumberRepository.existsBySerialNumber(serial)) {
                            throw new AppException(HttpStatus.CONFLICT, "Serial number already in stock: " + serial);
                        }
                    }
                    newSerialsByLine.put(line.getId(), serials);
                } else {
                    List<SerialNumber> matched = new ArrayList<>();
                    for (String serial : serials) {
                        if (!serialsInThisRequest.add(serial)) {
                            throw new AppException(HttpStatus.BAD_REQUEST, "Duplicate serial number in this adjustment: " + serial);
                        }
                        SerialNumber sn = serialNumberRepository.findBySerialNumber(serial)
                                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Serial number not found in stock: " + serial));
                        if (sn.getStatus() != SerialNumberStatus.IN_STOCK
                                || !Objects.equals(sn.getProductId(), line.getProductId())
                                || !Objects.equals(sn.getWarehouseId(), adjustment.getWarehouseId())) {
                            throw new AppException(HttpStatus.BAD_REQUEST, "Serial number " + serial + " is not available in stock at this warehouse");
                        }
                        matched.add(sn);
                    }
                    existingSerialsByLine.put(line.getId(), matched);
                }
            }
        }

        for (StockAdjustmentLine line : lines) {
            boolean isIncrease = line.getReason() == StockAdjustmentReason.STOCK_INCREASE;
            BigDecimal signedDelta = isIncrease ? line.getQuantity() : line.getQuantity().negate();
            ProductBatch batch = resolvedBatchByLine.get(line.getId());
            if (batch != null) {
                line.setBatchId(batch.getId());
            }

            if (isIncrease) {
                increaseStock(adjustment.getCompanyId(), line.getProductId(), adjustment.getWarehouseId(), line.getBinId(), line.getQuantity());
            } else {
                decreaseStock(line.getProductId(), adjustment.getWarehouseId(), line.getBinId(), line.getQuantity());
            }

            List<String> newSerials = newSerialsByLine.get(line.getId());
            if (newSerials != null) {
                for (String serial : newSerials) {
                    serialNumberRepository.save(SerialNumber.builder()
                            .companyId(adjustment.getCompanyId())
                            .productId(line.getProductId())
                            .warehouseId(adjustment.getWarehouseId())
                            .binId(line.getBinId())
                            .batchId(batch == null ? null : batch.getId())
                            .serialNumber(serial)
                            .adjustmentLineId(line.getId())
                            .build());
                }
            }
            List<SerialNumber> existingSerials = existingSerialsByLine.get(line.getId());
            if (existingSerials != null) {
                for (SerialNumber sn : existingSerials) {
                    sn.setStatus(SerialNumberStatus.ADJUSTED_OUT);
                    sn.setAdjustmentLineId(line.getId());
                    serialNumberRepository.save(sn);
                }
            }

            stockAdjustmentLineRepository.save(line);
            stockMovementRepository.save(StockMovement.builder()
                    .companyId(adjustment.getCompanyId())
                    .productId(line.getProductId())
                    .warehouseId(adjustment.getWarehouseId())
                    .binId(line.getBinId())
                    .type(StockMovementType.ADJUSTMENT)
                    .quantityDelta(signedDelta)
                    .referenceType("STOCK_ADJUSTMENT")
                    .referenceId(adjustment.getId())
                    .createdBy(actingUsername)
                    .build());
        }

        adjustment.setStatus(StockAdjustmentStatus.APPROVED);
        adjustment.setApprovedBy(actingUsername);
        adjustment.setApprovalDate(LocalDate.now());
        stockAdjustmentRepository.save(adjustment);
        return toFullResponse(adjustment, lines);
    }

    @Override
    @Transactional
    public StockAdjustmentResponse rejectStockAdjustment(Long id) {
        StockAdjustment adjustment = find(id);
        if (adjustment.getStatus() != StockAdjustmentStatus.PENDING) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only pending adjustments can be rejected");
        }
        adjustment.setStatus(StockAdjustmentStatus.REJECTED);
        stockAdjustmentRepository.save(adjustment);
        return toFullResponse(adjustment, stockAdjustmentLineRepository.findByStockAdjustmentId(id));
    }

    @Override
    @Transactional
    public void deleteStockAdjustment(Long id) {
        StockAdjustment adjustment = find(id);
        if (adjustment.getStatus() != StockAdjustmentStatus.PENDING) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only pending adjustments can be deleted");
        }
        stockAdjustmentLineRepository.deleteByStockAdjustmentId(id);
        stockAdjustmentRepository.deleteById(id);
    }

    private List<String> parseSerials(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split("[,\\n]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    // Found-or-created by (productId, batchNumber) — mirrors GoodsReceiptServiceImpl.
    private ProductBatch findOrCreateBatch(Long companyId, Long productId, String batchNumber, LocalDate expirationDate) {
        return productBatchRepository.findByProductIdAndBatchNumber(productId, batchNumber)
                .orElseGet(() -> productBatchRepository.save(ProductBatch.builder()
                        .companyId(companyId)
                        .productId(productId)
                        .batchNumber(batchNumber)
                        .expirationDate(expirationDate)
                        .build()));
    }

    // When binId is null, the adjustment line isn't pinned to one specific
    // bin — sum every bin (plus the unbinned row, if any) this product holds
    // at the warehouse, rather than only the unbinned row. Without this, a
    // product received into a bin via GoodsReceipt looks out of stock
    // whenever a decrease line leaves "bin" unset.
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
    // requested quantity is satisfied.
    private void decreaseStock(Long productId, Long warehouseId, Long binId, BigDecimal quantity) {
        if (binId != null) {
            StockLevel stockLevel = stockLevelRepository.findByProductIdAndWarehouseIdAndBinId(productId, warehouseId, binId)
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "No stock on hand for this product at the selected location"));
            BigDecimal updated = stockLevel.getQuantityOnHand().subtract(quantity);
            if (updated.compareTo(BigDecimal.ZERO) < 0) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Insufficient stock on hand for this product at the selected location");
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
            throw new AppException(HttpStatus.BAD_REQUEST, "Insufficient stock on hand for this product at the selected location");
        }
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

    private StockAdjustment find(Long id) {
        return stockAdjustmentRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Stock adjustment not found with id: " + id));
    }

    private StockAdjustmentResponse toSummaryResponse(StockAdjustment adjustment, String warehouseName) {
        return baseResponseBuilder(adjustment, warehouseName).lines(null).build();
    }

    private StockAdjustmentResponse toFullResponse(StockAdjustment adjustment, List<StockAdjustmentLine> lines) {
        String warehouseName = warehouseRepository.findById(adjustment.getWarehouseId()).map(Warehouse::getName).orElse(null);

        Map<Long, Product> products = lines.isEmpty() ? Map.of() : productRepository.findAllById(
                lines.stream().map(StockAdjustmentLine::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, WarehouseBin> bins = lines.isEmpty() ? Map.of() : warehouseBinRepository.findAllById(
                lines.stream().map(StockAdjustmentLine::getBinId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(WarehouseBin::getId, b -> b));
        Map<Long, ProductBatch> batches = lines.isEmpty() ? Map.of() : productBatchRepository.findAllById(
                lines.stream().map(StockAdjustmentLine::getBatchId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(ProductBatch::getId, b -> b));

        List<StockAdjustmentLineResponse> lineResponses = lines.stream()
                .map(line -> {
                    Product product = products.get(line.getProductId());
                    WarehouseBin bin = line.getBinId() == null ? null : bins.get(line.getBinId());
                    ProductBatch batch = line.getBatchId() == null ? null : batches.get(line.getBatchId());
                    List<String> serialNumbers = serialNumberRepository.findByAdjustmentLineId(line.getId()).stream()
                            .map(SerialNumber::getSerialNumber).toList();
                    if (serialNumbers.isEmpty()) {
                        serialNumbers = parseSerials(line.getSerialNumbersRaw());
                    }
                    return StockAdjustmentLineResponse.builder()
                            .id(line.getId())
                            .productId(line.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productSku(product == null ? null : product.getSku())
                            .binId(line.getBinId())
                            .binName(bin == null ? null : bin.getName())
                            .reason(line.getReason().name())
                            .quantity(line.getQuantity())
                            .batchId(line.getBatchId())
                            .batchNumber(batch == null ? line.getBatchNumber() : batch.getBatchNumber())
                            .expirationDate(batch == null ? line.getExpirationDate() : batch.getExpirationDate())
                            .serialNumbers(serialNumbers)
                            .build();
                })
                .toList();

        return baseResponseBuilder(adjustment, warehouseName)
                .lines(lineResponses)
                .build();
    }

    private StockAdjustmentResponse.StockAdjustmentResponseBuilder baseResponseBuilder(StockAdjustment adjustment, String warehouseName) {
        return StockAdjustmentResponse.builder()
                .id(adjustment.getId())
                .companyId(adjustment.getCompanyId())
                .warehouseId(adjustment.getWarehouseId())
                .warehouseName(warehouseName)
                .adjustmentNumber(adjustment.getAdjustmentNumber())
                .adjustmentDate(adjustment.getAdjustmentDate())
                .status(adjustment.getStatus().name())
                .notes(adjustment.getNotes())
                .requestedBy(adjustment.getRequestedBy())
                .approvedBy(adjustment.getApprovedBy())
                .approvalDate(adjustment.getApprovalDate());
    }
}
