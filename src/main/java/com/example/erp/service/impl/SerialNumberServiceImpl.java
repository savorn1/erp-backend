package com.example.erp.service.impl;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.SerialNumberFilterRequest;
import com.example.erp.dto.SerialNumberResponse;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductBatch;
import com.example.erp.entity.SerialNumber;
import com.example.erp.entity.Warehouse;
import com.example.erp.entity.WarehouseBin;
import com.example.erp.repository.ProductBatchRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.SerialNumberRepository;
import com.example.erp.repository.WarehouseBinRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.service.SerialNumberService;
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
public class SerialNumberServiceImpl implements SerialNumberService {

    private final SerialNumberRepository repository;
    private final ProductRepository productRepository;
    private final ProductBatchRepository productBatchRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseBinRepository binRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SerialNumberResponse> list(SerialNumberFilterRequest filter) {
        List<Specification<SerialNumber>> conditions = new ArrayList<>();
        if (filter.getSerialNumber() != null && !filter.getSerialNumber().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("serialNumber")), "%" + filter.getSerialNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getProductId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("productId"), filter.getProductId()));
        }
        if (filter.getWarehouseId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("warehouseId"), filter.getWarehouseId()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        Specification<SerialNumber> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<SerialNumber> page = repository.findAll(spec, pageable);
        List<SerialNumber> content = page.getContent();

        Map<Long, Product> products = productRepository.findAllById(
                content.stream().map(SerialNumber::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, String> warehouseNames = warehouseRepository.findAllById(
                content.stream().map(SerialNumber::getWarehouseId).distinct().toList()
        ).stream().collect(Collectors.toMap(Warehouse::getId, Warehouse::getName));
        Map<Long, String> binNames = binRepository.findAllById(
                content.stream().map(SerialNumber::getBinId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(WarehouseBin::getId, WarehouseBin::getName));
        Map<Long, ProductBatch> batches = productBatchRepository.findAllById(
                content.stream().map(SerialNumber::getBatchId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(ProductBatch::getId, b -> b));

        return PageResponse.of(page.map(sn -> {
            Product product = products.get(sn.getProductId());
            ProductBatch batch = sn.getBatchId() == null ? null : batches.get(sn.getBatchId());
            return SerialNumberResponse.builder()
                    .id(sn.getId())
                    .companyId(sn.getCompanyId())
                    .productId(sn.getProductId())
                    .productName(product == null ? null : product.getName())
                    .productSku(product == null ? null : product.getSku())
                    .warehouseId(sn.getWarehouseId())
                    .warehouseName(warehouseNames.get(sn.getWarehouseId()))
                    .binId(sn.getBinId())
                    .binName(sn.getBinId() == null ? null : binNames.get(sn.getBinId()))
                    .batchNumber(batch == null ? null : batch.getBatchNumber())
                    .expirationDate(batch == null ? null : batch.getExpirationDate())
                    .serialNumber(sn.getSerialNumber())
                    .status(sn.getStatus().name())
                    .createdAt(sn.getCreatedAt())
                    .build();
        }));
    }
}
