package com.example.erp.service.impl;

import com.example.erp.dto.CreateStockAdjustmentRequest;
import com.example.erp.dto.CreateStockCountRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.StockAdjustmentLineRequest;
import com.example.erp.dto.StockAdjustmentResponse;
import com.example.erp.dto.StockCountFilterRequest;
import com.example.erp.dto.StockCountLineCountRequest;
import com.example.erp.dto.StockCountLineRequest;
import com.example.erp.dto.StockCountLineResponse;
import com.example.erp.dto.StockCountResponse;
import com.example.erp.dto.SubmitStockCountRequest;
import com.example.erp.entity.Company;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductTrackingType;
import com.example.erp.entity.ProductUom;
import com.example.erp.entity.StockAdjustmentReason;
import com.example.erp.entity.StockCount;
import com.example.erp.entity.StockCountLine;
import com.example.erp.entity.StockCountStatus;
import com.example.erp.entity.StockLevel;
import com.example.erp.entity.UnitOfMeasure;
import com.example.erp.entity.Warehouse;
import com.example.erp.entity.WarehouseBin;
import com.example.erp.entity.WarehouseZone;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.ProductUomRepository;
import com.example.erp.repository.StockCountLineRepository;
import com.example.erp.repository.StockCountRepository;
import com.example.erp.repository.StockLevelRepository;
import com.example.erp.repository.UnitOfMeasureRepository;
import com.example.erp.repository.WarehouseBinRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.repository.WarehouseZoneRepository;
import com.example.erp.service.StockAdjustmentService;
import com.example.erp.service.StockCountService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class StockCountServiceImpl implements StockCountService {

    private final StockCountRepository stockCountRepository;
    private final StockCountLineRepository stockCountLineRepository;
    private final CompanyRepository companyRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseZoneRepository warehouseZoneRepository;
    private final WarehouseBinRepository warehouseBinRepository;
    private final ProductRepository productRepository;
    private final ProductUomRepository productUomRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final StockLevelRepository stockLevelRepository;
    private final StockAdjustmentService stockAdjustmentService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockCountResponse> listStockCounts(StockCountFilterRequest filter) {
        List<Specification<StockCount>> conditions = new ArrayList<>();
        if (filter.getCountNumber() != null && !filter.getCountNumber().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("countNumber")), "%" + filter.getCountNumber().toLowerCase() + "%"));
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
        Specification<StockCount> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<StockCount> page = stockCountRepository.findAll(spec, pageable);
        List<StockCount> content = page.getContent();

        Map<Long, String> warehouseNames = warehouseRepository.findAllById(
                content.stream().map(StockCount::getWarehouseId).distinct().toList()
        ).stream().collect(Collectors.toMap(Warehouse::getId, Warehouse::getName));

        return PageResponse.of(page.map(c -> toSummaryResponse(c, warehouseNames.get(c.getWarehouseId()))));
    }

    @Override
    public StockCountResponse getStockCount(Long id) {
        StockCount count = find(id);
        return toFullResponse(count, stockCountLineRepository.findByStockCountId(id));
    }

    @Override
    @Transactional
    public StockCountResponse createStockCount(CreateStockCountRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());
        requireWarehouse(request.getWarehouseId(), request.getCompanyId());

        Map<Long, UnitSelection> unitsByLine = new HashMap<>();
        for (StockCountLineRequest lineRequest : request.getLines()) {
            Product product = productRepository.findById(lineRequest.getProductId())
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Product not found with id: " + lineRequest.getProductId()));
            if (!product.getCompanyId().equals(request.getCompanyId())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Product does not belong to the selected company: " + product.getName());
            }
            unitsByLine.put(lineRequest.getProductId(), resolveLineUnit(product, lineRequest.getUnitOfMeasureId()));
            ProductTrackingType trackingType = product.getTrackingType() == null ? ProductTrackingType.NONE : product.getTrackingType();
            if (trackingType != ProductTrackingType.NONE) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Stock counts only support untracked products — " + product.getName() + " is " + trackingType + "-tracked");
            }
            if (lineRequest.getBinId() != null) {
                requireBinInWarehouse(lineRequest.getBinId(), request.getWarehouseId());
            }
        }

        StockCount count = StockCount.builder()
                .companyId(request.getCompanyId())
                .warehouseId(request.getWarehouseId())
                .countDate(request.getCountDate())
                .notes(request.getNotes())
                .countedBy(actingUsername)
                .build();
        stockCountRepository.save(count);
        count.setCountNumber("SC-" + String.format("%06d", count.getId()));
        stockCountRepository.save(count);

        List<StockCountLine> lines = request.getLines().stream()
                .map(r -> {
                    UnitSelection unit = unitsByLine.get(r.getProductId());
                    return StockCountLine.builder()
                            .stockCountId(count.getId())
                            .productId(r.getProductId())
                            .binId(r.getBinId())
                            .unitOfMeasureId(unit.unitOfMeasureId())
                            .conversionFactor(unit.conversionFactor())
                            .systemQuantity(availableQuantity(r.getProductId(), request.getWarehouseId(), r.getBinId()))
                            .build();
                })
                .toList();
        lines = stockCountLineRepository.saveAll(lines);

        return toFullResponse(count, lines);
    }

    @Override
    @Transactional
    public StockCountResponse submitCounts(Long id, SubmitStockCountRequest request) {
        StockCount count = find(id);
        if (count.getStatus() != StockCountStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft counts can have quantities entered");
        }
        Map<Long, StockCountLine> linesById = stockCountLineRepository.findByStockCountId(id).stream()
                .collect(Collectors.toMap(StockCountLine::getId, l -> l));

        for (StockCountLineCountRequest lineRequest : request.getLines()) {
            StockCountLine line = linesById.get(lineRequest.getLineId());
            if (line == null) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Line not found on this count: " + lineRequest.getLineId());
            }
            // The submitted figure is in the line's unit ("3 cases"); everything
            // stored is in base units, so convert on the way in. Multiplication
            // only — exact, unlike converting systemQuantity the other way.
            BigDecimal countedInBaseUnits = lineRequest.getCountedQuantity().multiply(conversionFactor(line));
            line.setCountedQuantity(countedInBaseUnits);
            line.setVarianceQuantity(countedInBaseUnits.subtract(line.getSystemQuantity()));
            stockCountLineRepository.save(line);
        }

        return toFullResponse(count, stockCountLineRepository.findByStockCountId(id));
    }

    @Override
    @Transactional
    public StockCountResponse completeStockCount(Long id) {
        StockCount count = find(id);
        if (count.getStatus() != StockCountStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft counts can be completed");
        }
        List<StockCountLine> lines = stockCountLineRepository.findByStockCountId(id);
        if (lines.stream().anyMatch(l -> l.getCountedQuantity() == null)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Every line needs a counted quantity before this count can be completed");
        }
        count.setStatus(StockCountStatus.COMPLETED);
        stockCountRepository.save(count);
        return toFullResponse(count, lines);
    }

    @Override
    @Transactional
    public StockCountResponse reconcileStockCount(Long id, String actingUsername) {
        StockCount count = find(id);
        if (count.getStatus() != StockCountStatus.COMPLETED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only completed counts can be reconciled");
        }
        List<StockCountLine> lines = stockCountLineRepository.findByStockCountId(id);

        List<StockAdjustmentLineRequest> adjustmentLines = new ArrayList<>();
        for (StockCountLine line : lines) {
            if (line.getVarianceQuantity() == null || line.getVarianceQuantity().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            StockAdjustmentLineRequest adjustmentLine = new StockAdjustmentLineRequest();
            adjustmentLine.setProductId(line.getProductId());
            adjustmentLine.setBinId(line.getBinId());
            boolean isIncrease = line.getVarianceQuantity().compareTo(BigDecimal.ZERO) > 0;
            adjustmentLine.setReason(isIncrease ? StockAdjustmentReason.STOCK_INCREASE : StockAdjustmentReason.STOCK_DECREASE);
            adjustmentLine.setQuantity(line.getVarianceQuantity().abs());
            adjustmentLines.add(adjustmentLine);
        }

        if (!adjustmentLines.isEmpty()) {
            CreateStockAdjustmentRequest adjustmentRequest = new CreateStockAdjustmentRequest();
            adjustmentRequest.setCompanyId(count.getCompanyId());
            adjustmentRequest.setWarehouseId(count.getWarehouseId());
            adjustmentRequest.setAdjustmentDate(count.getCountDate());
            adjustmentRequest.setNotes("Reconciliation of stock count " + count.getCountNumber());
            adjustmentRequest.setLines(adjustmentLines);
            StockAdjustmentResponse adjustment = stockAdjustmentService.createStockAdjustment(adjustmentRequest, actingUsername);
            count.setAdjustmentId(adjustment.getId());
        }

        count.setStatus(StockCountStatus.RECONCILED);
        stockCountRepository.save(count);
        return toFullResponse(count, lines);
    }

    @Override
    @Transactional
    public void deleteStockCount(Long id) {
        StockCount count = find(id);
        if (count.getStatus() != StockCountStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft counts can be deleted");
        }
        stockCountLineRepository.deleteByStockCountId(id);
        stockCountRepository.deleteById(id);
    }

    // When binId is null, the count line isn't pinned to one bin — sum every
    // bin (plus the unbinned row, if any) this product holds at the
    // warehouse. Without this, a binned product snapshots systemQuantity as
    // zero, and the physical count then reconciles into a phantom
    // STOCK_INCREASE that duplicates stock instead of correcting it.
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

    // The product's own base unit always works, factor 1 — no ProductUom row
    // is required for it. Any other unit must already be registered as an
    // inventory-allowed ProductUom for this product. Mirrors
    // StockTransferServiceImpl.resolveLineUnit: both are internal stock
    // documents, so both gate on allowInventory rather than purchase/sale.
    private record UnitSelection(Long unitOfMeasureId, BigDecimal conversionFactor) {}

    private UnitSelection resolveLineUnit(Product product, Long requestedUnitOfMeasureId) {
        Long unitId = requestedUnitOfMeasureId != null ? requestedUnitOfMeasureId : product.getUnitOfMeasureId();
        if (unitId.equals(product.getUnitOfMeasureId())) {
            return new UnitSelection(unitId, BigDecimal.ONE);
        }
        ProductUom productUom = productUomRepository.findByProductIdAndVariantIdIsNullAndUnitOfMeasureId(product.getId(), unitId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST,
                        "This unit is not configured for " + product.getName() + " — add it under the product's UOMs first"));
        if (!productUom.isAllowInventory()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "This unit is not allowed for stock counts of " + product.getName());
        }
        return new UnitSelection(unitId, productUom.getConversionFactor());
    }

    // Null on rows written before stock counts had units — those were always
    // entered in base units, so one-for-one is the right reading.
    private static BigDecimal conversionFactor(StockCountLine line) {
        return line.getConversionFactor() != null ? line.getConversionFactor() : BigDecimal.ONE;
    }

    // Base units -> the line's unit, for display on the count sheet. Never
    // fed back into a stored quantity: 37 bottles at 12 to the case is
    // 3.0833 cases, and only the base figure stays exact.
    private static BigDecimal inLineUnit(StockCountLine line, BigDecimal baseQuantity) {
        if (baseQuantity == null) {
            return null;
        }
        BigDecimal factor = conversionFactor(line);
        if (factor.compareTo(BigDecimal.ZERO) == 0) {
            return baseQuantity;
        }
        return baseQuantity.divide(factor, 4, RoundingMode.HALF_UP);
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

    private StockCount find(Long id) {
        return stockCountRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Stock count not found with id: " + id));
    }

    private StockCountResponse toSummaryResponse(StockCount count, String warehouseName) {
        return baseResponseBuilder(count, warehouseName).lines(null).build();
    }

    private StockCountResponse toFullResponse(StockCount count, List<StockCountLine> lines) {
        String warehouseName = warehouseRepository.findById(count.getWarehouseId()).map(Warehouse::getName).orElse(null);

        Map<Long, Product> products = lines.isEmpty() ? Map.of() : productRepository.findAllById(
                lines.stream().map(StockCountLine::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, WarehouseBin> bins = lines.isEmpty() ? Map.of() : warehouseBinRepository.findAllById(
                lines.stream().map(StockCountLine::getBinId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(WarehouseBin::getId, b -> b));
        // Both the line's own unit and each product's base unit — the count
        // sheet shows quantities in the first and the variance in the second.
        List<Long> unitIds = Stream.concat(
                lines.stream().map(StockCountLine::getUnitOfMeasureId),
                products.values().stream().map(Product::getUnitOfMeasureId)
        ).filter(Objects::nonNull).distinct().toList();
        Map<Long, UnitOfMeasure> units = unitIds.isEmpty() ? Map.of() : unitOfMeasureRepository.findAllById(unitIds)
                .stream().collect(Collectors.toMap(UnitOfMeasure::getId, u -> u));

        List<StockCountLineResponse> lineResponses = lines.stream()
                .map(line -> {
                    Product product = products.get(line.getProductId());
                    WarehouseBin bin = line.getBinId() == null ? null : bins.get(line.getBinId());
                    UnitOfMeasure unit = line.getUnitOfMeasureId() == null ? null : units.get(line.getUnitOfMeasureId());
                    UnitOfMeasure baseUnit = product == null || product.getUnitOfMeasureId() == null
                            ? null : units.get(product.getUnitOfMeasureId());
                    return StockCountLineResponse.builder()
                            .id(line.getId())
                            .productId(line.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productSku(product == null ? null : product.getSku())
                            .binId(line.getBinId())
                            .binName(bin == null ? null : bin.getName())
                            .unitOfMeasureId(line.getUnitOfMeasureId())
                            .unitOfMeasureAbbreviation(unit == null ? null : unit.getAbbreviation())
                            .conversionFactor(conversionFactor(line))
                            .baseUnitOfMeasureAbbreviation(baseUnit == null ? null : baseUnit.getAbbreviation())
                            .systemQuantity(line.getSystemQuantity())
                            .countedQuantity(line.getCountedQuantity())
                            .varianceQuantity(line.getVarianceQuantity())
                            .systemQuantityInUnit(inLineUnit(line, line.getSystemQuantity()))
                            .countedQuantityInUnit(inLineUnit(line, line.getCountedQuantity()))
                            .build();
                })
                .toList();

        return baseResponseBuilder(count, warehouseName)
                .lines(lineResponses)
                .build();
    }

    private StockCountResponse.StockCountResponseBuilder baseResponseBuilder(StockCount count, String warehouseName) {
        String adjustmentNumber = null;
        if (count.getAdjustmentId() != null) {
            adjustmentNumber = stockAdjustmentService.getStockAdjustment(count.getAdjustmentId()).getAdjustmentNumber();
        }
        return StockCountResponse.builder()
                .id(count.getId())
                .companyId(count.getCompanyId())
                .warehouseId(count.getWarehouseId())
                .warehouseName(warehouseName)
                .countNumber(count.getCountNumber())
                .countDate(count.getCountDate())
                .status(count.getStatus().name())
                .notes(count.getNotes())
                .countedBy(count.getCountedBy())
                .adjustmentId(count.getAdjustmentId())
                .adjustmentNumber(adjustmentNumber);
    }
}
