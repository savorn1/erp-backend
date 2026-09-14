package com.example.erp.service.impl;

import com.example.erp.dto.ProductUomPriceRequest;
import com.example.erp.dto.ProductUomPriceResponse;
import com.example.erp.entity.PriceGroup;
import com.example.erp.entity.ProductUom;
import com.example.erp.entity.ProductUomPrice;
import com.example.erp.exception.AppException;
import com.example.erp.repository.PriceGroupRepository;
import com.example.erp.repository.ProductUomPriceRepository;
import com.example.erp.repository.ProductUomRepository;
import com.example.erp.service.ProductUomPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductUomPriceServiceImpl implements ProductUomPriceService {

    private final ProductUomPriceRepository repository;
    private final ProductUomRepository productUomRepository;
    private final PriceGroupRepository priceGroupRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductUomPriceResponse> list(Long productId, Long productUomId) {
        requireProductUom(productId, productUomId);
        List<ProductUomPrice> rows = repository.findByProductUomId(productUomId);
        Map<Long, String> priceGroupNames = priceGroupRepository.findAllById(
                rows.stream().map(ProductUomPrice::getPriceGroupId).distinct().toList()
        ).stream().collect(Collectors.toMap(PriceGroup::getId, PriceGroup::getName));
        return rows.stream().map(row -> toResponse(row, priceGroupNames.get(row.getPriceGroupId()))).toList();
    }

    @Override
    @Transactional
    public ProductUomPriceResponse create(Long productId, Long productUomId, ProductUomPriceRequest request) {
        requireProductUom(productId, productUomId);
        PriceGroup priceGroup = requirePriceGroup(request.getPriceGroupId());
        validateDateRange(request);
        if (repository.findByProductUomIdAndPriceGroupId(productUomId, request.getPriceGroupId()).isPresent()) {
            throw new AppException(HttpStatus.CONFLICT, "A price for this UOM already exists in the selected price group");
        }
        ProductUomPrice row = ProductUomPrice.builder()
                .productUomId(productUomId)
                .priceGroupId(request.getPriceGroupId())
                .price(request.getPrice())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .active(request.isActive())
                .build();
        repository.save(row);
        return toResponse(row, priceGroup.getName());
    }

    @Override
    @Transactional
    public ProductUomPriceResponse update(Long productId, Long productUomId, Long id, ProductUomPriceRequest request) {
        requireProductUom(productId, productUomId);
        ProductUomPrice row = find(productUomId, id);
        PriceGroup priceGroup = requirePriceGroup(request.getPriceGroupId());
        validateDateRange(request);
        if (repository.existsByProductUomIdAndPriceGroupIdAndIdNot(productUomId, request.getPriceGroupId(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "A price for this UOM already exists in the selected price group");
        }
        row.setPriceGroupId(request.getPriceGroupId());
        row.setPrice(request.getPrice());
        row.setEffectiveFrom(request.getEffectiveFrom());
        row.setEffectiveTo(request.getEffectiveTo());
        row.setActive(request.isActive());
        repository.save(row);
        return toResponse(row, priceGroup.getName());
    }

    @Override
    @Transactional
    public void delete(Long productId, Long productUomId, Long id) {
        requireProductUom(productId, productUomId);
        repository.delete(find(productUomId, id));
    }

    private void validateDateRange(ProductUomPriceRequest request) {
        if (request.getEffectiveFrom() != null && request.getEffectiveTo() != null
                && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Effective to date cannot be before effective from date");
        }
    }

    private ProductUom requireProductUom(Long productId, Long productUomId) {
        ProductUom productUom = productUomRepository.findById(productUomId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Product UOM not found with id: " + productUomId));
        if (!productUom.getProductId().equals(productId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Product UOM " + productUomId + " does not belong to product " + productId);
        }
        return productUom;
    }

    private PriceGroup requirePriceGroup(Long priceGroupId) {
        return priceGroupRepository.findById(priceGroupId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Price group not found with id: " + priceGroupId));
    }

    private ProductUomPrice find(Long productUomId, Long id) {
        ProductUomPrice row = repository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Product UOM price not found with id: " + id));
        if (!row.getProductUomId().equals(productUomId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Product UOM price " + id + " does not belong to product UOM " + productUomId);
        }
        return row;
    }

    private ProductUomPriceResponse toResponse(ProductUomPrice row, String priceGroupName) {
        return ProductUomPriceResponse.builder()
                .id(row.getId())
                .productUomId(row.getProductUomId())
                .priceGroupId(row.getPriceGroupId())
                .priceGroupName(priceGroupName)
                .price(row.getPrice())
                .effectiveFrom(row.getEffectiveFrom())
                .effectiveTo(row.getEffectiveTo())
                .active(row.isActive())
                .build();
    }
}
