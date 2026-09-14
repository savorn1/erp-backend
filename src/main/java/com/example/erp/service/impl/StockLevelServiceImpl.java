package com.example.erp.service.impl;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.StockLevelFilterRequest;
import com.example.erp.dto.StockLevelResponse;
import com.example.erp.entity.Product;
import com.example.erp.entity.StockLevel;
import com.example.erp.entity.UnitOfMeasure;
import com.example.erp.entity.Warehouse;
import com.example.erp.entity.WarehouseBin;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.StockLevelRepository;
import com.example.erp.repository.UnitOfMeasureRepository;
import com.example.erp.repository.WarehouseBinRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.service.StockLevelService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockLevelServiceImpl implements StockLevelService {

    private final StockLevelRepository repository;
    private final ProductRepository productRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseBinRepository binRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockLevelResponse> list(StockLevelFilterRequest filter) {
        List<Specification<StockLevel>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getProductId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("productId"), filter.getProductId()));
        }
        if (filter.getWarehouseId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("warehouseId"), filter.getWarehouseId()));
        }
        Specification<StockLevel> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<StockLevel> page = repository.findAll(spec, pageable);
        List<StockLevel> content = page.getContent();

        Map<Long, Product> products = productRepository.findAllById(
                content.stream().map(StockLevel::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, UnitOfMeasure> units = unitOfMeasureRepository.findAllById(
                products.values().stream().map(Product::getUnitOfMeasureId).distinct().toList()
        ).stream().collect(Collectors.toMap(UnitOfMeasure::getId, u -> u));
        Map<Long, String> warehouseNames = warehouseRepository.findAllById(
                content.stream().map(StockLevel::getWarehouseId).distinct().toList()
        ).stream().collect(Collectors.toMap(Warehouse::getId, Warehouse::getName));
        Map<Long, String> binNames = binRepository.findAllById(
                content.stream().map(StockLevel::getBinId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(WarehouseBin::getId, WarehouseBin::getName));

        return PageResponse.of(page.map(sl -> {
            Product product = products.get(sl.getProductId());
            UnitOfMeasure uom = product == null ? null : units.get(product.getUnitOfMeasureId());
            return StockLevelResponse.builder()
                    .id(sl.getId())
                    .companyId(sl.getCompanyId())
                    .productId(sl.getProductId())
                    .productName(product == null ? null : product.getName())
                    .productSku(product == null ? null : product.getSku())
                    .unitOfMeasureAbbreviation(uom == null ? null : uom.getAbbreviation())
                    .warehouseId(sl.getWarehouseId())
                    .warehouseName(warehouseNames.get(sl.getWarehouseId()))
                    .binId(sl.getBinId())
                    .binName(sl.getBinId() == null ? null : binNames.get(sl.getBinId()))
                    .quantityOnHand(sl.getQuantityOnHand())
                    .build();
        }));
    }
}
