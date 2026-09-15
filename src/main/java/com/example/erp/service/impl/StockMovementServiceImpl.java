package com.example.erp.service.impl;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.StockMovementFilterRequest;
import com.example.erp.dto.StockMovementResponse;
import com.example.erp.entity.Product;
import com.example.erp.entity.StockMovement;
import com.example.erp.entity.StockMovementDirection;
import com.example.erp.entity.Warehouse;
import com.example.erp.entity.WarehouseBin;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.StockMovementRepository;
import com.example.erp.repository.WarehouseBinRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.service.StockMovementService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository repository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseBinRepository binRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockMovementResponse> list(StockMovementFilterRequest filter) {
        List<Specification<StockMovement>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getProductId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("productId"), filter.getProductId()));
        }
        if (filter.getWarehouseId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("warehouseId"), filter.getWarehouseId()));
        }
        if (filter.getType() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("type"), filter.getType()));
        }
        if (filter.getDirection() != null) {
            boolean in = filter.getDirection() == StockMovementDirection.IN;
            conditions.add((root, query, cb) -> in
                    ? cb.ge(root.get("quantityDelta"), BigDecimal.ZERO)
                    : cb.lt(root.get("quantityDelta"), BigDecimal.ZERO));
        }
        if (filter.getDateFrom() != null) {
            conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getDateFrom().atStartOfDay()));
        }
        if (filter.getDateTo() != null) {
            conditions.add((root, query, cb) -> cb.lessThan(root.get("createdAt"), filter.getDateTo().plusDays(1).atStartOfDay()));
        }
        Specification<StockMovement> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<StockMovement> page = repository.findAll(spec, pageable);
        List<StockMovement> content = page.getContent();

        Map<Long, Product> products = productRepository.findAllById(
                content.stream().map(StockMovement::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, String> warehouseNames = warehouseRepository.findAllById(
                content.stream().map(StockMovement::getWarehouseId).distinct().toList()
        ).stream().collect(Collectors.toMap(Warehouse::getId, Warehouse::getName));
        Map<Long, String> binNames = binRepository.findAllById(
                content.stream().map(StockMovement::getBinId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(WarehouseBin::getId, WarehouseBin::getName));

        return PageResponse.of(page.map(m -> {
            Product product = products.get(m.getProductId());
            return StockMovementResponse.builder()
                    .id(m.getId())
                    .companyId(m.getCompanyId())
                    .productId(m.getProductId())
                    .productName(product == null ? null : product.getName())
                    .productSku(product == null ? null : product.getSku())
                    .warehouseId(m.getWarehouseId())
                    .warehouseName(warehouseNames.get(m.getWarehouseId()))
                    .binId(m.getBinId())
                    .binName(m.getBinId() == null ? null : binNames.get(m.getBinId()))
                    .type(m.getType().name())
                    .direction(m.getQuantityDelta().signum() >= 0 ? "IN" : "OUT")
                    .quantityDelta(m.getQuantityDelta())
                    .referenceType(m.getReferenceType())
                    .referenceId(m.getReferenceId())
                    .createdBy(m.getCreatedBy())
                    .createdAt(m.getCreatedAt())
                    .build();
        }));
    }
}
