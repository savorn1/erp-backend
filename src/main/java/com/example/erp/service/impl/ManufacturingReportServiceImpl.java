package com.example.erp.service.impl;

import com.example.erp.dto.BomComparisonLineResponse;
import com.example.erp.dto.BomComparisonResponse;
import com.example.erp.dto.BomComparisonSideResponse;
import com.example.erp.dto.BomCostResponse;
import com.example.erp.dto.BomCostRowResponse;
import com.example.erp.dto.CostVarianceResponse;
import com.example.erp.dto.CostVarianceRowResponse;
import com.example.erp.dto.ManufacturingCostResponse;
import com.example.erp.dto.ManufacturingCostRowResponse;
import com.example.erp.dto.MachineCostResponse;
import com.example.erp.dto.MachineCostRowResponse;
import com.example.erp.dto.MachineUtilizationResponse;
import com.example.erp.dto.MachineUtilizationRowResponse;
import com.example.erp.dto.ManufacturingProfitabilityResponse;
import com.example.erp.dto.ManufacturingProfitabilityRowResponse;
import com.example.erp.dto.ManufacturingReportFilterRequest;
import com.example.erp.dto.MaterialConsumptionResponse;
import com.example.erp.dto.MaterialConsumptionRowResponse;
import com.example.erp.dto.MaterialRequirementRowResponse;
import com.example.erp.dto.MaterialRequirementsResponse;
import com.example.erp.dto.MoSummaryResponse;
import com.example.erp.dto.MoSummaryRowResponse;
import com.example.erp.dto.OperationPerformanceResponse;
import com.example.erp.dto.OperationPerformanceRowResponse;
import com.example.erp.dto.PlanVsActualResponse;
import com.example.erp.dto.PlanVsActualRowResponse;
import com.example.erp.dto.ProductionOutputResponse;
import com.example.erp.dto.ProductionOutputRowResponse;
import com.example.erp.dto.ProductionTimeResponse;
import com.example.erp.dto.ProductionTimeRowResponse;
import com.example.erp.dto.ProductionTrendResponse;
import com.example.erp.dto.ProductionTrendRowResponse;
import com.example.erp.dto.QualityPassFailResponse;
import com.example.erp.dto.QualityPassFailRowResponse;
import com.example.erp.dto.RejectionRowResponse;
import com.example.erp.dto.RejectionsResponse;
import com.example.erp.dto.ScrapWastageResponse;
import com.example.erp.dto.ScrapWastageRowResponse;
import com.example.erp.dto.WorkCenterUtilizationResponse;
import com.example.erp.dto.WorkCenterUtilizationRowResponse;
import com.example.erp.entity.BillOfMaterial;
import com.example.erp.entity.BillOfMaterialLine;
import com.example.erp.entity.BillOfMaterialStatus;
import com.example.erp.entity.ManufacturingOrder;
import com.example.erp.entity.ManufacturingOrderMaterial;
import com.example.erp.entity.ManufacturingOrderStatus;
import com.example.erp.entity.ManufacturingRejection;
import com.example.erp.entity.Machine;
import com.example.erp.entity.Product;
import com.example.erp.entity.QualityCheckStatus;
import com.example.erp.entity.StockLevel;
import com.example.erp.entity.Warehouse;
import com.example.erp.entity.WorkCenter;
import com.example.erp.entity.WorkOrder;
import com.example.erp.entity.WorkOrderStatus;
import com.example.erp.exception.AppException;
import com.example.erp.repository.BillOfMaterialLineRepository;
import com.example.erp.repository.BillOfMaterialRepository;
import com.example.erp.repository.MachineRepository;
import com.example.erp.repository.ManufacturingOrderMaterialRepository;
import com.example.erp.repository.ManufacturingOrderRepository;
import com.example.erp.repository.ManufacturingRejectionRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.StockLevelRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.repository.WorkCenterRepository;
import com.example.erp.repository.WorkOrderRepository;
import com.example.erp.service.ManufacturingReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManufacturingReportServiceImpl implements ManufacturingReportService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final ManufacturingOrderRepository orderRepository;
    private final ManufacturingOrderMaterialRepository materialRepository;
    private final ManufacturingRejectionRepository rejectionRepository;
    private final ProductRepository productRepository;
    private final BillOfMaterialRepository bomRepository;
    private final BillOfMaterialLineRepository bomLineRepository;
    private final StockLevelRepository stockLevelRepository;
    private final WarehouseRepository warehouseRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkCenterRepository workCenterRepository;
    private final MachineRepository machineRepository;

    @Override
    @Transactional(readOnly = true)
    public MoSummaryResponse summary(ManufacturingReportFilterRequest filter) {
        List<ManufacturingOrder> orders = matchingOrders(filter, ManufacturingOrder::getCreatedAt);

        Map<ManufacturingOrderStatus, List<ManufacturingOrder>> byStatus = orders.stream()
                .collect(Collectors.groupingBy(ManufacturingOrder::getStatus));

        List<MoSummaryRowResponse> rows = new ArrayList<>();
        for (ManufacturingOrderStatus status : ManufacturingOrderStatus.values()) {
            List<ManufacturingOrder> statusOrders = byStatus.getOrDefault(status, List.of());
            if (statusOrders.isEmpty()) continue;
            rows.add(MoSummaryRowResponse.builder()
                    .status(status.name())
                    .orderCount(statusOrders.size())
                    .totalPlannedQuantity(sum(statusOrders, ManufacturingOrder::getPlannedQuantity))
                    .totalProducedQuantity(sum(statusOrders, ManufacturingOrder::getProducedQuantity))
                    .totalScrapQuantity(sum(statusOrders, ManufacturingOrder::getScrapQuantity))
                    .build());
        }

        return MoSummaryResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalOrders(orders.size())
                .totalPlannedQuantity(sum(orders, ManufacturingOrder::getPlannedQuantity))
                .totalProducedQuantity(sum(orders, ManufacturingOrder::getProducedQuantity))
                .totalScrapQuantity(sum(orders, ManufacturingOrder::getScrapQuantity))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialConsumptionResponse materialConsumption(ManufacturingReportFilterRequest filter) {
        List<ManufacturingOrder> orders = matchingOrders(filter, ManufacturingOrder::getActualStartDate).stream()
                .filter(mo -> mo.getActualStartDate() != null)
                .toList();

        Map<Long, BigDecimal[]> byComponent = new HashMap<>(); // [quantity, cost]
        for (ManufacturingOrder mo : orders) {
            for (ManufacturingOrderMaterial material : materialRepository.findByManufacturingOrderId(mo.getId())) {
                if (material.getConsumedQuantity().signum() == 0) continue;
                BigDecimal[] bucket = byComponent.computeIfAbsent(material.getComponentProductId(), id -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                bucket[0] = bucket[0].add(material.getConsumedQuantity());
                bucket[1] = bucket[1].add(material.getLineCost());
            }
        }

        Map<Long, Product> products = productRepository.findAllById(byComponent.keySet()).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<MaterialConsumptionRowResponse> rows = new ArrayList<>();
        BigDecimal totalCost = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal[]> entry : byComponent.entrySet()) {
            Product product = products.get(entry.getKey());
            rows.add(MaterialConsumptionRowResponse.builder()
                    .componentProductId(entry.getKey())
                    .componentProductName(product == null ? null : product.getName())
                    .componentProductSku(product == null ? null : product.getSku())
                    .totalConsumedQuantity(entry.getValue()[0])
                    .totalCost(entry.getValue()[1])
                    .build());
            totalCost = totalCost.add(entry.getValue()[1]);
        }
        rows.sort(Comparator.comparing(MaterialConsumptionRowResponse::getTotalCost).reversed());

        return MaterialConsumptionResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalConsumedCost(totalCost)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionOutputResponse productionOutput(ManufacturingReportFilterRequest filter) {
        List<ManufacturingOrder> orders = completedOrders(filter);

        Map<Long, List<ManufacturingOrder>> byProduct = orders.stream().collect(Collectors.groupingBy(ManufacturingOrder::getProductId));
        Map<Long, Product> products = productRepository.findAllById(byProduct.keySet()).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<ProductionOutputRowResponse> rows = new ArrayList<>();
        for (Map.Entry<Long, List<ManufacturingOrder>> entry : byProduct.entrySet()) {
            Product product = products.get(entry.getKey());
            rows.add(ProductionOutputRowResponse.builder()
                    .productId(entry.getKey())
                    .productName(product == null ? null : product.getName())
                    .productSku(product == null ? null : product.getSku())
                    .orderCount(entry.getValue().size())
                    .totalProducedQuantity(sum(entry.getValue(), ManufacturingOrder::getProducedQuantity))
                    .totalScrapQuantity(sum(entry.getValue(), ManufacturingOrder::getScrapQuantity))
                    .build());
        }
        rows.sort(Comparator.comparing(ProductionOutputRowResponse::getTotalProducedQuantity).reversed());

        return ProductionOutputResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalProducedQuantity(sum(orders, ManufacturingOrder::getProducedQuantity))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ScrapWastageResponse scrapWastage(ManufacturingReportFilterRequest filter) {
        List<ManufacturingOrder> orders = completedOrders(filter).stream()
                .filter(mo -> mo.getScrapQuantity().signum() > 0)
                .sorted(Comparator.comparing(ManufacturingOrder::getActualEndDate).reversed())
                .toList();

        Map<Long, Product> products = productRepository.findAllById(
                orders.stream().map(ManufacturingOrder::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<ScrapWastageRowResponse> rows = orders.stream()
                .map(mo -> {
                    Product product = products.get(mo.getProductId());
                    BigDecimal totalOutput = mo.getProducedQuantity().add(mo.getScrapQuantity());
                    BigDecimal scrapPercent = totalOutput.signum() == 0
                            ? BigDecimal.ZERO
                            : mo.getScrapQuantity().multiply(HUNDRED).divide(totalOutput, 4, RoundingMode.HALF_UP);
                    return ScrapWastageRowResponse.builder()
                            .moNumber(mo.getMoNumber())
                            .productId(mo.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productSku(product == null ? null : product.getSku())
                            .plannedQuantity(mo.getPlannedQuantity())
                            .producedQuantity(mo.getProducedQuantity())
                            .scrapQuantity(mo.getScrapQuantity())
                            .scrapPercent(scrapPercent)
                            .scrapReason(mo.getScrapReason())
                            .actualEndDate(mo.getActualEndDate())
                            .build();
                })
                .toList();

        return ScrapWastageResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalScrapQuantity(sum(orders, ManufacturingOrder::getScrapQuantity))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ManufacturingCostResponse cost(ManufacturingReportFilterRequest filter) {
        List<ManufacturingOrder> orders = completedOrders(filter).stream()
                .sorted(Comparator.comparing(ManufacturingOrder::getActualEndDate).reversed())
                .toList();

        Map<Long, Product> products = productRepository.findAllById(
                orders.stream().map(ManufacturingOrder::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<ManufacturingCostRowResponse> rows = orders.stream()
                .map(mo -> {
                    Product product = products.get(mo.getProductId());
                    return ManufacturingCostRowResponse.builder()
                            .moNumber(mo.getMoNumber())
                            .productId(mo.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productSku(product == null ? null : product.getSku())
                            .producedQuantity(mo.getProducedQuantity())
                            .materialCost(mo.getMaterialCost())
                            .laborCost(mo.getLaborCost())
                            .overheadCost(mo.getOverheadCost())
                            .totalCost(mo.getTotalCost())
                            .unitCost(mo.getUnitCost())
                            .build();
                })
                .toList();

        return ManufacturingCostResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalMaterialCost(sum(orders, ManufacturingOrder::getMaterialCost))
                .totalLaborCost(sum(orders, ManufacturingOrder::getLaborCost))
                .totalOverheadCost(sum(orders, ManufacturingOrder::getOverheadCost))
                .totalCost(sum(orders, ManufacturingOrder::getTotalCost))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RejectionsResponse rejections(ManufacturingReportFilterRequest filter) {
        List<Specification<ManufacturingOrder>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getWarehouseId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("warehouseId"), filter.getWarehouseId()));
        if (filter.getProductId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("productId"), filter.getProductId()));
        Map<Long, ManufacturingOrder> ordersById = orderRepository.findAll(Specification.allOf(conditions)).stream()
                .collect(Collectors.toMap(ManufacturingOrder::getId, mo -> mo));

        List<ManufacturingRejection> rejections = new ArrayList<>();
        for (Long orderId : ordersById.keySet()) {
            rejections.addAll(rejectionRepository.findByManufacturingOrderId(orderId));
        }
        List<ManufacturingRejection> filtered = rejections.stream()
                .filter(r -> {
                    LocalDate d = r.getRejectedAt().toLocalDate();
                    if (filter.getDateFrom() != null && d.isBefore(filter.getDateFrom())) return false;
                    if (filter.getDateTo() != null && d.isAfter(filter.getDateTo())) return false;
                    return true;
                })
                .sorted(Comparator.comparing(ManufacturingRejection::getRejectedAt).reversed())
                .toList();

        Map<Long, Product> products = productRepository.findAllById(
                filtered.stream().map(ManufacturingRejection::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<RejectionRowResponse> rows = filtered.stream()
                .map(r -> {
                    ManufacturingOrder mo = ordersById.get(r.getManufacturingOrderId());
                    Product product = products.get(r.getProductId());
                    return RejectionRowResponse.builder()
                            .moNumber(mo == null ? null : mo.getMoNumber())
                            .productId(r.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productSku(product == null ? null : product.getSku())
                            .quantity(r.getQuantity())
                            .reason(r.getReason())
                            .rejectedBy(r.getRejectedBy())
                            .rejectedAt(r.getRejectedAt())
                            .build();
                })
                .toList();

        BigDecimal totalRejectedQuantity = filtered.stream().map(ManufacturingRejection::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);

        return RejectionsResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalRejectedQuantity(totalRejectedQuantity)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialRequirementsResponse materialRequirements(ManufacturingReportFilterRequest filter) {
        List<Specification<ManufacturingOrder>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> root.get("status").in(ManufacturingOrderStatus.DRAFT, ManufacturingOrderStatus.RELEASED));
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getWarehouseId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("warehouseId"), filter.getWarehouseId()));
        if (filter.getProductId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("productId"), filter.getProductId()));
        List<ManufacturingOrder> orders = orderRepository.findAll(Specification.allOf(conditions));

        // Grouped by (componentProductId, warehouseId) — availability is
        // always warehouse-specific, so requirements from orders in
        // different warehouses can't be pooled together.
        record Key(Long componentProductId, Long warehouseId) {}
        Map<Key, BigDecimal> requiredByKey = new HashMap<>();
        for (ManufacturingOrder mo : orders) {
            for (ManufacturingOrderMaterial material : materialRepository.findByManufacturingOrderId(mo.getId())) {
                Key key = new Key(material.getComponentProductId(), mo.getWarehouseId());
                requiredByKey.merge(key, material.getRequiredQuantity(), BigDecimal::add);
            }
        }

        Map<Long, Product> products = productRepository.findAllById(
                requiredByKey.keySet().stream().map(Key::componentProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, Warehouse> warehouses = warehouseRepository.findAllById(
                requiredByKey.keySet().stream().map(Key::warehouseId).distinct().toList()
        ).stream().collect(Collectors.toMap(Warehouse::getId, w -> w));

        List<MaterialRequirementRowResponse> rows = new ArrayList<>();
        long shortageCount = 0;
        for (Map.Entry<Key, BigDecimal> entry : requiredByKey.entrySet()) {
            Product product = products.get(entry.getKey().componentProductId());
            Warehouse warehouse = warehouses.get(entry.getKey().warehouseId());
            BigDecimal available = stockLevelRepository.findByProductIdAndWarehouseIdAndBinIdIsNull(
                    entry.getKey().componentProductId(), entry.getKey().warehouseId()
            ).map(StockLevel::getQuantityOnHand).orElse(BigDecimal.ZERO);
            BigDecimal shortfall = entry.getValue().subtract(available);
            if (shortfall.signum() < 0) shortfall = BigDecimal.ZERO;
            if (shortfall.signum() > 0) shortageCount++;

            rows.add(MaterialRequirementRowResponse.builder()
                    .componentProductId(entry.getKey().componentProductId())
                    .componentProductName(product == null ? null : product.getName())
                    .componentProductSku(product == null ? null : product.getSku())
                    .warehouseId(entry.getKey().warehouseId())
                    .warehouseName(warehouse == null ? null : warehouse.getName())
                    .totalRequiredQuantity(entry.getValue())
                    .availableQuantity(available)
                    .shortfallQuantity(shortfall)
                    .build());
        }
        rows.sort(Comparator.comparing(MaterialRequirementRowResponse::getShortfallQuantity).reversed());

        return MaterialRequirementsResponse.builder()
                .rows(rows)
                .shortageCount(shortageCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BomCostResponse bomCost(ManufacturingReportFilterRequest filter) {
        List<Specification<BillOfMaterial>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> cb.equal(root.get("status"), BillOfMaterialStatus.ACTIVE));
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getProductId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("productId"), filter.getProductId()));
        List<BillOfMaterial> boms = bomRepository.findAll(Specification.allOf(conditions));

        Map<Long, Product> products = productRepository.findAllById(
                boms.stream().map(BillOfMaterial::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<BomCostRowResponse> rows = boms.stream()
                .map(bom -> {
                    Product product = products.get(bom.getProductId());
                    BigDecimal perBatch = materialCostPerBatch(bom);
                    BigDecimal perUnit = bom.getOutputQuantity().signum() > 0
                            ? perBatch.divide(bom.getOutputQuantity(), 4, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return BomCostRowResponse.builder()
                            .bomId(bom.getId())
                            .bomNumber(bom.getBomNumber())
                            .name(bom.getName())
                            .version(bom.getVersion())
                            .productId(bom.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productSku(product == null ? null : product.getSku())
                            .outputQuantity(bom.getOutputQuantity())
                            .materialCostPerBatch(perBatch)
                            .materialCostPerUnit(perUnit)
                            .build();
                })
                .toList();

        return BomCostResponse.builder().rows(rows).build();
    }

    @Override
    @Transactional(readOnly = true)
    public CostVarianceResponse costVariance(ManufacturingReportFilterRequest filter) {
        List<ManufacturingOrder> orders = matchingOrders(filter, ManufacturingOrder::getActualStartDate).stream()
                .filter(mo -> mo.getActualStartDate() != null)
                .toList();

        Map<Long, BillOfMaterial> boms = bomRepository.findAllById(
                orders.stream().map(ManufacturingOrder::getBomId).distinct().toList()
        ).stream().collect(Collectors.toMap(BillOfMaterial::getId, b -> b));
        Map<Long, Product> products = productRepository.findAllById(
                orders.stream().map(ManufacturingOrder::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<CostVarianceRowResponse> rows = new ArrayList<>();
        BigDecimal totalStandard = BigDecimal.ZERO;
        BigDecimal totalActual = BigDecimal.ZERO;
        for (ManufacturingOrder mo : orders) {
            BillOfMaterial bom = boms.get(mo.getBomId());
            if (bom == null) continue;
            Product product = products.get(mo.getProductId());
            BigDecimal standardPerUnit = bom.getOutputQuantity().signum() > 0
                    ? materialCostPerBatch(bom).divide(bom.getOutputQuantity(), 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal standardCost = standardPerUnit.multiply(mo.getPlannedQuantity());
            BigDecimal actualCost = mo.getMaterialCost();
            BigDecimal varianceAmount = actualCost.subtract(standardCost);
            BigDecimal variancePercent = standardCost.signum() == 0
                    ? BigDecimal.ZERO
                    : varianceAmount.multiply(HUNDRED).divide(standardCost, 4, RoundingMode.HALF_UP);

            rows.add(CostVarianceRowResponse.builder()
                    .moNumber(mo.getMoNumber())
                    .productId(mo.getProductId())
                    .productName(product == null ? null : product.getName())
                    .productSku(product == null ? null : product.getSku())
                    .plannedQuantity(mo.getPlannedQuantity())
                    .standardMaterialCost(standardCost)
                    .actualMaterialCost(actualCost)
                    .varianceAmount(varianceAmount)
                    .variancePercent(variancePercent)
                    .build());
            totalStandard = totalStandard.add(standardCost);
            totalActual = totalActual.add(actualCost);
        }
        rows.sort(Comparator.comparing(CostVarianceRowResponse::getVarianceAmount).reversed());

        return CostVarianceResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalStandardMaterialCost(totalStandard)
                .totalActualMaterialCost(totalActual)
                .totalVarianceAmount(totalActual.subtract(totalStandard))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionTimeResponse productionTime(ManufacturingReportFilterRequest filter) {
        List<ManufacturingOrder> orders = matchingOrders(filter, ManufacturingOrder::getActualEndDate).stream()
                .filter(mo -> mo.getActualStartDate() != null && mo.getActualEndDate() != null)
                .toList();

        Map<Long, Product> products = productRepository.findAllById(
                orders.stream().map(ManufacturingOrder::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<ProductionTimeRowResponse> rows = new ArrayList<>();
        BigDecimal totalHours = BigDecimal.ZERO;
        for (ManufacturingOrder mo : orders) {
            Product product = products.get(mo.getProductId());
            BigDecimal hours = BigDecimal.valueOf(Duration.between(mo.getActualStartDate(), mo.getActualEndDate()).getSeconds())
                    .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
            rows.add(ProductionTimeRowResponse.builder()
                    .moNumber(mo.getMoNumber())
                    .productId(mo.getProductId())
                    .productName(product == null ? null : product.getName())
                    .productSku(product == null ? null : product.getSku())
                    .plannedStartDate(mo.getPlannedStartDate())
                    .plannedEndDate(mo.getPlannedEndDate())
                    .actualStartDate(mo.getActualStartDate())
                    .actualEndDate(mo.getActualEndDate())
                    .actualDurationHours(hours)
                    .build());
            totalHours = totalHours.add(hours);
        }
        rows.sort(Comparator.comparing(ProductionTimeRowResponse::getActualEndDate).reversed());

        BigDecimal averageHours = rows.isEmpty() ? BigDecimal.ZERO : totalHours.divide(BigDecimal.valueOf(rows.size()), 2, RoundingMode.HALF_UP);

        return ProductionTimeResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .averageDurationHours(averageHours)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public QualityPassFailResponse qualityPassFail(ManufacturingReportFilterRequest filter) {
        List<ManufacturingOrder> orders = matchingOrders(filter, ManufacturingOrder::getQualityCheckedAt).stream()
                .filter(mo -> mo.getQualityCheckedAt() != null
                        && (mo.getQualityStatus() == QualityCheckStatus.PASSED || mo.getQualityStatus() == QualityCheckStatus.FAILED))
                .toList();

        Map<Long, List<ManufacturingOrder>> byProduct = orders.stream().collect(Collectors.groupingBy(ManufacturingOrder::getProductId));
        Map<Long, Product> products = productRepository.findAllById(byProduct.keySet()).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<QualityPassFailRowResponse> rows = new ArrayList<>();
        for (Map.Entry<Long, List<ManufacturingOrder>> entry : byProduct.entrySet()) {
            Product product = products.get(entry.getKey());
            long passed = entry.getValue().stream().filter(mo -> mo.getQualityStatus() == QualityCheckStatus.PASSED).count();
            long failed = entry.getValue().size() - passed;
            BigDecimal passRate = entry.getValue().isEmpty()
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(passed).multiply(HUNDRED).divide(BigDecimal.valueOf(entry.getValue().size()), 2, RoundingMode.HALF_UP);
            rows.add(QualityPassFailRowResponse.builder()
                    .productId(entry.getKey())
                    .productName(product == null ? null : product.getName())
                    .productSku(product == null ? null : product.getSku())
                    .inspectedCount(entry.getValue().size())
                    .passedCount(passed)
                    .failedCount(failed)
                    .passRatePercent(passRate)
                    .build());
        }
        rows.sort(Comparator.comparing(QualityPassFailRowResponse::getInspectedCount).reversed());

        long totalPassed = orders.stream().filter(mo -> mo.getQualityStatus() == QualityCheckStatus.PASSED).count();
        long totalFailed = orders.size() - totalPassed;
        BigDecimal overallPassRate = orders.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(totalPassed).multiply(HUNDRED).divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);

        return QualityPassFailResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalInspected(orders.size())
                .totalPassed(totalPassed)
                .totalFailed(totalFailed)
                .overallPassRatePercent(overallPassRate)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PlanVsActualResponse planVsActual(ManufacturingReportFilterRequest filter) {
        List<ManufacturingOrder> orders = completedOrders(filter);

        Map<Long, Product> products = productRepository.findAllById(
                orders.stream().map(ManufacturingOrder::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<PlanVsActualRowResponse> rows = new ArrayList<>();
        BigDecimal totalAchievement = BigDecimal.ZERO;
        BigDecimal totalYield = BigDecimal.ZERO;
        for (ManufacturingOrder mo : orders) {
            Product product = products.get(mo.getProductId());
            BigDecimal achievement = mo.getPlannedQuantity().signum() == 0
                    ? BigDecimal.ZERO
                    : mo.getProducedQuantity().multiply(HUNDRED).divide(mo.getPlannedQuantity(), 2, RoundingMode.HALF_UP);
            BigDecimal totalOutput = mo.getProducedQuantity().add(mo.getScrapQuantity());
            BigDecimal yieldPercent = totalOutput.signum() == 0
                    ? BigDecimal.ZERO
                    : mo.getProducedQuantity().multiply(HUNDRED).divide(totalOutput, 2, RoundingMode.HALF_UP);
            rows.add(PlanVsActualRowResponse.builder()
                    .moNumber(mo.getMoNumber())
                    .productId(mo.getProductId())
                    .productName(product == null ? null : product.getName())
                    .productSku(product == null ? null : product.getSku())
                    .plannedQuantity(mo.getPlannedQuantity())
                    .producedQuantity(mo.getProducedQuantity())
                    .scrapQuantity(mo.getScrapQuantity())
                    .achievementPercent(achievement)
                    .yieldPercent(yieldPercent)
                    .build());
            totalAchievement = totalAchievement.add(achievement);
            totalYield = totalYield.add(yieldPercent);
        }
        rows.sort(Comparator.comparing(PlanVsActualRowResponse::getAchievementPercent));

        BigDecimal avgAchievement = rows.isEmpty() ? BigDecimal.ZERO : totalAchievement.divide(BigDecimal.valueOf(rows.size()), 2, RoundingMode.HALF_UP);
        BigDecimal avgYield = rows.isEmpty() ? BigDecimal.ZERO : totalYield.divide(BigDecimal.valueOf(rows.size()), 2, RoundingMode.HALF_UP);

        return PlanVsActualResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .averageAchievementPercent(avgAchievement)
                .averageYieldPercent(avgYield)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionTrendResponse productionTrend(ManufacturingReportFilterRequest filter) {
        List<ManufacturingOrder> orders = completedOrders(filter);

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, List<ManufacturingOrder>> byMonth = orders.stream()
                .collect(Collectors.groupingBy(mo -> mo.getActualEndDate().toLocalDate().format(monthFormatter)));

        List<ProductionTrendRowResponse> rows = byMonth.entrySet().stream()
                .map(entry -> ProductionTrendRowResponse.builder()
                        .month(entry.getKey())
                        .orderCount(entry.getValue().size())
                        .totalProducedQuantity(sum(entry.getValue(), ManufacturingOrder::getProducedQuantity))
                        .totalScrapQuantity(sum(entry.getValue(), ManufacturingOrder::getScrapQuantity))
                        .build())
                .sorted(Comparator.comparing(ProductionTrendRowResponse::getMonth))
                .toList();

        return ProductionTrendResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ManufacturingProfitabilityResponse profitability(ManufacturingReportFilterRequest filter) {
        List<ManufacturingOrder> orders = completedOrders(filter).stream()
                .filter(mo -> mo.getProducedQuantity().signum() > 0)
                .toList();

        Map<Long, List<ManufacturingOrder>> byProduct = orders.stream().collect(Collectors.groupingBy(ManufacturingOrder::getProductId));
        Map<Long, Product> products = productRepository.findAllById(byProduct.keySet()).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<ManufacturingProfitabilityRowResponse> rows = new ArrayList<>();
        BigDecimal totalMargin = BigDecimal.ZERO;
        for (Map.Entry<Long, List<ManufacturingOrder>> entry : byProduct.entrySet()) {
            Product product = products.get(entry.getKey());
            BigDecimal totalProduced = sum(entry.getValue(), ManufacturingOrder::getProducedQuantity);
            BigDecimal totalCost = sum(entry.getValue(), ManufacturingOrder::getTotalCost);
            BigDecimal averageUnitCost = totalProduced.signum() == 0
                    ? BigDecimal.ZERO
                    : totalCost.divide(totalProduced, 4, RoundingMode.HALF_UP);
            BigDecimal sellingPrice = product == null ? BigDecimal.ZERO : product.getSellingPrice();
            BigDecimal marginPerUnit = sellingPrice.subtract(averageUnitCost);
            BigDecimal rowTotalMargin = marginPerUnit.multiply(totalProduced);
            BigDecimal marginPercent = sellingPrice.signum() == 0
                    ? BigDecimal.ZERO
                    : marginPerUnit.multiply(HUNDRED).divide(sellingPrice, 2, RoundingMode.HALF_UP);

            rows.add(ManufacturingProfitabilityRowResponse.builder()
                    .productId(entry.getKey())
                    .productName(product == null ? null : product.getName())
                    .productSku(product == null ? null : product.getSku())
                    .totalProducedQuantity(totalProduced)
                    .averageUnitCost(averageUnitCost)
                    .sellingPrice(sellingPrice)
                    .marginPerUnit(marginPerUnit)
                    .totalMargin(rowTotalMargin)
                    .marginPercent(marginPercent)
                    .build());
            totalMargin = totalMargin.add(rowTotalMargin);
        }
        rows.sort(Comparator.comparing(ManufacturingProfitabilityRowResponse::getTotalMargin).reversed());

        return ManufacturingProfitabilityResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalMargin(totalMargin)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkCenterUtilizationResponse workCenterUtilization(ManufacturingReportFilterRequest filter) {
        List<WorkOrder> workOrders = matchingCompletedWorkOrders(filter);

        Map<Long, List<WorkOrder>> byWorkCenter = workOrders.stream().collect(Collectors.groupingBy(WorkOrder::getWorkCenterId));
        Map<Long, WorkCenter> workCenters = workCenterRepository.findAllById(byWorkCenter.keySet()).stream()
                .collect(Collectors.toMap(WorkCenter::getId, w -> w));

        BigDecimal periodHours = periodHours(filter);

        List<WorkCenterUtilizationRowResponse> rows = new ArrayList<>();
        for (Map.Entry<Long, List<WorkOrder>> entry : byWorkCenter.entrySet()) {
            WorkCenter workCenter = workCenters.get(entry.getKey());
            BigDecimal totalHours = totalActualHours(entry.getValue());
            BigDecimal avgHours = totalHours.divide(BigDecimal.valueOf(entry.getValue().size()), 2, RoundingMode.HALF_UP);
            BigDecimal utilization = periodHours == null || periodHours.signum() == 0
                    ? null
                    : totalHours.multiply(HUNDRED).divide(periodHours, 2, RoundingMode.HALF_UP);
            rows.add(WorkCenterUtilizationRowResponse.builder()
                    .workCenterId(entry.getKey())
                    .workCenterName(workCenter == null ? null : workCenter.getName())
                    .operationCount(entry.getValue().size())
                    .totalActualHours(totalHours)
                    .averageHoursPerOperation(avgHours)
                    .utilizationPercent(utilization)
                    .build());
        }
        rows.sort(Comparator.comparing(WorkCenterUtilizationRowResponse::getTotalActualHours).reversed());

        return WorkCenterUtilizationResponse.builder().dateFrom(filter.getDateFrom()).dateTo(filter.getDateTo()).rows(rows).build();
    }

    @Override
    @Transactional(readOnly = true)
    public MachineUtilizationResponse machineUtilization(ManufacturingReportFilterRequest filter) {
        List<WorkOrder> workOrders = matchingCompletedWorkOrders(filter).stream()
                .filter(wo -> wo.getMachineId() != null)
                .toList();

        Map<Long, List<WorkOrder>> byMachine = workOrders.stream().collect(Collectors.groupingBy(WorkOrder::getMachineId));
        Map<Long, Machine> machines = machineRepository.findAllById(byMachine.keySet()).stream()
                .collect(Collectors.toMap(Machine::getId, m -> m));

        List<MachineUtilizationRowResponse> rows = new ArrayList<>();
        for (Map.Entry<Long, List<WorkOrder>> entry : byMachine.entrySet()) {
            Machine machine = machines.get(entry.getKey());
            BigDecimal totalHours = totalActualHours(entry.getValue());
            BigDecimal avgHours = totalHours.divide(BigDecimal.valueOf(entry.getValue().size()), 2, RoundingMode.HALF_UP);
            rows.add(MachineUtilizationRowResponse.builder()
                    .machineId(entry.getKey())
                    .machineName(machine == null ? null : machine.getName())
                    .machineStatus(machine == null ? null : machine.getStatus().name())
                    .operationCount(entry.getValue().size())
                    .totalActualHours(totalHours)
                    .averageHoursPerOperation(avgHours)
                    .build());
        }
        rows.sort(Comparator.comparing(MachineUtilizationRowResponse::getTotalActualHours).reversed());

        return MachineUtilizationResponse.builder().dateFrom(filter.getDateFrom()).dateTo(filter.getDateTo()).rows(rows).build();
    }

    @Override
    @Transactional(readOnly = true)
    public OperationPerformanceResponse operationPerformance(ManufacturingReportFilterRequest filter) {
        List<WorkOrder> workOrders = matchingCompletedWorkOrders(filter);

        record Key(String name, Long workCenterId) {}
        Map<Key, List<WorkOrder>> byOperation = workOrders.stream()
                .collect(Collectors.groupingBy(wo -> new Key(wo.getName(), wo.getWorkCenterId())));
        Map<Long, WorkCenter> workCenters = workCenterRepository.findAllById(
                byOperation.keySet().stream().map(Key::workCenterId).distinct().toList()
        ).stream().collect(Collectors.toMap(WorkCenter::getId, w -> w));

        List<OperationPerformanceRowResponse> rows = new ArrayList<>();
        for (Map.Entry<Key, List<WorkOrder>> entry : byOperation.entrySet()) {
            WorkCenter workCenter = workCenters.get(entry.getKey().workCenterId());
            BigDecimal totalActualMinutes = entry.getValue().stream()
                    .map(wo -> BigDecimal.valueOf(Duration.between(wo.getActualStartDate(), wo.getActualEndDate()).getSeconds())
                            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avgActualMinutes = totalActualMinutes.divide(BigDecimal.valueOf(entry.getValue().size()), 2, RoundingMode.HALF_UP);
            BigDecimal standardMinutes = entry.getValue().get(0).getStandardTimeMinutes();
            if (standardMinutes == null) standardMinutes = BigDecimal.ZERO;
            rows.add(OperationPerformanceRowResponse.builder()
                    .operationName(entry.getKey().name())
                    .workCenterName(workCenter == null ? null : workCenter.getName())
                    .executionCount(entry.getValue().size())
                    .standardTimeMinutes(standardMinutes)
                    .averageActualTimeMinutes(avgActualMinutes)
                    .varianceMinutes(avgActualMinutes.subtract(standardMinutes))
                    .build());
        }
        rows.sort(Comparator.comparing(OperationPerformanceRowResponse::getVarianceMinutes).reversed());

        return OperationPerformanceResponse.builder().dateFrom(filter.getDateFrom()).dateTo(filter.getDateTo()).rows(rows).build();
    }

    @Override
    @Transactional(readOnly = true)
    public MachineCostResponse machineCost(ManufacturingReportFilterRequest filter) {
        List<WorkOrder> workOrders = matchingCompletedWorkOrders(filter).stream()
                .filter(wo -> wo.getMachineId() != null)
                .toList();

        Map<Long, List<WorkOrder>> byMachine = workOrders.stream().collect(Collectors.groupingBy(WorkOrder::getMachineId));
        Map<Long, Machine> machines = machineRepository.findAllById(byMachine.keySet()).stream()
                .collect(Collectors.toMap(Machine::getId, m -> m));

        List<MachineCostRowResponse> rows = new ArrayList<>();
        BigDecimal totalCost = BigDecimal.ZERO;
        for (Map.Entry<Long, List<WorkOrder>> entry : byMachine.entrySet()) {
            Machine machine = machines.get(entry.getKey());
            BigDecimal totalHours = totalActualHours(entry.getValue());
            BigDecimal costPerHour = machine == null ? null : machine.getCostPerHour();
            BigDecimal rowCost = costPerHour == null ? null : totalHours.multiply(costPerHour).setScale(4, RoundingMode.HALF_UP);
            rows.add(MachineCostRowResponse.builder()
                    .machineId(entry.getKey())
                    .machineName(machine == null ? null : machine.getName())
                    .operationCount(entry.getValue().size())
                    .totalActualHours(totalHours)
                    .costPerHour(costPerHour)
                    .totalCost(rowCost)
                    .build());
            if (rowCost != null) totalCost = totalCost.add(rowCost);
        }
        rows.sort(Comparator.comparing((MachineCostRowResponse r) -> r.getTotalCost() == null ? BigDecimal.ZERO : r.getTotalCost()).reversed());

        return MachineCostResponse.builder().dateFrom(filter.getDateFrom()).dateTo(filter.getDateTo()).rows(rows).totalCost(totalCost).build();
    }

    // Work orders don't carry company/warehouse/product fields directly —
    // matched via their parent ManufacturingOrder, then filtered to
    // COMPLETED (the only status with both actual timestamps set) and to
    // the requested date range on actualStartDate.
    private List<WorkOrder> matchingCompletedWorkOrders(ManufacturingReportFilterRequest filter) {
        List<Long> orderIds = matchingOrders(filter, ManufacturingOrder::getCreatedAt).stream().map(ManufacturingOrder::getId).toList();
        if (orderIds.isEmpty()) return List.of();
        List<WorkOrder> all = new ArrayList<>();
        for (Long orderId : orderIds) {
            all.addAll(workOrderRepository.findByManufacturingOrderIdOrderBySequenceNumberAsc(orderId));
        }
        return all.stream()
                .filter(wo -> wo.getStatus() == WorkOrderStatus.COMPLETED && wo.getActualStartDate() != null && wo.getActualEndDate() != null)
                .filter(wo -> {
                    LocalDate d = wo.getActualStartDate().toLocalDate();
                    if (filter.getDateFrom() != null && d.isBefore(filter.getDateFrom())) return false;
                    if (filter.getDateTo() != null && d.isAfter(filter.getDateTo())) return false;
                    return true;
                })
                .toList();
    }

    private BigDecimal totalActualHours(List<WorkOrder> workOrders) {
        return workOrders.stream()
                .map(wo -> BigDecimal.valueOf(Duration.between(wo.getActualStartDate(), wo.getActualEndDate()).getSeconds())
                        .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal periodHours(ManufacturingReportFilterRequest filter) {
        if (filter.getDateFrom() == null || filter.getDateTo() == null) return null;
        long hours = Duration.between(filter.getDateFrom().atStartOfDay(), filter.getDateTo().plusDays(1).atStartOfDay()).toHours();
        return BigDecimal.valueOf(hours);
    }

    // Sum of each component line's current Product.costPrice * quantity,
    // scaled by scrapPercent — see BomCostRowResponse's own comment on why
    // this is a live estimate, not a historical actual.
    private BigDecimal materialCostPerBatch(BillOfMaterial bom) {
        List<BillOfMaterialLine> lines = bomLineRepository.findByBomId(bom.getId());
        Map<Long, Product> components = productRepository.findAllById(
                lines.stream().map(BillOfMaterialLine::getComponentProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        BigDecimal total = BigDecimal.ZERO;
        for (BillOfMaterialLine line : lines) {
            Product component = components.get(line.getComponentProductId());
            if (component == null) continue;
            BigDecimal scrapMultiplier = BigDecimal.ONE.add(
                    (line.getScrapPercent() == null ? BigDecimal.ZERO : line.getScrapPercent()).divide(HUNDRED, 8, RoundingMode.HALF_UP));
            total = total.add(line.getQuantity().multiply(scrapMultiplier).multiply(component.getCostPrice()));
        }
        return total;
    }

    @Override
    @Transactional(readOnly = true)
    public BomComparisonResponse bomComparison(Long bomId, Long compareToBomId) {
        BillOfMaterial left = bomRepository.findById(bomId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Bill of materials not found with id: " + bomId));
        BillOfMaterial right = bomRepository.findById(compareToBomId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Bill of materials not found with id: " + compareToBomId));

        return BomComparisonResponse.builder()
                .left(buildComparisonSide(left))
                .right(buildComparisonSide(right))
                .build();
    }

    private BomComparisonSideResponse buildComparisonSide(BillOfMaterial bom) {
        List<BillOfMaterialLine> lines = bomLineRepository.findByBomId(bom.getId());
        Map<Long, Product> components = productRepository.findAllById(
                lines.stream().map(BillOfMaterialLine::getComponentProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<BomComparisonLineResponse> lineResponses = new ArrayList<>();
        BigDecimal totalCost = BigDecimal.ZERO;
        for (BillOfMaterialLine line : lines) {
            Product component = components.get(line.getComponentProductId());
            BigDecimal scrapMultiplier = BigDecimal.ONE.add(
                    (line.getScrapPercent() == null ? BigDecimal.ZERO : line.getScrapPercent()).divide(HUNDRED, 8, RoundingMode.HALF_UP));
            BigDecimal lineCost = component == null
                    ? BigDecimal.ZERO
                    : line.getQuantity().multiply(scrapMultiplier).multiply(component.getCostPrice());
            lineResponses.add(BomComparisonLineResponse.builder()
                    .componentProductId(line.getComponentProductId())
                    .componentProductName(component == null ? null : component.getName())
                    .componentProductSku(component == null ? null : component.getSku())
                    .quantity(line.getQuantity())
                    .scrapPercent(line.getScrapPercent())
                    .lineCost(lineCost)
                    .build());
            totalCost = totalCost.add(lineCost);
        }

        BigDecimal perUnit = bom.getOutputQuantity().signum() > 0
                ? totalCost.divide(bom.getOutputQuantity(), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return BomComparisonSideResponse.builder()
                .bomId(bom.getId())
                .bomNumber(bom.getBomNumber())
                .version(bom.getVersion())
                .name(bom.getName())
                .status(bom.getStatus().name())
                .outputQuantity(bom.getOutputQuantity())
                .materialCostPerBatch(totalCost)
                .materialCostPerUnit(perUnit)
                .lines(lineResponses)
                .build();
    }

    private List<ManufacturingOrder> completedOrders(ManufacturingReportFilterRequest filter) {
        return matchingOrders(filter, ManufacturingOrder::getActualEndDate).stream()
                .filter(mo -> mo.getStatus() == ManufacturingOrderStatus.COMPLETED && mo.getActualEndDate() != null)
                .toList();
    }

    // Fetches every order matching company/warehouse/product, then filters
    // dateFrom/dateTo in memory against whichever timestamp the caller cares
    // about (createdAt for the all-statuses summary, actualStartDate for
    // consumption, actualEndDate for output/scrap/cost) — simpler than a
    // per-report Specification date clause, and manufacturing order volume
    // is low enough that this isn't a performance concern.
    private List<ManufacturingOrder> matchingOrders(ManufacturingReportFilterRequest filter, java.util.function.Function<ManufacturingOrder, LocalDateTime> dateField) {
        List<Specification<ManufacturingOrder>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getWarehouseId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("warehouseId"), filter.getWarehouseId()));
        }
        if (filter.getProductId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("productId"), filter.getProductId()));
        }
        List<ManufacturingOrder> orders = orderRepository.findAll(Specification.allOf(conditions));
        return orders.stream()
                .filter(mo -> {
                    LocalDateTime date = dateField.apply(mo);
                    if (date == null) return filter.getDateFrom() == null && filter.getDateTo() == null;
                    LocalDate d = date.toLocalDate();
                    if (filter.getDateFrom() != null && d.isBefore(filter.getDateFrom())) return false;
                    if (filter.getDateTo() != null && d.isAfter(filter.getDateTo())) return false;
                    return true;
                })
                .toList();
    }

    private BigDecimal sum(List<ManufacturingOrder> orders, java.util.function.Function<ManufacturingOrder, BigDecimal> field) {
        return orders.stream().map(field).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
