package com.example.erp.service.impl;

import com.example.erp.dto.ConvertUomRequest;
import com.example.erp.dto.ConvertUomResponse;
import com.example.erp.dto.ProductUomRequest;
import com.example.erp.dto.ProductUomResponse;
import com.example.erp.entity.PriceGroup;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductUom;
import com.example.erp.entity.ProductUomPrice;
import com.example.erp.entity.ProductVariant;
import com.example.erp.entity.UnitOfMeasure;
import com.example.erp.exception.AppException;
import com.example.erp.repository.PriceGroupRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.ProductUomPriceRepository;
import com.example.erp.repository.ProductUomRepository;
import com.example.erp.repository.ProductVariantRepository;
import com.example.erp.repository.UnitOfMeasureRepository;
import com.example.erp.service.ProductUomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductUomServiceImpl implements ProductUomService {

    private final ProductUomRepository productUomRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final ProductUomPriceRepository productUomPriceRepository;
    private final PriceGroupRepository priceGroupRepository;

    @Override
    @Transactional
    public List<ProductUomResponse> list(Long productId, Long variantId, Long priceGroupId) {
        Product product = requireProduct(productId);
        ProductVariant variant = requireVariant(variantId, productId);
        ensureBaseUnitRow(product, variantId);
        PriceGroup priceGroup = priceGroupId == null ? null : priceGroupRepository.findById(priceGroupId).orElse(null);
        return rowsFor(productId, variantId).stream().map(row -> toResponse(row, product, variant, priceGroup)).toList();
    }

    @Override
    @Transactional
    public ProductUomResponse create(Long productId, Long variantId, ProductUomRequest request) {
        Product product = requireProduct(productId);
        ProductVariant variant = requireVariant(variantId, productId);
        ensureBaseUnitRow(product, variantId);
        UnitOfMeasure unit = requireUnit(request.getUnitOfMeasureId());
        if (existsFor(productId, variantId, unit.getId())) {
            throw new AppException(HttpStatus.CONFLICT, "This already has a UOM row for " + unit.getName());
        }
        requireUniqueBarcode(request.getBarcode(), product.getCompanyId(), null);

        boolean isBase = unit.getId().equals(product.getUnitOfMeasureId());
        BigDecimal factor = isBase ? BigDecimal.ONE : requireConversionFactor(request);

        ProductUom row = ProductUom.builder()
                .productId(productId)
                .variantId(variantId)
                .unitOfMeasureId(unit.getId())
                .conversionFactor(factor)
                .baseUnit(isBase)
                .allowPurchase(request.isAllowPurchase())
                .allowSales(request.isAllowSales())
                .allowInventory(request.isAllowInventory())
                .defaultPurchase(request.isDefaultPurchase())
                .defaultSales(request.isDefaultSales())
                .barcode(blankToNull(request.getBarcode()))
                .price(request.getPrice())
                .active(request.isActive())
                .build();
        productUomRepository.save(row);
        if (row.isDefaultPurchase()) enforceSingleDefault(productId, variantId, row.getId(), true);
        if (row.isDefaultSales()) enforceSingleDefault(productId, variantId, row.getId(), false);
        return toResponse(row, product, variant, null);
    }

    @Override
    @Transactional
    public ProductUomResponse update(Long productId, Long variantId, Long id, ProductUomRequest request) {
        Product product = requireProduct(productId);
        ProductVariant variant = requireVariant(variantId, productId);
        ProductUom row = find(productId, variantId, id);
        UnitOfMeasure unit = requireUnit(request.getUnitOfMeasureId());
        if (!row.getUnitOfMeasureId().equals(unit.getId()) && existsFor(productId, variantId, unit.getId())) {
            throw new AppException(HttpStatus.CONFLICT, "This already has a UOM row for " + unit.getName());
        }
        requireUniqueBarcode(request.getBarcode(), product.getCompanyId(), id);

        boolean isBase = unit.getId().equals(product.getUnitOfMeasureId());
        if (row.isBaseUnit() && !isBase) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Cannot change the base-unit row to a different unit — update the product's unit of measure instead");
        }
        BigDecimal factor = isBase ? BigDecimal.ONE : requireConversionFactor(request);

        row.setUnitOfMeasureId(unit.getId());
        row.setConversionFactor(factor);
        row.setBaseUnit(isBase);
        row.setAllowPurchase(request.isAllowPurchase());
        row.setAllowSales(request.isAllowSales());
        row.setAllowInventory(request.isAllowInventory());
        row.setDefaultPurchase(request.isDefaultPurchase());
        row.setDefaultSales(request.isDefaultSales());
        row.setBarcode(blankToNull(request.getBarcode()));
        row.setPrice(request.getPrice());
        row.setActive(request.isActive());
        productUomRepository.save(row);
        if (row.isDefaultPurchase()) enforceSingleDefault(productId, variantId, row.getId(), true);
        if (row.isDefaultSales()) enforceSingleDefault(productId, variantId, row.getId(), false);
        return toResponse(row, product, variant, null);
    }

    @Override
    @Transactional
    public void delete(Long productId, Long variantId, Long id) {
        requireVariant(variantId, productId);
        ProductUom row = find(productId, variantId, id);
        if (row.isBaseUnit()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delete the base-unit row");
        }
        productUomPriceRepository.deleteByProductUomId(id);
        productUomRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ConvertUomResponse convert(ConvertUomRequest request) {
        Product product = requireProduct(request.getProductId());
        requireVariant(request.getVariantId(), request.getProductId());
        ProductUom fromRow = findFor(request.getProductId(), request.getVariantId(), request.getFromUnitOfMeasureId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Source unit is not configured"));
        ProductUom toRow = findFor(request.getProductId(), request.getVariantId(), request.getToUnitOfMeasureId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Target unit is not configured"));

        BigDecimal baseQuantity = request.getQuantity().multiply(fromRow.getConversionFactor());
        BigDecimal convertedQuantity = baseQuantity.divide(toRow.getConversionFactor(), 6, RoundingMode.HALF_UP);

        return ConvertUomResponse.builder()
                .productId(product.getId())
                .variantId(request.getVariantId())
                .fromUnitOfMeasureId(request.getFromUnitOfMeasureId())
                .quantity(request.getQuantity())
                .toUnitOfMeasureId(request.getToUnitOfMeasureId())
                .convertedQuantity(convertedQuantity)
                .baseUnitOfMeasureId(product.getUnitOfMeasureId())
                .baseQuantity(baseQuantity)
                .build();
    }

    // Idempotent — every (product, variant) scope implicitly has the
    // product's own unitOfMeasureId as a usable UOM even before anyone
    // explicitly configures alternates. A variant shares its parent
    // product's physical base unit — it's the same item, just a SKU/color/
    // size split, not a different unit of measure.
    private void ensureBaseUnitRow(Product product, Long variantId) {
        if (existsFor(product.getId(), variantId, product.getUnitOfMeasureId())) return;
        productUomRepository.save(ProductUom.builder()
                .productId(product.getId())
                .variantId(variantId)
                .unitOfMeasureId(product.getUnitOfMeasureId())
                .conversionFactor(BigDecimal.ONE)
                .baseUnit(true)
                .allowPurchase(true)
                .allowSales(true)
                .allowInventory(true)
                .defaultPurchase(true)
                .defaultSales(true)
                .active(true)
                .build());
    }

    private void enforceSingleDefault(Long productId, Long variantId, Long keepId, boolean purchase) {
        for (ProductUom row : rowsFor(productId, variantId)) {
            if (row.getId().equals(keepId)) continue;
            if (purchase && row.isDefaultPurchase()) {
                row.setDefaultPurchase(false);
                productUomRepository.save(row);
            } else if (!purchase && row.isDefaultSales()) {
                row.setDefaultSales(false);
                productUomRepository.save(row);
            }
        }
    }

    private List<ProductUom> rowsFor(Long productId, Long variantId) {
        return variantId == null
                ? productUomRepository.findByProductIdAndVariantIdIsNull(productId)
                : productUomRepository.findByProductIdAndVariantId(productId, variantId);
    }

    private boolean existsFor(Long productId, Long variantId, Long unitOfMeasureId) {
        return variantId == null
                ? productUomRepository.existsByProductIdAndVariantIdIsNullAndUnitOfMeasureId(productId, unitOfMeasureId)
                : productUomRepository.existsByProductIdAndVariantIdAndUnitOfMeasureId(productId, variantId, unitOfMeasureId);
    }

    private Optional<ProductUom> findFor(Long productId, Long variantId, Long unitOfMeasureId) {
        return variantId == null
                ? productUomRepository.findByProductIdAndVariantIdIsNullAndUnitOfMeasureId(productId, unitOfMeasureId)
                : productUomRepository.findByProductIdAndVariantIdAndUnitOfMeasureId(productId, variantId, unitOfMeasureId);
    }

    private BigDecimal requireConversionFactor(ProductUomRequest request) {
        if (request.getConversionFactor() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Conversion factor is required for a non-base unit");
        }
        return request.getConversionFactor();
    }

    private void requireUniqueBarcode(String barcode, Long companyId, Long excludeId) {
        String normalized = blankToNull(barcode);
        if (normalized == null) return;
        boolean clash = excludeId == null
                ? productUomRepository.existsByBarcode(normalized)
                : productUomRepository.existsByBarcodeAndIdNot(normalized, excludeId);
        boolean variantClash = excludeId == null
                ? productVariantRepository.existsByBarcode(normalized)
                : productVariantRepository.existsByBarcodeAndIdNot(normalized, excludeId);
        if (clash || variantClash || productRepository.existsByCompanyIdAndBarcode(companyId, normalized)) {
            throw new AppException(HttpStatus.CONFLICT, "Barcode already in use: " + normalized);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private UnitOfMeasure requireUnit(Long unitOfMeasureId) {
        return unitOfMeasureRepository.findById(unitOfMeasureId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Unit of measure not found with id: " + unitOfMeasureId));
    }

    private Product requireProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Product not found with id: " + productId));
    }

    private ProductVariant requireVariant(Long variantId, Long productId) {
        if (variantId == null) return null;
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Product variant not found with id: " + variantId));
        if (!variant.getProductId().equals(productId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Product variant " + variantId + " does not belong to product " + productId);
        }
        return variant;
    }

    private ProductUom find(Long productId, Long variantId, Long id) {
        ProductUom row = productUomRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Product UOM not found with id: " + id));
        if (!row.getProductId().equals(productId) || !Objects.equals(row.getVariantId(), variantId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Product UOM " + id + " does not belong to this product/variant");
        }
        return row;
    }

    private ProductUomResponse toResponse(ProductUom row, Product product, ProductVariant variant, PriceGroup priceGroup) {
        UnitOfMeasure unit = unitOfMeasureRepository.findById(row.getUnitOfMeasureId()).orElse(null);
        BigDecimal basePrice = variant != null && variant.getSellingPrice() != null ? variant.getSellingPrice() : product.getSellingPrice();
        BigDecimal fallbackPrice = row.getPrice() != null ? row.getPrice() : basePrice.multiply(row.getConversionFactor());

        ProductUomPrice match = priceGroup == null ? null : findEffectivePrice(row.getId(), priceGroup.getId());
        BigDecimal effectivePrice = match != null ? match.getPrice() : fallbackPrice;

        return ProductUomResponse.builder()
                .id(row.getId())
                .productId(row.getProductId())
                .variantId(row.getVariantId())
                .unitOfMeasureId(row.getUnitOfMeasureId())
                .unitOfMeasureName(unit == null ? null : unit.getName())
                .unitOfMeasureAbbreviation(unit == null ? null : unit.getAbbreviation())
                .conversionFactor(row.getConversionFactor())
                .baseUnit(row.isBaseUnit())
                .allowPurchase(row.isAllowPurchase())
                .allowSales(row.isAllowSales())
                .allowInventory(row.isAllowInventory())
                .defaultPurchase(row.isDefaultPurchase())
                .defaultSales(row.isDefaultSales())
                .barcode(row.getBarcode())
                .effectivePrice(effectivePrice)
                .priceGroupId(match == null ? null : priceGroup.getId())
                .priceGroupName(match == null ? null : priceGroup.getName())
                .price(row.getPrice())
                .active(row.isActive())
                .build();
    }

    // Active and, if bounded, today falls within [effectiveFrom, effectiveTo].
    // A null bound on either side means unbounded in that direction.
    private ProductUomPrice findEffectivePrice(Long productUomId, Long priceGroupId) {
        return productUomPriceRepository.findByProductUomIdAndPriceGroupId(productUomId, priceGroupId)
                .filter(ProductUomPrice::isActive)
                .filter(p -> p.getEffectiveFrom() == null || !LocalDate.now().isBefore(p.getEffectiveFrom()))
                .filter(p -> p.getEffectiveTo() == null || !LocalDate.now().isAfter(p.getEffectiveTo()))
                .orElse(null);
    }
}
