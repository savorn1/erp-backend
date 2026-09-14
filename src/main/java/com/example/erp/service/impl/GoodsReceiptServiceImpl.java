package com.example.erp.service.impl;

import com.example.erp.dto.CreateGoodsReceiptRequest;
import com.example.erp.dto.GoodsReceiptFilterRequest;
import com.example.erp.dto.GoodsReceiptLineRequest;
import com.example.erp.dto.GoodsReceiptLineResponse;
import com.example.erp.dto.GoodsReceiptResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.QualityCheckRequest;
import com.example.erp.entity.GoodsReceipt;
import com.example.erp.entity.GoodsReceiptLine;
import com.example.erp.entity.GoodsReceiptStatus;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductBatch;
import com.example.erp.entity.ProductTrackingType;
import com.example.erp.entity.PurchaseOrder;
import com.example.erp.entity.PurchaseOrderLine;
import com.example.erp.entity.PurchaseOrderStatus;
import com.example.erp.entity.QualityCheckStatus;
import com.example.erp.entity.SerialNumber;
import com.example.erp.entity.SerialNumberStatus;
import com.example.erp.entity.StockLevel;
import com.example.erp.entity.StockMovement;
import com.example.erp.entity.StockMovementType;
import com.example.erp.entity.UnitOfMeasure;
import com.example.erp.entity.Warehouse;
import com.example.erp.entity.WarehouseBin;
import com.example.erp.entity.WarehouseZone;
import com.example.erp.exception.AppException;
import com.example.erp.repository.GoodsReceiptLineRepository;
import com.example.erp.repository.GoodsReceiptRepository;
import com.example.erp.repository.ProductBatchRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.PurchaseOrderLineRepository;
import com.example.erp.repository.PurchaseOrderRepository;
import com.example.erp.repository.SerialNumberRepository;
import com.example.erp.repository.StockLevelRepository;
import com.example.erp.repository.StockMovementRepository;
import com.example.erp.repository.UnitOfMeasureRepository;
import com.example.erp.repository.WarehouseBinRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.repository.WarehouseZoneRepository;
import com.example.erp.service.GoodsReceiptService;
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
import java.time.LocalDateTime;
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
public class GoodsReceiptServiceImpl implements GoodsReceiptService {

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final GoodsReceiptLineRepository goodsReceiptLineRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final StockLevelRepository stockLevelRepository;
    private final StockMovementRepository stockMovementRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseZoneRepository warehouseZoneRepository;
    private final WarehouseBinRepository warehouseBinRepository;
    private final ProductRepository productRepository;
    private final ProductBatchRepository productBatchRepository;
    private final SerialNumberRepository serialNumberRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GoodsReceiptResponse> listGoodsReceipts(GoodsReceiptFilterRequest filter) {
        List<Specification<GoodsReceipt>> conditions = new ArrayList<>();
        if (filter.getReceiptNumber() != null && !filter.getReceiptNumber().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("receiptNumber")), "%" + filter.getReceiptNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getPurchaseOrderId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("purchaseOrderId"), filter.getPurchaseOrderId()));
        }
        if (filter.getWarehouseId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("warehouseId"), filter.getWarehouseId()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        Specification<GoodsReceipt> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<GoodsReceipt> page = goodsReceiptRepository.findAll(spec, pageable);
        List<GoodsReceipt> content = page.getContent();

        Map<Long, String> poNumbers = purchaseOrderRepository.findAllById(
                content.stream().map(GoodsReceipt::getPurchaseOrderId).distinct().toList()
        ).stream().collect(Collectors.toMap(PurchaseOrder::getId, PurchaseOrder::getPoNumber));
        Map<Long, String> warehouseNames = warehouseRepository.findAllById(
                content.stream().map(GoodsReceipt::getWarehouseId).distinct().toList()
        ).stream().collect(Collectors.toMap(Warehouse::getId, Warehouse::getName));

        return PageResponse.of(page.map(gr -> toResponse(gr,
                poNumbers.get(gr.getPurchaseOrderId()), warehouseNames.get(gr.getWarehouseId()), List.of())));
    }

    @Override
    public GoodsReceiptResponse getGoodsReceipt(Long id) {
        GoodsReceipt receipt = find(id);
        List<GoodsReceiptLine> lines = goodsReceiptLineRepository.findByGoodsReceiptId(id);
        String poNumber = purchaseOrderRepository.findById(receipt.getPurchaseOrderId()).map(PurchaseOrder::getPoNumber).orElse(null);
        String warehouseName = warehouseRepository.findById(receipt.getWarehouseId()).map(Warehouse::getName).orElse(null);
        return toResponse(receipt, poNumber, warehouseName, lines);
    }

    @Override
    @Transactional
    public GoodsReceiptResponse createGoodsReceipt(CreateGoodsReceiptRequest request, String actingUsername) {
        PurchaseOrder po = purchaseOrderRepository.findById(request.getPurchaseOrderId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Purchase order not found with id: " + request.getPurchaseOrderId()));
        if (po.getStatus() != PurchaseOrderStatus.SENT && po.getStatus() != PurchaseOrderStatus.PARTIALLY_RECEIVED) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Purchase order must be sent to the supplier before it can be received (current status: " + po.getStatus() + ")");
        }

        Map<Long, PurchaseOrderLine> poLines = purchaseOrderLineRepository.findByPurchaseOrderId(po.getId()).stream()
                .collect(Collectors.toMap(PurchaseOrderLine::getId, l -> l));
        Map<Long, Product> productsByLineId = new HashMap<>();
        for (GoodsReceiptLineRequest lineRequest : request.getLines()) {
            PurchaseOrderLine poLine = poLines.get(lineRequest.getPurchaseOrderLineId());
            if (poLine != null) {
                productRepository.findById(poLine.getProductId()).ifPresent(p -> productsByLineId.put(lineRequest.getPurchaseOrderLineId(), p));
            }
        }

        // ── Stock validation ────────────────────────────────────────────────
        // Every line is checked before anything is written, so a receipt is
        // either fully applied or rejected outright — never half-posted.
        Set<String> serialsInThisRequest = new HashSet<>();
        for (GoodsReceiptLineRequest lineRequest : request.getLines()) {
            PurchaseOrderLine poLine = poLines.get(lineRequest.getPurchaseOrderLineId());
            if (poLine == null) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Purchase order line not found on this order: " + lineRequest.getPurchaseOrderLineId());
            }
            // Net of quantity other receipts already claimed against this line
            // that haven't cleared quality check yet — two in-flight receipts
            // can't both claim the same outstanding stock before either is
            // inspected.
            BigDecimal pending = goodsReceiptLineRepository
                    .sumQuantityByPurchaseOrderLineIdAndQualityStatus(poLine.getId(), QualityCheckStatus.PENDING);
            BigDecimal remaining = poLine.getQuantityOrdered().subtract(poLine.getQuantityReceived()).subtract(pending);
            if (lineRequest.getQuantityReceived().compareTo(remaining) > 0) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Cannot receive more than the remaining quantity (" + remaining + ") for line " + poLine.getId());
            }
            if (lineRequest.getBinId() != null) {
                requireBinInWarehouse(lineRequest.getBinId(), po.getWarehouseId());
            }

            Product product = productsByLineId.get(lineRequest.getPurchaseOrderLineId());
            ProductTrackingType trackingType = product == null || product.getTrackingType() == null
                    ? ProductTrackingType.NONE : product.getTrackingType();

            if (trackingType == ProductTrackingType.BATCH) {
                if (lineRequest.getBatchNumber() == null || lineRequest.getBatchNumber().isBlank()) {
                    throw new AppException(HttpStatus.BAD_REQUEST,
                            "Batch/lot number is required for " + product.getName());
                }
            }
            if (lineRequest.getExpirationDate() != null && lineRequest.getExpirationDate().isBefore(LocalDate.now())) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Cannot receive stock with an expiration date in the past (line " + poLine.getId() + ")");
            }
            if (trackingType == ProductTrackingType.SERIAL) {
                List<String> serials = lineRequest.getSerialNumbers();
                // One serial per base/inventory unit — receiving "2 BOX" of a
                // 24-per-box serial-tracked product still needs 48 serials.
                BigDecimal conversionFactor = poLine.getConversionFactor() != null ? poLine.getConversionFactor() : BigDecimal.ONE;
                int expected = lineRequest.getQuantityReceived().multiply(conversionFactor).stripTrailingZeros().intValueExact();
                if (serials == null || serials.size() != expected) {
                    throw new AppException(HttpStatus.BAD_REQUEST,
                            product.getName() + " is serial-tracked — expected exactly " + expected + " serial number(s)");
                }
                for (String serial : serials) {
                    if (serial == null || serial.isBlank()) {
                        throw new AppException(HttpStatus.BAD_REQUEST, "Serial numbers cannot be blank (" + product.getName() + ")");
                    }
                    if (!serialsInThisRequest.add(serial)) {
                        throw new AppException(HttpStatus.BAD_REQUEST, "Duplicate serial number in this receipt: " + serial);
                    }
                    if (serialNumberRepository.existsBySerialNumber(serial)) {
                        throw new AppException(HttpStatus.CONFLICT, "Serial number already in stock: " + serial);
                    }
                }
            }
        }

        GoodsReceipt receipt = GoodsReceipt.builder()
                .companyId(po.getCompanyId())
                .purchaseOrderId(po.getId())
                .warehouseId(po.getWarehouseId())
                .receiptDate(request.getReceiptDate())
                .notes(request.getNotes())
                .createdBy(actingUsername)
                .build();
        goodsReceiptRepository.save(receipt);
        receipt.setReceiptNumber("GR-" + String.format("%06d", receipt.getId()));
        goodsReceiptRepository.save(receipt);

        List<GoodsReceiptLine> savedLines = new ArrayList<>();
        for (GoodsReceiptLineRequest lineRequest : request.getLines()) {
            PurchaseOrderLine poLine = poLines.get(lineRequest.getPurchaseOrderLineId());

            Long batchId = null;
            if (lineRequest.getBatchNumber() != null && !lineRequest.getBatchNumber().isBlank()) {
                batchId = findOrCreateBatch(po.getCompanyId(), poLine.getProductId(),
                        lineRequest.getBatchNumber(), lineRequest.getExpirationDate()).getId();
            }

            GoodsReceiptLine receiptLine = GoodsReceiptLine.builder()
                    .goodsReceiptId(receipt.getId())
                    .purchaseOrderLineId(poLine.getId())
                    .productId(poLine.getProductId())
                    .unitOfMeasureId(poLine.getUnitOfMeasureId())
                    .conversionFactor(poLine.getConversionFactor())
                    .quantityReceived(lineRequest.getQuantityReceived())
                    .binId(lineRequest.getBinId())
                    .batchId(batchId)
                    .qualityStatus(QualityCheckStatus.PENDING)
                    .build();
            goodsReceiptLineRepository.save(receiptLine);
            savedLines.add(receiptLine);

            // Reserved, not yet usable stock — see recordQualityCheck, which
            // flips these to IN_STOCK (PASSED) or QC_REJECTED (FAILED).
            if (lineRequest.getSerialNumbers() != null) {
                for (String serial : lineRequest.getSerialNumbers()) {
                    serialNumberRepository.save(SerialNumber.builder()
                            .companyId(po.getCompanyId())
                            .productId(poLine.getProductId())
                            .warehouseId(po.getWarehouseId())
                            .binId(lineRequest.getBinId())
                            .batchId(batchId)
                            .serialNumber(serial)
                            .status(SerialNumberStatus.PENDING_QC)
                            .goodsReceiptLineId(receiptLine.getId())
                            .build());
                }
            }
        }

        return toResponse(receipt, po.getPoNumber(), warehouseNameOf(po.getWarehouseId()), savedLines);
    }

    @Override
    @Transactional
    public GoodsReceiptResponse recordQualityCheck(Long receiptId, Long lineId, QualityCheckRequest request, String actingUsername) {
        if (request.getStatus() == QualityCheckStatus.PENDING) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Quality check result must be PASSED or FAILED");
        }
        GoodsReceipt receipt = find(receiptId);
        GoodsReceiptLine line = goodsReceiptLineRepository.findById(lineId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Goods receipt line not found with id: " + lineId));
        if (!line.getGoodsReceiptId().equals(receiptId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Line " + lineId + " does not belong to receipt " + receiptId);
        }
        if (line.getQualityStatus() != QualityCheckStatus.PENDING) {
            throw new AppException(HttpStatus.BAD_REQUEST, "This line has already been quality-checked");
        }

        List<SerialNumber> serials = serialNumberRepository.findByGoodsReceiptLineId(line.getId());

        if (request.getStatus() == QualityCheckStatus.PASSED) {
            PurchaseOrder po = purchaseOrderRepository.findById(receipt.getPurchaseOrderId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Purchase order not found with id: " + receipt.getPurchaseOrderId()));
            PurchaseOrderLine poLine = purchaseOrderLineRepository.findById(line.getPurchaseOrderLineId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Purchase order line not found with id: " + line.getPurchaseOrderLineId()));

            // Stock and its ledger always move in the product's base/inventory
            // unit, regardless of what unit this line was received in.
            BigDecimal conversionFactor = line.getConversionFactor() != null ? line.getConversionFactor() : BigDecimal.ONE;
            BigDecimal baseQuantity = line.getQuantityReceived().multiply(conversionFactor);

            increaseStock(receipt.getCompanyId(), line.getProductId(), receipt.getWarehouseId(), line.getBinId(), baseQuantity);
            stockMovementRepository.save(StockMovement.builder()
                    .companyId(receipt.getCompanyId())
                    .productId(line.getProductId())
                    .warehouseId(receipt.getWarehouseId())
                    .binId(line.getBinId())
                    .type(StockMovementType.RECEIPT)
                    .quantityDelta(baseQuantity)
                    .referenceType("GOODS_RECEIPT")
                    .referenceId(receipt.getId())
                    .createdBy(actingUsername)
                    .build());

            poLine.setQuantityReceived(poLine.getQuantityReceived().add(line.getQuantityReceived()));
            purchaseOrderLineRepository.save(poLine);

            serials.forEach(s -> s.setStatus(SerialNumberStatus.IN_STOCK));
            serialNumberRepository.saveAll(serials);

            boolean fullyReceived = purchaseOrderLineRepository.findByPurchaseOrderId(po.getId()).stream()
                    .allMatch(l -> l.getQuantityReceived().compareTo(l.getQuantityOrdered()) >= 0);
            po.setStatus(fullyReceived ? PurchaseOrderStatus.RECEIVED : PurchaseOrderStatus.PARTIALLY_RECEIVED);
            purchaseOrderRepository.save(po);
        } else {
            serials.forEach(s -> s.setStatus(SerialNumberStatus.QC_REJECTED));
            serialNumberRepository.saveAll(serials);
        }

        line.setQualityStatus(request.getStatus());
        line.setQualityNotes(request.getNotes());
        line.setQualityCheckedBy(actingUsername);
        line.setQualityCheckedAt(LocalDateTime.now());
        goodsReceiptLineRepository.save(line);

        List<GoodsReceiptLine> allLines = goodsReceiptLineRepository.findByGoodsReceiptId(receiptId);
        boolean allChecked = allLines.stream().noneMatch(l -> l.getQualityStatus() == QualityCheckStatus.PENDING);
        if (allChecked) {
            receipt.setStatus(GoodsReceiptStatus.COMPLETED);
            goodsReceiptRepository.save(receipt);
        }

        String poNumber = purchaseOrderRepository.findById(receipt.getPurchaseOrderId()).map(PurchaseOrder::getPoNumber).orElse(null);
        return toResponse(receipt, poNumber, warehouseNameOf(receipt.getWarehouseId()), allLines);
    }

    // Found-or-created by (productId, batchNumber) — if the batch already
    // exists, its stored expiration date wins over whatever this request
    // supplied (the batch's expiration is a fact about the batch itself, set
    // once when it's first received).
    private ProductBatch findOrCreateBatch(Long companyId, Long productId, String batchNumber, LocalDate expirationDate) {
        return productBatchRepository.findByProductIdAndBatchNumber(productId, batchNumber)
                .orElseGet(() -> productBatchRepository.save(ProductBatch.builder()
                        .companyId(companyId)
                        .productId(productId)
                        .batchNumber(batchNumber)
                        .expirationDate(expirationDate)
                        .build()));
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
            throw new AppException(HttpStatus.BAD_REQUEST, "Bin does not belong to the receiving warehouse");
        }
    }

    private String warehouseNameOf(Long warehouseId) {
        return warehouseRepository.findById(warehouseId).map(Warehouse::getName).orElse(null);
    }

    private GoodsReceipt find(Long id) {
        return goodsReceiptRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Goods receipt not found with id: " + id));
    }

    private GoodsReceiptResponse toResponse(GoodsReceipt receipt, String poNumber, String warehouseName, List<GoodsReceiptLine> lines) {
        Map<Long, Product> products = lines.isEmpty() ? Map.of() : productRepository.findAllById(
                lines.stream().map(GoodsReceiptLine::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, WarehouseBin> bins = lines.isEmpty() ? Map.of() : warehouseBinRepository.findAllById(
                lines.stream().map(GoodsReceiptLine::getBinId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(WarehouseBin::getId, b -> b));
        Map<Long, ProductBatch> batches = lines.isEmpty() ? Map.of() : productBatchRepository.findAllById(
                lines.stream().map(GoodsReceiptLine::getBatchId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(ProductBatch::getId, b -> b));
        Map<Long, List<String>> serialsByLineId = lines.isEmpty() ? Map.of() : serialNumberRepository
                .findByGoodsReceiptLineIdIn(lines.stream().map(GoodsReceiptLine::getId).toList()).stream()
                .collect(Collectors.groupingBy(SerialNumber::getGoodsReceiptLineId,
                        Collectors.mapping(SerialNumber::getSerialNumber, Collectors.toList())));
        Map<Long, UnitOfMeasure> units = lines.isEmpty() ? Map.of() : unitOfMeasureRepository.findAllById(
                lines.stream().map(GoodsReceiptLine::getUnitOfMeasureId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(UnitOfMeasure::getId, u -> u));

        List<GoodsReceiptLineResponse> lineResponses = lines.stream()
                .map(line -> {
                    Product product = products.get(line.getProductId());
                    WarehouseBin bin = line.getBinId() == null ? null : bins.get(line.getBinId());
                    ProductBatch batch = line.getBatchId() == null ? null : batches.get(line.getBatchId());
                    UnitOfMeasure unit = line.getUnitOfMeasureId() == null ? null : units.get(line.getUnitOfMeasureId());
                    BigDecimal conversionFactor = line.getConversionFactor() != null ? line.getConversionFactor() : BigDecimal.ONE;
                    return GoodsReceiptLineResponse.builder()
                            .id(line.getId())
                            .purchaseOrderLineId(line.getPurchaseOrderLineId())
                            .productId(line.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productSku(product == null ? null : product.getSku())
                            .unitOfMeasureId(line.getUnitOfMeasureId())
                            .unitOfMeasureAbbreviation(unit == null ? null : unit.getAbbreviation())
                            .conversionFactor(conversionFactor)
                            .quantityReceived(line.getQuantityReceived())
                            .baseQuantityReceived(line.getQuantityReceived().multiply(conversionFactor))
                            .binId(line.getBinId())
                            .binName(bin == null ? null : bin.getName())
                            .batchId(line.getBatchId())
                            .batchNumber(batch == null ? null : batch.getBatchNumber())
                            .expirationDate(batch == null ? null : batch.getExpirationDate())
                            .serialNumbers(serialsByLineId.getOrDefault(line.getId(), List.of()))
                            .qualityStatus(line.getQualityStatus().name())
                            .qualityNotes(line.getQualityNotes())
                            .qualityCheckedBy(line.getQualityCheckedBy())
                            .qualityCheckedAt(line.getQualityCheckedAt())
                            .build();
                })
                .toList();

        return GoodsReceiptResponse.builder()
                .id(receipt.getId())
                .companyId(receipt.getCompanyId())
                .purchaseOrderId(receipt.getPurchaseOrderId())
                .poNumber(poNumber)
                .warehouseId(receipt.getWarehouseId())
                .warehouseName(warehouseName)
                .receiptNumber(receipt.getReceiptNumber())
                .receiptDate(receipt.getReceiptDate())
                .status(receipt.getStatus().name())
                .notes(receipt.getNotes())
                .createdBy(receipt.getCreatedBy())
                .lines(lineResponses)
                .build();
    }
}
