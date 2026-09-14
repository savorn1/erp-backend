package com.example.erp.service.impl;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ProductVariantFilterRequest;
import com.example.erp.dto.ProductVariantRequest;
import com.example.erp.dto.ProductVariantResponse;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductVariant;
import com.example.erp.entity.UnitOfMeasure;
import com.example.erp.exception.AppException;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.ProductVariantRepository;
import com.example.erp.repository.UnitOfMeasureRepository;
import com.example.erp.service.ProductVariantService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository repository;
    private final ProductRepository productRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductVariantResponse> list(ProductVariantFilterRequest filter) {
        List<Specification<ProductVariant>> conditions = new ArrayList<>();
        if (filter.getName() != null && !filter.getName().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        }
        if (filter.getSku() != null && !filter.getSku().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("sku")), "%" + filter.getSku().toLowerCase() + "%"));
        }
        if (filter.getProductId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("productId"), filter.getProductId()));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        Specification<ProductVariant> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<ProductVariant> page = repository.findAll(spec, pageable);
        List<ProductVariant> content = page.getContent();
        Map<Long, Product> products = productRepository.findAllById(
                content.stream().map(ProductVariant::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, UnitOfMeasure> units = unitOfMeasureRepository.findAllById(
                content.stream().map(ProductVariant::getUnitOfMeasureId).filter(java.util.Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(UnitOfMeasure::getId, u -> u));

        return PageResponse.of(page.map(v -> toResponse(v, products.get(v.getProductId()),
                v.getUnitOfMeasureId() == null ? null : units.get(v.getUnitOfMeasureId()))));
    }

    @Override
    public ProductVariantResponse get(Long id) {
        ProductVariant variant = find(id);
        return toResponse(variant, productRepository.findById(variant.getProductId()).orElse(null), unitOf(variant.getUnitOfMeasureId()));
    }

    @Override
    @Transactional
    public ProductVariantResponse create(ProductVariantRequest request) {
        Product product = requireProduct(request.getProductId());
        if (repository.existsBySku(request.getSku())) {
            throw new AppException(HttpStatus.CONFLICT, "SKU already taken: " + request.getSku());
        }
        if (request.getBarcode() != null && !request.getBarcode().isBlank() && repository.existsByBarcode(request.getBarcode())) {
            throw new AppException(HttpStatus.CONFLICT, "Barcode already taken: " + request.getBarcode());
        }
        UnitOfMeasure unit = requireUnitIfSet(request.getUnitOfMeasureId());
        ProductVariant variant = ProductVariant.builder()
                .productId(request.getProductId())
                .name(request.getName())
                .sku(request.getSku())
                .barcode(blankToNull(request.getBarcode()))
                .unitOfMeasureId(request.getUnitOfMeasureId())
                .costPrice(request.getCostPrice())
                .sellingPrice(request.getSellingPrice())
                .imageUrl(request.getImageUrl())
                .active(request.isActive())
                .build();
        repository.save(variant);
        return toResponse(variant, product, unit);
    }

    @Override
    @Transactional
    public ProductVariantResponse update(Long id, ProductVariantRequest request) {
        ProductVariant variant = find(id);
        Product product = requireProduct(request.getProductId());
        if (repository.existsBySkuAndIdNot(request.getSku(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "SKU already taken: " + request.getSku());
        }
        if (request.getBarcode() != null && !request.getBarcode().isBlank()
                && repository.existsByBarcodeAndIdNot(request.getBarcode(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "Barcode already taken: " + request.getBarcode());
        }
        UnitOfMeasure unit = requireUnitIfSet(request.getUnitOfMeasureId());
        variant.setProductId(request.getProductId());
        variant.setName(request.getName());
        variant.setSku(request.getSku());
        variant.setBarcode(blankToNull(request.getBarcode()));
        variant.setUnitOfMeasureId(request.getUnitOfMeasureId());
        variant.setCostPrice(request.getCostPrice());
        variant.setSellingPrice(request.getSellingPrice());
        variant.setImageUrl(request.getImageUrl());
        variant.setActive(request.isActive());
        repository.save(variant);
        return toResponse(variant, product, unit);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private Product requireProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Product not found with id: " + productId));
    }

    private UnitOfMeasure requireUnitIfSet(Long unitOfMeasureId) {
        if (unitOfMeasureId == null) return null;
        return unitOfMeasureRepository.findById(unitOfMeasureId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Unit of measure not found with id: " + unitOfMeasureId));
    }

    private UnitOfMeasure unitOf(Long unitOfMeasureId) {
        return unitOfMeasureId == null ? null : unitOfMeasureRepository.findById(unitOfMeasureId).orElse(null);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private ProductVariant find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Product variant not found with id: " + id));
    }

    private ProductVariantResponse toResponse(ProductVariant variant, Product product, UnitOfMeasure unit) {
        return ProductVariantResponse.builder()
                .id(variant.getId())
                .productId(variant.getProductId())
                .productName(product == null ? null : product.getName())
                .productSku(product == null ? null : product.getSku())
                .name(variant.getName())
                .sku(variant.getSku())
                .barcode(variant.getBarcode())
                .unitOfMeasureId(variant.getUnitOfMeasureId())
                .unitOfMeasureName(unit == null ? null : unit.getName())
                .unitOfMeasureAbbreviation(unit == null ? null : unit.getAbbreviation())
                .costPrice(variant.getCostPrice())
                .sellingPrice(variant.getSellingPrice())
                .imageUrl(variant.getImageUrl())
                .active(variant.isActive())
                .build();
    }
}
