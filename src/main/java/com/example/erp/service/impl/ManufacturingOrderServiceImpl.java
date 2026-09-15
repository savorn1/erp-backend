package com.example.erp.service.impl;

import com.example.erp.dto.CompleteManufacturingOrderRequest;
import com.example.erp.dto.CreateManufacturingOrderRequest;
import com.example.erp.dto.ManufacturingOrderFilterRequest;
import com.example.erp.dto.ManufacturingOrderMaterialResponse;
import com.example.erp.dto.ManufacturingOrderResponse;
import com.example.erp.dto.MaterialAvailabilityRowResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.QualityCheckRequest;
import com.example.erp.dto.UpdateManufacturingOrderRequest;
import com.example.erp.dto.WorkOrderResponse;
import com.example.erp.entity.BillOfMaterial;
import com.example.erp.entity.BillOfMaterialLine;
import com.example.erp.entity.BillOfMaterialStatus;
import com.example.erp.entity.Company;
import com.example.erp.entity.Machine;
import com.example.erp.entity.ManufacturingOrder;
import com.example.erp.entity.ManufacturingOrderMaterial;
import com.example.erp.entity.ManufacturingOrderStatus;
import com.example.erp.entity.ManufacturingRejection;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductionPlan;
import com.example.erp.entity.ProductionPlanStatus;
import com.example.erp.entity.QualityCheckStatus;
import com.example.erp.entity.Routing;
import com.example.erp.entity.RoutingOperation;
import com.example.erp.entity.RoutingStatus;
import com.example.erp.entity.StockLevel;
import com.example.erp.entity.StockMovement;
import com.example.erp.entity.StockMovementType;
import com.example.erp.entity.UnitOfMeasure;
import com.example.erp.entity.Warehouse;
import com.example.erp.entity.WorkCenter;
import com.example.erp.entity.WorkOrder;
import com.example.erp.exception.AppException;
import com.example.erp.repository.BillOfMaterialLineRepository;
import com.example.erp.repository.BillOfMaterialRepository;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.MachineRepository;
import com.example.erp.repository.ManufacturingOrderMaterialRepository;
import com.example.erp.repository.ManufacturingOrderRepository;
import com.example.erp.repository.ManufacturingRejectionRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.ProductionPlanRepository;
import com.example.erp.repository.RoutingOperationRepository;
import com.example.erp.repository.RoutingRepository;
import com.example.erp.repository.StockLevelRepository;
import com.example.erp.repository.StockMovementRepository;
import com.example.erp.repository.UnitOfMeasureRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.repository.WorkCenterRepository;
import com.example.erp.repository.WorkOrderRepository;
import com.example.erp.service.ManufacturingOrderService;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManufacturingOrderServiceImpl implements ManufacturingOrderService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final ManufacturingOrderRepository orderRepository;
    private final ManufacturingOrderMaterialRepository materialRepository;
    private final BillOfMaterialRepository bomRepository;
    private final BillOfMaterialLineRepository bomLineRepository;
    private final CompanyRepository companyRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final StockLevelRepository stockLevelRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductionPlanRepository productionPlanRepository;
    private final ManufacturingRejectionRepository rejectionRepository;
    private final RoutingRepository routingRepository;
    private final RoutingOperationRepository routingOperationRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkCenterRepository workCenterRepository;
    private final MachineRepository machineRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ManufacturingOrderResponse> listOrders(ManufacturingOrderFilterRequest filter) {
        List<Specification<ManufacturingOrder>> conditions = new ArrayList<>();
        if (filter.getMoNumber() != null && !filter.getMoNumber().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("moNumber")), "%" + filter.getMoNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getWarehouseId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("warehouseId"), filter.getWarehouseId()));
        }
        if (filter.getProductId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("productId"), filter.getProductId()));
        }
        if (filter.getProductionPlanId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("productionPlanId"), filter.getProductionPlanId()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        Specification<ManufacturingOrder> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<ManufacturingOrder> page = orderRepository.findAll(spec, pageable);
        List<ManufacturingOrder> content = page.getContent();

        List<Long> orderIds = content.stream().map(ManufacturingOrder::getId).toList();
        Map<Long, List<ManufacturingOrderMaterial>> materialsByOrderId = orderIds.isEmpty() ? Map.of() : allMaterialsGroupedByOrder(orderIds);

        return PageResponse.of(page.map(mo -> toFullResponse(mo, materialsByOrderId.getOrDefault(mo.getId(), List.of()))));
    }

    @Override
    @Transactional(readOnly = true)
    public ManufacturingOrderResponse getOrder(Long id) {
        ManufacturingOrder mo = find(id);
        return toFullResponse(mo, materialRepository.findByManufacturingOrderId(id));
    }

    @Override
    @Transactional
    public ManufacturingOrderResponse createOrder(CreateManufacturingOrderRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());
        requireWarehouse(request.getWarehouseId(), request.getCompanyId());
        BillOfMaterial bom = requireActiveBom(request.getBomId(), request.getCompanyId());
        if (request.getProductionPlanId() != null) {
            requireOpenProductionPlan(request.getProductionPlanId(), request.getCompanyId());
        }

        ManufacturingOrder mo = ManufacturingOrder.builder()
                .companyId(request.getCompanyId())
                .bomId(bom.getId())
                .productId(bom.getProductId())
                .warehouseId(request.getWarehouseId())
                .productionPlanId(request.getProductionPlanId())
                .plannedQuantity(request.getPlannedQuantity())
                .plannedStartDate(request.getPlannedStartDate())
                .plannedEndDate(request.getPlannedEndDate())
                .notes(request.getNotes())
                .createdBy(actingUsername)
                .build();
        orderRepository.save(mo);
        mo.setMoNumber("MO-" + String.format("%06d", mo.getId()));
        orderRepository.save(mo);

        List<ManufacturingOrderMaterial> materials = generateMaterials(mo, bom);
        generateWorkOrders(mo, bom);
        return toFullResponse(mo, materials);
    }

    @Override
    @Transactional
    public ManufacturingOrderResponse updateOrder(Long id, UpdateManufacturingOrderRequest request) {
        ManufacturingOrder mo = find(id);
        if (mo.getStatus() != ManufacturingOrderStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft manufacturing orders can be edited");
        }
        requireWarehouse(request.getWarehouseId(), mo.getCompanyId());
        if (request.getProductionPlanId() != null) {
            requireOpenProductionPlan(request.getProductionPlanId(), mo.getCompanyId());
        }

        mo.setWarehouseId(request.getWarehouseId());
        mo.setProductionPlanId(request.getProductionPlanId());
        mo.setPlannedQuantity(request.getPlannedQuantity());
        mo.setPlannedStartDate(request.getPlannedStartDate());
        mo.setPlannedEndDate(request.getPlannedEndDate());
        mo.setNotes(request.getNotes());
        orderRepository.save(mo);

        BillOfMaterial bom = bomRepository.findById(mo.getBomId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Bill of materials not found with id: " + mo.getBomId()));
        materialRepository.deleteByManufacturingOrderId(id);
        List<ManufacturingOrderMaterial> materials = generateMaterials(mo, bom);
        return toFullResponse(mo, materials);
    }

    @Override
    @Transactional
    public ManufacturingOrderResponse releaseOrder(Long id) {
        ManufacturingOrder mo = find(id);
        if (mo.getStatus() != ManufacturingOrderStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft manufacturing orders can be released");
        }
        mo.setStatus(ManufacturingOrderStatus.RELEASED);
        orderRepository.save(mo);
        return toFullResponse(mo, materialRepository.findByManufacturingOrderId(id));
    }

    @Override
    @Transactional
    public ManufacturingOrderResponse startProduction(Long id, String actingUsername) {
        ManufacturingOrder mo = find(id);
        if (mo.getStatus() != ManufacturingOrderStatus.RELEASED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only released manufacturing orders can start production");
        }
        List<ManufacturingOrderMaterial> materials = materialRepository.findByManufacturingOrderId(id);

        // Every line is checked for availability before anything is written,
        // so a start is either fully applied or rejected outright — mirrors
        // StockAdjustmentServiceImpl's approve validation shape.
        Map<Long, Product> components = productRepository.findAllById(
                materials.stream().map(ManufacturingOrderMaterial::getComponentProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));
        for (ManufacturingOrderMaterial material : materials) {
            BigDecimal available = availableQuantity(material.getComponentProductId(), mo.getWarehouseId());
            if (available.compareTo(material.getRequiredQuantity()) < 0) {
                Product product = components.get(material.getComponentProductId());
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Insufficient stock for " + (product == null ? material.getComponentProductId() : product.getName())
                                + " — need " + material.getRequiredQuantity() + ", have " + available);
            }
        }

        BigDecimal materialCost = BigDecimal.ZERO;
        for (ManufacturingOrderMaterial material : materials) {
            decreaseStock(material.getComponentProductId(), mo.getWarehouseId(), material.getRequiredQuantity());
            stockMovementRepository.save(StockMovement.builder()
                    .companyId(mo.getCompanyId())
                    .productId(material.getComponentProductId())
                    .warehouseId(mo.getWarehouseId())
                    .type(StockMovementType.MATERIAL_CONSUMPTION)
                    .quantityDelta(material.getRequiredQuantity().negate())
                    .referenceType("MANUFACTURING_ORDER")
                    .referenceId(mo.getId())
                    .createdBy(actingUsername)
                    .build());

            material.setConsumedQuantity(material.getRequiredQuantity());
            material.setLineCost(material.getRequiredQuantity().multiply(material.getUnitCost()));
            materialRepository.save(material);
            materialCost = materialCost.add(material.getLineCost());
        }

        mo.setMaterialCost(materialCost);
        mo.setActualStartDate(LocalDateTime.now());
        mo.setStatus(ManufacturingOrderStatus.IN_PROGRESS);
        orderRepository.save(mo);
        return toFullResponse(mo, materials);
    }

    @Override
    @Transactional
    public ManufacturingOrderResponse completeProduction(Long id, CompleteManufacturingOrderRequest request, String actingUsername) {
        ManufacturingOrder mo = find(id);
        if (mo.getStatus() != ManufacturingOrderStatus.IN_PROGRESS) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only in-progress manufacturing orders can be completed");
        }
        BigDecimal producedQuantity = request.getProducedQuantity();
        BigDecimal scrapQuantity = request.getScrapQuantity() == null ? BigDecimal.ZERO : request.getScrapQuantity();
        if (producedQuantity.signum() == 0 && scrapQuantity.signum() == 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Produced quantity and scrap quantity cannot both be zero");
        }

        // Finished goods aren't posted to stock yet — that only happens once
        // quality control passes them (see recordQualityCheck). This just
        // records what came off the line.
        BigDecimal laborCost = request.getLaborCost() == null ? BigDecimal.ZERO : request.getLaborCost();
        BigDecimal overheadCost = request.getOverheadCost() == null ? BigDecimal.ZERO : request.getOverheadCost();
        BigDecimal totalCost = mo.getMaterialCost().add(laborCost).add(overheadCost);
        BigDecimal unitCost = producedQuantity.signum() > 0
                ? totalCost.divide(producedQuantity, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        mo.setProducedQuantity(producedQuantity);
        mo.setScrapQuantity(scrapQuantity);
        mo.setScrapReason(request.getScrapReason());
        mo.setLaborCost(laborCost);
        mo.setOverheadCost(overheadCost);
        mo.setTotalCost(totalCost);
        mo.setUnitCost(unitCost);
        mo.setActualEndDate(LocalDateTime.now());
        mo.setStatus(ManufacturingOrderStatus.PENDING_QC);
        orderRepository.save(mo);
        return toFullResponse(mo, materialRepository.findByManufacturingOrderId(id));
    }

    @Override
    @Transactional
    public ManufacturingOrderResponse recordQualityCheck(Long id, QualityCheckRequest request, String actingUsername) {
        ManufacturingOrder mo = find(id);
        if (mo.getStatus() != ManufacturingOrderStatus.PENDING_QC) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only orders pending quality control can be inspected");
        }
        if (request.getStatus() == QualityCheckStatus.PENDING) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Quality check status must be PASSED or FAILED");
        }

        if (request.getStatus() == QualityCheckStatus.PASSED) {
            if (mo.getProducedQuantity().signum() > 0) {
                increaseStock(mo.getCompanyId(), mo.getProductId(), mo.getWarehouseId(), mo.getProducedQuantity());
                stockMovementRepository.save(StockMovement.builder()
                        .companyId(mo.getCompanyId())
                        .productId(mo.getProductId())
                        .warehouseId(mo.getWarehouseId())
                        .type(StockMovementType.PRODUCTION_OUTPUT)
                        .quantityDelta(mo.getProducedQuantity())
                        .referenceType("MANUFACTURING_ORDER")
                        .referenceId(mo.getId())
                        .createdBy(actingUsername)
                        .build());
            }
        } else {
            // Rejected output is treated as additional scrap — it never
            // enters stock, mirroring how a failed GoodsReceiptLine never
            // advances quantityReceived. A ManufacturingRejection row is
            // also kept, distinct from scrapQuantity/scrapReason, so a
            // QC-driven rejection has its own reason and audit trail
            // separate from routine production-floor scrap.
            if (mo.getProducedQuantity().signum() > 0) {
                rejectionRepository.save(ManufacturingRejection.builder()
                        .manufacturingOrderId(mo.getId())
                        .productId(mo.getProductId())
                        .quantity(mo.getProducedQuantity())
                        .reason(request.getNotes())
                        .rejectedBy(actingUsername)
                        .build());
            }
            mo.setScrapQuantity(mo.getScrapQuantity().add(mo.getProducedQuantity()));
            mo.setProducedQuantity(BigDecimal.ZERO);
            mo.setUnitCost(BigDecimal.ZERO);
        }

        mo.setQualityStatus(request.getStatus());
        mo.setQualityNotes(request.getNotes());
        mo.setQualityCheckedBy(actingUsername);
        mo.setQualityCheckedAt(LocalDateTime.now());
        mo.setStatus(ManufacturingOrderStatus.COMPLETED);
        orderRepository.save(mo);
        return toFullResponse(mo, materialRepository.findByManufacturingOrderId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialAvailabilityRowResponse> checkMaterialAvailability(Long id) {
        ManufacturingOrder mo = find(id);
        List<ManufacturingOrderMaterial> materials = materialRepository.findByManufacturingOrderId(id);

        Map<Long, Product> components = productRepository.findAllById(
                materials.stream().map(ManufacturingOrderMaterial::getComponentProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, UnitOfMeasure> units = unitOfMeasureRepository.findAllById(
                components.values().stream().map(Product::getUnitOfMeasureId).distinct().toList()
        ).stream().collect(Collectors.toMap(UnitOfMeasure::getId, u -> u));

        return materials.stream()
                .map(material -> {
                    Product component = components.get(material.getComponentProductId());
                    UnitOfMeasure unit = component == null ? null : units.get(component.getUnitOfMeasureId());
                    BigDecimal available = availableQuantity(material.getComponentProductId(), mo.getWarehouseId());
                    BigDecimal shortfall = material.getRequiredQuantity().subtract(available);
                    if (shortfall.signum() < 0) shortfall = BigDecimal.ZERO;
                    return MaterialAvailabilityRowResponse.builder()
                            .componentProductId(material.getComponentProductId())
                            .componentProductName(component == null ? null : component.getName())
                            .componentProductSku(component == null ? null : component.getSku())
                            .unitOfMeasureId(component == null ? null : component.getUnitOfMeasureId())
                            .unitOfMeasureAbbreviation(unit == null ? null : unit.getAbbreviation())
                            .requiredQuantity(material.getRequiredQuantity())
                            .availableQuantity(available)
                            .shortfallQuantity(shortfall)
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional
    public ManufacturingOrderResponse cancelOrder(Long id) {
        ManufacturingOrder mo = find(id);
        if (mo.getStatus() != ManufacturingOrderStatus.DRAFT && mo.getStatus() != ManufacturingOrderStatus.RELEASED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft or released manufacturing orders can be cancelled — materials have already been consumed");
        }
        mo.setStatus(ManufacturingOrderStatus.CANCELLED);
        orderRepository.save(mo);
        return toFullResponse(mo, materialRepository.findByManufacturingOrderId(id));
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        ManufacturingOrder mo = find(id);
        if (mo.getStatus() != ManufacturingOrderStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft manufacturing orders can be deleted");
        }
        materialRepository.deleteByManufacturingOrderId(id);
        workOrderRepository.deleteByManufacturingOrderId(id);
        orderRepository.delete(mo);
    }

    // Additive, optional layer — see WorkOrder's own comment. Picks the
    // BOM's first ACTIVE routing (if any) and clones its operations into
    // this order's WorkOrders; a BOM with no active routing produces an
    // order with none.
    private void generateWorkOrders(ManufacturingOrder mo, BillOfMaterial bom) {
        Routing routing = routingRepository.findAll((root, query, cb) ->
                        cb.and(cb.equal(root.get("bomId"), bom.getId()), cb.equal(root.get("status"), RoutingStatus.ACTIVE)))
                .stream().findFirst().orElse(null);
        if (routing == null) return;

        List<RoutingOperation> operations = routingOperationRepository.findByRoutingIdOrderBySequenceNumberAsc(routing.getId());
        List<WorkOrder> workOrders = operations.stream()
                .map(op -> WorkOrder.builder()
                        .manufacturingOrderId(mo.getId())
                        .routingOperationId(op.getId())
                        .sequenceNumber(op.getSequenceNumber())
                        .name(op.getName())
                        .workCenterId(op.getWorkCenterId())
                        .machineId(op.getMachineId())
                        .standardTimeMinutes(op.getStandardTimeMinutes())
                        .build())
                .toList();
        workOrderRepository.saveAll(workOrders);
    }

    private List<ManufacturingOrderMaterial> generateMaterials(ManufacturingOrder mo, BillOfMaterial bom) {
        List<BillOfMaterialLine> bomLines = bomLineRepository.findByBomId(bom.getId());
        BigDecimal scaleFactor = mo.getPlannedQuantity().divide(bom.getOutputQuantity(), 8, RoundingMode.HALF_UP);

        Map<Long, Product> components = productRepository.findAllById(
                bomLines.stream().map(BillOfMaterialLine::getComponentProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<ManufacturingOrderMaterial> materials = bomLines.stream()
                .map(line -> {
                    BigDecimal baseRequired = line.getQuantity().multiply(scaleFactor);
                    BigDecimal scrapMultiplier = BigDecimal.ONE.add(
                            (line.getScrapPercent() == null ? BigDecimal.ZERO : line.getScrapPercent()).divide(HUNDRED, 8, RoundingMode.HALF_UP));
                    BigDecimal requiredQuantity = baseRequired.multiply(scrapMultiplier).setScale(4, RoundingMode.HALF_UP);
                    Product component = components.get(line.getComponentProductId());
                    BigDecimal unitCost = component == null ? BigDecimal.ZERO : component.getCostPrice();
                    return ManufacturingOrderMaterial.builder()
                            .manufacturingOrderId(mo.getId())
                            .componentProductId(line.getComponentProductId())
                            .requiredQuantity(requiredQuantity)
                            .unitCost(unitCost)
                            .build();
                })
                .toList();
        return materialRepository.saveAll(materials);
    }

    private Map<Long, List<ManufacturingOrderMaterial>> allMaterialsGroupedByOrder(List<Long> orderIds) {
        List<ManufacturingOrderMaterial> all = new ArrayList<>();
        for (Long orderId : orderIds) {
            all.addAll(materialRepository.findByManufacturingOrderId(orderId));
        }
        return all.stream().collect(Collectors.groupingBy(ManufacturingOrderMaterial::getManufacturingOrderId));
    }

    // Material lines don't pin a bin — components are just as likely to have
    // been received into one via GoodsReceipt, so this sums every bin (plus
    // the unbinned row, if any) rather than only the unbinned row. Without
    // this, any binned component looked permanently out of stock here.
    private BigDecimal availableQuantity(Long productId, Long warehouseId) {
        return stockLevelRepository.findByProductIdAndWarehouseId(productId, warehouseId).stream()
                .map(StockLevel::getQuantityOnHand)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Mirrors availableQuantity's aggregate semantics: pulls from whichever
    // bin(s) actually hold the component, largest first, until satisfied.
    private void decreaseStock(Long productId, Long warehouseId, BigDecimal quantity) {
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
            throw new AppException(HttpStatus.BAD_REQUEST, "Insufficient stock on hand for this component at the selected warehouse");
        }
    }

    private void increaseStock(Long companyId, Long productId, Long warehouseId, BigDecimal quantity) {
        StockLevel stockLevel = stockLevelRepository.findByProductIdAndWarehouseIdAndBinIdIsNull(productId, warehouseId)
                .orElseGet(() -> StockLevel.builder()
                        .companyId(companyId)
                        .productId(productId)
                        .warehouseId(warehouseId)
                        .build());
        stockLevel.setQuantityOnHand(stockLevel.getQuantityOnHand().add(quantity));
        stockLevelRepository.save(stockLevel);
    }

    private ManufacturingOrderResponse toFullResponse(ManufacturingOrder mo, List<ManufacturingOrderMaterial> materials) {
        Company company = companyRepository.findById(mo.getCompanyId()).orElse(null);
        BillOfMaterial bom = bomRepository.findById(mo.getBomId()).orElse(null);
        Product product = productRepository.findById(mo.getProductId()).orElse(null);
        Warehouse warehouse = warehouseRepository.findById(mo.getWarehouseId()).orElse(null);
        ProductionPlan plan = mo.getProductionPlanId() == null ? null : productionPlanRepository.findById(mo.getProductionPlanId()).orElse(null);

        Map<Long, Product> components = productRepository.findAllById(
                materials.stream().map(ManufacturingOrderMaterial::getComponentProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, UnitOfMeasure> units = unitOfMeasureRepository.findAllById(
                components.values().stream().map(Product::getUnitOfMeasureId).distinct().toList()
        ).stream().collect(Collectors.toMap(UnitOfMeasure::getId, u -> u));

        List<ManufacturingOrderMaterialResponse> materialResponses = materials.stream()
                .map(m -> {
                    Product component = components.get(m.getComponentProductId());
                    UnitOfMeasure unit = component == null ? null : units.get(component.getUnitOfMeasureId());
                    return ManufacturingOrderMaterialResponse.builder()
                            .id(m.getId())
                            .componentProductId(m.getComponentProductId())
                            .componentProductName(component == null ? null : component.getName())
                            .componentProductSku(component == null ? null : component.getSku())
                            .unitOfMeasureId(component == null ? null : component.getUnitOfMeasureId())
                            .unitOfMeasureAbbreviation(unit == null ? null : unit.getAbbreviation())
                            .requiredQuantity(m.getRequiredQuantity())
                            .consumedQuantity(m.getConsumedQuantity())
                            .unitCost(m.getUnitCost())
                            .lineCost(m.getLineCost())
                            .build();
                })
                .toList();

        List<WorkOrder> workOrders = workOrderRepository.findByManufacturingOrderIdOrderBySequenceNumberAsc(mo.getId());
        Map<Long, WorkCenter> workCenters = workCenterRepository.findAllById(
                workOrders.stream().map(WorkOrder::getWorkCenterId).distinct().toList()
        ).stream().collect(Collectors.toMap(WorkCenter::getId, w -> w));
        Map<Long, Machine> machines = machineRepository.findAllById(
                workOrders.stream().map(WorkOrder::getMachineId).filter(java.util.Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(Machine::getId, m -> m));
        List<WorkOrderResponse> workOrderResponses = workOrders.stream()
                .map(wo -> {
                    WorkCenter workCenter = workCenters.get(wo.getWorkCenterId());
                    Machine machine = wo.getMachineId() == null ? null : machines.get(wo.getMachineId());
                    return WorkOrderResponse.builder()
                            .id(wo.getId())
                            .manufacturingOrderId(wo.getManufacturingOrderId())
                            .moNumber(mo.getMoNumber())
                            .sequenceNumber(wo.getSequenceNumber())
                            .name(wo.getName())
                            .workCenterId(wo.getWorkCenterId())
                            .workCenterName(workCenter == null ? null : workCenter.getName())
                            .machineId(wo.getMachineId())
                            .machineName(machine == null ? null : machine.getName())
                            .standardTimeMinutes(wo.getStandardTimeMinutes())
                            .status(wo.getStatus().name())
                            .actualStartDate(wo.getActualStartDate())
                            .actualEndDate(wo.getActualEndDate())
                            .notes(wo.getNotes())
                            .build();
                })
                .toList();

        return ManufacturingOrderResponse.builder()
                .id(mo.getId())
                .companyId(mo.getCompanyId())
                .companyName(company == null ? null : company.getName())
                .bomId(mo.getBomId())
                .bomNumber(bom == null ? null : bom.getBomNumber())
                .productId(mo.getProductId())
                .productName(product == null ? null : product.getName())
                .productSku(product == null ? null : product.getSku())
                .warehouseId(mo.getWarehouseId())
                .warehouseName(warehouse == null ? null : warehouse.getName())
                .productionPlanId(mo.getProductionPlanId())
                .planNumber(plan == null ? null : plan.getPlanNumber())
                .moNumber(mo.getMoNumber())
                .plannedQuantity(mo.getPlannedQuantity())
                .producedQuantity(mo.getProducedQuantity())
                .scrapQuantity(mo.getScrapQuantity())
                .scrapReason(mo.getScrapReason())
                .status(mo.getStatus().name())
                .qualityStatus(mo.getQualityStatus() == null ? null : mo.getQualityStatus().name())
                .qualityNotes(mo.getQualityNotes())
                .qualityCheckedBy(mo.getQualityCheckedBy())
                .qualityCheckedAt(mo.getQualityCheckedAt())
                .plannedStartDate(mo.getPlannedStartDate())
                .plannedEndDate(mo.getPlannedEndDate())
                .actualStartDate(mo.getActualStartDate())
                .actualEndDate(mo.getActualEndDate())
                .materialCost(mo.getMaterialCost())
                .laborCost(mo.getLaborCost())
                .overheadCost(mo.getOverheadCost())
                .totalCost(mo.getTotalCost())
                .unitCost(mo.getUnitCost())
                .notes(mo.getNotes())
                .createdBy(mo.getCreatedBy())
                .createdAt(mo.getCreatedAt())
                .materials(materialResponses)
                .workOrders(workOrderResponses)
                .build();
    }

    private ManufacturingOrder find(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Manufacturing order not found with id: " + id));
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

    private BillOfMaterial requireActiveBom(Long bomId, Long companyId) {
        BillOfMaterial bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Bill of materials not found with id: " + bomId));
        if (!bom.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Bill of materials does not belong to the selected company");
        }
        if (bom.getStatus() != BillOfMaterialStatus.ACTIVE) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only an active bill of materials can be used for a manufacturing order");
        }
        return bom;
    }

    private void requireOpenProductionPlan(Long productionPlanId, Long companyId) {
        ProductionPlan plan = productionPlanRepository.findById(productionPlanId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Production plan not found with id: " + productionPlanId));
        if (!plan.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Production plan does not belong to the selected company");
        }
        if (plan.getStatus() == ProductionPlanStatus.CLOSED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot add a manufacturing order to a closed production plan");
        }
    }
}
