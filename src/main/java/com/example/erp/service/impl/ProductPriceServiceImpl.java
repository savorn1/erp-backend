package com.example.erp.service.impl;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ProductPriceFilterRequest;
import com.example.erp.dto.ProductPriceRequest;
import com.example.erp.dto.ProductPriceResponse;
import com.example.erp.entity.PriceGroup;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductPrice;
import com.example.erp.exception.AppException;
import com.example.erp.repository.PriceGroupRepository;
import com.example.erp.repository.ProductPriceRepository;
import com.example.erp.entity.ProductUom;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.ProductUomRepository;
import com.example.erp.repository.UnitOfMeasureRepository;
import com.example.erp.service.ProductPriceService;
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
public class ProductPriceServiceImpl implements ProductPriceService {

    private final ProductPriceRepository repository;
    private final ProductRepository productRepository;
    private final PriceGroupRepository priceGroupRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final ProductUomRepository productUomRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductPriceResponse> list(ProductPriceFilterRequest filter) {
        List<Specification<ProductPrice>> conditions = new ArrayList<>();
        if (filter.getProductId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("productId"), filter.getProductId()));
        }
        if (filter.getPriceGroupId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("priceGroupId"), filter.getPriceGroupId()));
        }
        Specification<ProductPrice> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<ProductPrice> page = repository.findAll(spec, pageable);
        List<ProductPrice> content = page.getContent();
        Map<Long, Product> products = productRepository.findAllById(
                content.stream().map(ProductPrice::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, String> priceGroupNames = priceGroupRepository.findAllById(
                content.stream().map(ProductPrice::getPriceGroupId).distinct().toList()
        ).stream().collect(Collectors.toMap(PriceGroup::getId, PriceGroup::getName));

        return PageResponse.of(page.map(pp -> toResponse(pp, products.get(pp.getProductId()), priceGroupNames.get(pp.getPriceGroupId()))));
    }

    @Override
    public ProductPriceResponse get(Long id) {
        ProductPrice productPrice = find(id);
        Product product = productRepository.findById(productPrice.getProductId()).orElse(null);
        String priceGroupName = priceGroupRepository.findById(productPrice.getPriceGroupId()).map(PriceGroup::getName).orElse(null);
        return toResponse(productPrice, product, priceGroupName);
    }

    @Override
    @Transactional
    public ProductPriceResponse create(ProductPriceRequest request) {
        Product product = requireProduct(request.getProductId());
        PriceGroup priceGroup = requirePriceGroup(request.getPriceGroupId());
        Long unitId = normaliseUnit(product, request.getUnitOfMeasureId());
        if (existsFor(request.getProductId(), request.getPriceGroupId(), unitId, null)) {
            throw new AppException(HttpStatus.CONFLICT, conflictMessage(unitId));
        }
        ProductPrice productPrice = ProductPrice.builder()
                .productId(request.getProductId())
                .priceGroupId(request.getPriceGroupId())
                .unitOfMeasureId(unitId)
                .price(request.getPrice())
                .build();
        repository.save(productPrice);
        return toResponse(productPrice, product, priceGroup.getName());
    }

    @Override
    @Transactional
    public ProductPriceResponse update(Long id, ProductPriceRequest request) {
        ProductPrice productPrice = find(id);
        Product product = requireProduct(request.getProductId());
        PriceGroup priceGroup = requirePriceGroup(request.getPriceGroupId());
        Long unitId = normaliseUnit(product, request.getUnitOfMeasureId());
        if (existsFor(request.getProductId(), request.getPriceGroupId(), unitId, id)) {
            throw new AppException(HttpStatus.CONFLICT, conflictMessage(unitId));
        }
        productPrice.setProductId(request.getProductId());
        productPrice.setPriceGroupId(request.getPriceGroupId());
        productPrice.setUnitOfMeasureId(unitId);
        productPrice.setPrice(request.getPrice());
        repository.save(productPrice);
        return toResponse(productPrice, product, priceGroup.getName());
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

    private PriceGroup requirePriceGroup(Long priceGroupId) {
        return priceGroupRepository.findById(priceGroupId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Price group not found with id: " + priceGroupId));
    }

    private ProductPrice find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Product price not found with id: " + id));
    }

    // The unit the price is actually per: the priced unit when the row names
    // one, otherwise the product's base unit. One lookup per response rather
    // than a batch — this runs on a page of at most a few dozen rows.
    private String pricedUnitAbbreviation(Product product, Long pricedUnitId) {
        Long unitId = pricedUnitId != null ? pricedUnitId
                : (product == null ? null : product.getUnitOfMeasureId());
        if (unitId == null) {
            return null;
        }
        return unitOfMeasureRepository.findById(unitId)
                .map(com.example.erp.entity.UnitOfMeasure::getAbbreviation)
                .orElse(null);
    }

    /**
     * Collapses "the product's own base unit" to null.
     *
     * <p>Rows written before per-unit pricing carry null and mean the base
     * unit, so normalising here keeps one representation of the base price
     * instead of two that would both satisfy the uniqueness check and then
     * race each other during lookup.
     *
     * <p>Any other unit has to be a sales-allowed UOM on the product: pricing
     * a unit the product cannot be sold in would create a row nothing ever
     * reads.
     */
    private Long normaliseUnit(Product product, Long requestedUnitOfMeasureId) {
        if (requestedUnitOfMeasureId == null || requestedUnitOfMeasureId.equals(product.getUnitOfMeasureId())) {
            return null;
        }
        ProductUom productUom = productUomRepository
                .findByProductIdAndVariantIdIsNullAndUnitOfMeasureId(product.getId(), requestedUnitOfMeasureId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST,
                        "This unit is not configured for " + product.getName() + " — add it under the product's UOMs first"));
        if (!productUom.isAllowSales()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "This unit is not allowed for selling " + product.getName());
        }
        return requestedUnitOfMeasureId;
    }

    private boolean existsFor(Long productId, Long priceGroupId, Long unitId, Long excludingId) {
        if (unitId == null) {
            return excludingId == null
                    ? repository.existsByProductIdAndPriceGroupIdAndUnitOfMeasureIdIsNull(productId, priceGroupId)
                    : repository.existsByProductIdAndPriceGroupIdAndUnitOfMeasureIdIsNullAndIdNot(productId, priceGroupId, excludingId);
        }
        return excludingId == null
                ? repository.existsByProductIdAndPriceGroupIdAndUnitOfMeasureId(productId, priceGroupId, unitId)
                : repository.existsByProductIdAndPriceGroupIdAndUnitOfMeasureIdAndIdNot(productId, priceGroupId, unitId, excludingId);
    }

    private String conflictMessage(Long unitId) {
        return unitId == null
                ? "A base-unit price for this product already exists in the selected price group"
                : "A price for this product in that unit already exists in the selected price group";
    }

    private ProductPriceResponse toResponse(ProductPrice productPrice, Product product, String priceGroupName) {
        return ProductPriceResponse.builder()
                .id(productPrice.getId())
                .productId(productPrice.getProductId())
                .productName(product == null ? null : product.getName())
                .productSku(product == null ? null : product.getSku())
                .priceGroupId(productPrice.getPriceGroupId())
                .priceGroupName(priceGroupName)
                .price(productPrice.getPrice())
                .unitOfMeasureId(productPrice.getUnitOfMeasureId())
                .unitOfMeasureAbbreviation(pricedUnitAbbreviation(product, productPrice.getUnitOfMeasureId()))
                .perUnitPrice(productPrice.getUnitOfMeasureId() != null)
                .build();
    }
}
