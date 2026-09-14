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
import com.example.erp.repository.ProductRepository;
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
        if (repository.findByProductIdAndPriceGroupId(request.getProductId(), request.getPriceGroupId()).isPresent()) {
            throw new AppException(HttpStatus.CONFLICT, "A price for this product already exists in the selected price group");
        }
        ProductPrice productPrice = ProductPrice.builder()
                .productId(request.getProductId())
                .priceGroupId(request.getPriceGroupId())
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
        if (repository.existsByProductIdAndPriceGroupIdAndIdNot(request.getProductId(), request.getPriceGroupId(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "A price for this product already exists in the selected price group");
        }
        productPrice.setProductId(request.getProductId());
        productPrice.setPriceGroupId(request.getPriceGroupId());
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

    private ProductPriceResponse toResponse(ProductPrice productPrice, Product product, String priceGroupName) {
        return ProductPriceResponse.builder()
                .id(productPrice.getId())
                .productId(productPrice.getProductId())
                .productName(product == null ? null : product.getName())
                .productSku(product == null ? null : product.getSku())
                .priceGroupId(productPrice.getPriceGroupId())
                .priceGroupName(priceGroupName)
                .price(productPrice.getPrice())
                .build();
    }
}
