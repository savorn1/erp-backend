package com.example.erp.service.impl;

import com.example.erp.dto.CreateProductRequest;
import com.example.erp.dto.ImportResultResponse;
import com.example.erp.dto.ImportRowError;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ProductFilterRequest;
import com.example.erp.dto.ProductResponse;
import com.example.erp.dto.UpdateProductRequest;
import com.example.erp.dto.UpdateProductStatusRequest;
import com.example.erp.entity.Company;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductBrand;
import com.example.erp.entity.ProductCategory;
import com.example.erp.entity.ProductTrackingType;
import com.example.erp.entity.ProductType;
import com.example.erp.entity.ProductUom;
import com.example.erp.entity.Supplier;
import com.example.erp.entity.UnitOfMeasure;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.ProductBrandRepository;
import com.example.erp.repository.ProductCategoryRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.ProductTypeRepository;
import com.example.erp.repository.ProductUomRepository;
import com.example.erp.repository.ProductVariantRepository;
import com.example.erp.repository.SupplierRepository;
import com.example.erp.repository.UnitOfMeasureRepository;
import com.example.erp.service.ProductService;
import com.example.erp.util.CsvUtils;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CompanyRepository companyRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductBrandRepository brandRepository;
    private final ProductTypeRepository typeRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final SupplierRepository supplierRepository;
    private final ProductUomRepository productUomRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> listProducts(ProductFilterRequest filter) {
        List<Specification<Product>> conditions = new ArrayList<>();
        if (filter.getName() != null && !filter.getName().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        }
        if (filter.getSku() != null && !filter.getSku().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("sku")), "%" + filter.getSku().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getCategoryId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("categoryId"), filter.getCategoryId()));
        }
        if (filter.getBrandId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("brandId"), filter.getBrandId()));
        }
        if (filter.getTypeId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("typeId"), filter.getTypeId()));
        }
        if (filter.getSupplierId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("supplierId"), filter.getSupplierId()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        Specification<Product> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<Product> page = productRepository.findAll(spec, pageable);
        List<Product> content = page.getContent();

        // Batch-resolved rather than looked up per row — same reasoning as
        // BranchServiceImpl/DepartmentServiceImpl (N+1 avoidance on plain FK columns).
        Map<Long, String> companyNames = companyRepository.findAllById(
                content.stream().map(Product::getCompanyId).distinct().toList()
        ).stream().collect(Collectors.toMap(Company::getId, Company::getName));
        Map<Long, String> categoryNames = categoryRepository.findAllById(
                content.stream().map(Product::getCategoryId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(ProductCategory::getId, ProductCategory::getName));
        Map<Long, String> brandNames = brandRepository.findAllById(
                content.stream().map(Product::getBrandId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(ProductBrand::getId, ProductBrand::getName));
        Map<Long, String> typeNames = typeRepository.findAllById(
                content.stream().map(Product::getTypeId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(ProductType::getId, ProductType::getName));
        List<Long> productIds = content.stream().map(Product::getId).toList();
        Map<Long, ProductUom> defaultPurchaseByProduct = productUomRepository
                .findByProductIdInAndVariantIdIsNullAndDefaultPurchaseTrue(productIds).stream()
                .collect(Collectors.toMap(ProductUom::getProductId, r -> r, (a, b) -> a));
        Map<Long, ProductUom> defaultSalesByProduct = productUomRepository
                .findByProductIdInAndVariantIdIsNullAndDefaultSalesTrue(productIds).stream()
                .collect(Collectors.toMap(ProductUom::getProductId, r -> r, (a, b) -> a));

        Map<Long, UnitOfMeasure> uoms = unitOfMeasureRepository.findAllById(
                Stream.of(
                        content.stream().map(Product::getUnitOfMeasureId),
                        defaultPurchaseByProduct.values().stream().map(ProductUom::getUnitOfMeasureId),
                        defaultSalesByProduct.values().stream().map(ProductUom::getUnitOfMeasureId)
                ).flatMap(s -> s).distinct().toList()
        ).stream().collect(Collectors.toMap(UnitOfMeasure::getId, u -> u));
        Map<Long, String> supplierNames = supplierRepository.findAllById(
                content.stream().map(Product::getSupplierId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(Supplier::getId, Supplier::getName));

        return PageResponse.of(page.map(p -> {
            UnitOfMeasure baseUnit = uoms.get(p.getUnitOfMeasureId());
            ProductUom purchaseRow = defaultPurchaseByProduct.get(p.getId());
            ProductUom salesRow = defaultSalesByProduct.get(p.getId());
            UnitOfMeasure purchaseUnit = purchaseRow == null ? baseUnit : uoms.get(purchaseRow.getUnitOfMeasureId());
            UnitOfMeasure salesUnit = salesRow == null ? baseUnit : uoms.get(salesRow.getUnitOfMeasureId());
            return toResponse(p,
                    companyNames.get(p.getCompanyId()),
                    p.getCategoryId() == null ? null : categoryNames.get(p.getCategoryId()),
                    p.getBrandId() == null ? null : brandNames.get(p.getBrandId()),
                    p.getTypeId() == null ? null : typeNames.get(p.getTypeId()),
                    baseUnit, purchaseUnit, salesUnit,
                    p.getSupplierId() == null ? null : supplierNames.get(p.getSupplierId()));
        }));
    }

    @Override
    public ProductResponse getProduct(Long id) {
        Product product = find(id);
        UnitOfMeasure baseUnit = unitOfMeasureRepository.findById(product.getUnitOfMeasureId()).orElse(null);
        UnitOfMeasure purchaseUnit = productUomRepository.findByProductIdAndVariantIdIsNullAndDefaultPurchaseTrue(id)
                .map(r -> unitOfMeasureRepository.findById(r.getUnitOfMeasureId()).orElse(null))
                .orElse(baseUnit);
        UnitOfMeasure salesUnit = productUomRepository.findByProductIdAndVariantIdIsNullAndDefaultSalesTrue(id)
                .map(r -> unitOfMeasureRepository.findById(r.getUnitOfMeasureId()).orElse(null))
                .orElse(baseUnit);
        return toResponse(product,
                companyNameOf(product.getCompanyId()),
                product.getCategoryId() == null ? null : categoryRepository.findById(product.getCategoryId()).map(ProductCategory::getName).orElse(null),
                product.getBrandId() == null ? null : brandRepository.findById(product.getBrandId()).map(ProductBrand::getName).orElse(null),
                product.getTypeId() == null ? null : typeRepository.findById(product.getTypeId()).map(ProductType::getName).orElse(null),
                baseUnit, purchaseUnit, salesUnit,
                product.getSupplierId() == null ? null : supplierRepository.findById(product.getSupplierId()).map(Supplier::getName).orElse(null));
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Company company = requireCompany(request.getCompanyId());
        validateReferences(request.getCompanyId(), request.getCategoryId(), request.getBrandId(), request.getTypeId(),
                request.getUnitOfMeasureId(), request.getSupplierId());
        if (productRepository.existsByCompanyIdAndSku(request.getCompanyId(), request.getSku())) {
            throw new AppException(HttpStatus.CONFLICT, "SKU already taken in this company: " + request.getSku());
        }
        if (request.getBarcode() != null && !request.getBarcode().isBlank()
                && productRepository.existsByCompanyIdAndBarcode(request.getCompanyId(), request.getBarcode())) {
            throw new AppException(HttpStatus.CONFLICT, "Barcode already taken in this company: " + request.getBarcode());
        }

        Product product = Product.builder()
                .companyId(request.getCompanyId())
                .categoryId(request.getCategoryId())
                .brandId(request.getBrandId())
                .typeId(request.getTypeId())
                .unitOfMeasureId(request.getUnitOfMeasureId())
                .supplierId(request.getSupplierId())
                .name(request.getName())
                .description(request.getDescription())
                .sku(request.getSku())
                .barcode(blankToNull(request.getBarcode()))
                .costPrice(request.getCostPrice())
                .sellingPrice(request.getSellingPrice())
                .taxRate(request.getTaxRate())
                .trackingType(request.getTrackingType())
                .imageUrl(request.getImageUrl())
                .reorderPoint(request.getReorderPoint())
                .maxStock(request.getMaxStock())
                .warrantyMonths(request.getWarrantyMonths())
                .build();
        productRepository.save(product);
        return getProduct(product.getId());
    }

    @Override
    public ImportResultResponse importProductsFromCsv(MultipartFile file, Long companyId) {
        requireCompany(companyId);
        List<CSVRecord> records = CsvUtils.parse(file);
        List<ImportRowError> errors = new ArrayList<>();
        int successCount = 0;

        for (CSVRecord record : records) {
            int rowNumber = (int) record.getRecordNumber() + 1;
            try {
                CreateProductRequest request = new CreateProductRequest();
                request.setCompanyId(companyId);
                request.setName(CsvUtils.getRequired(record, "name"));
                request.setSku(CsvUtils.getRequired(record, "sku"));
                request.setBarcode(CsvUtils.getOptional(record, "barcode"));
                request.setDescription(CsvUtils.getOptional(record, "description"));
                request.setCategoryId(resolveByName("category", CsvUtils.getOptional(record, "category"), categoryRepository::findByNameIgnoreCase, ProductCategory::getId));
                request.setBrandId(resolveByName("brand", CsvUtils.getOptional(record, "brand"), brandRepository::findByNameIgnoreCase, ProductBrand::getId));
                request.setTypeId(resolveByName("type", CsvUtils.getOptional(record, "type"), typeRepository::findByNameIgnoreCase, ProductType::getId));
                String uomName = CsvUtils.getRequired(record, "unitOfMeasure");
                request.setUnitOfMeasureId(resolveByName("unitOfMeasure", uomName, unitOfMeasureRepository::findByNameIgnoreCase, UnitOfMeasure::getId));
                String supplierName = CsvUtils.getOptional(record, "supplier");
                if (supplierName != null) {
                    Supplier supplier = supplierRepository.findByCompanyIdAndNameIgnoreCase(companyId, supplierName)
                            .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Supplier not found: " + supplierName));
                    request.setSupplierId(supplier.getId());
                }
                request.setCostPrice(CsvUtils.getRequiredDecimal(record, "costPrice"));
                request.setSellingPrice(CsvUtils.getRequiredDecimal(record, "sellingPrice"));
                request.setTaxRate(CsvUtils.getDecimal(record, "taxRate", BigDecimal.ZERO));
                String trackingType = CsvUtils.getOptional(record, "trackingType");
                request.setTrackingType(trackingType == null ? ProductTrackingType.NONE : ProductTrackingType.valueOf(trackingType.toUpperCase()));
                request.setReorderPoint(CsvUtils.getDecimal(record, "reorderPoint", null));
                request.setMaxStock(CsvUtils.getDecimal(record, "maxStock", null));

                createProduct(request);
                successCount++;
            } catch (Exception e) {
                errors.add(ImportRowError.builder().rowNumber(rowNumber).message(e.getMessage()).build());
            }
        }

        return ImportResultResponse.builder()
                .totalRows(records.size())
                .successCount(successCount)
                .failureCount(errors.size())
                .errors(errors)
                .build();
    }

    private <T> Long resolveByName(String fieldLabel, String name, java.util.function.Function<String, java.util.Optional<T>> lookup, java.util.function.Function<T, Long> idOf) {
        if (name == null) return null;
        T entity = lookup.apply(name).orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, fieldLabel + " not found: " + name));
        return idOf.apply(entity);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = find(id);
        requireCompany(request.getCompanyId());
        validateReferences(request.getCompanyId(), request.getCategoryId(), request.getBrandId(), request.getTypeId(),
                request.getUnitOfMeasureId(), request.getSupplierId());
        if (productRepository.existsByCompanyIdAndSkuAndIdNot(request.getCompanyId(), request.getSku(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "SKU already taken in this company: " + request.getSku());
        }
        if (request.getBarcode() != null && !request.getBarcode().isBlank()
                && productRepository.existsByCompanyIdAndBarcodeAndIdNot(request.getCompanyId(), request.getBarcode(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "Barcode already taken in this company: " + request.getBarcode());
        }

        Long oldUnitOfMeasureId = product.getUnitOfMeasureId();
        product.setCompanyId(request.getCompanyId());
        product.setCategoryId(request.getCategoryId());
        product.setBrandId(request.getBrandId());
        product.setTypeId(request.getTypeId());
        product.setUnitOfMeasureId(request.getUnitOfMeasureId());
        product.setSupplierId(request.getSupplierId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setBarcode(blankToNull(request.getBarcode()));
        product.setCostPrice(request.getCostPrice());
        product.setSellingPrice(request.getSellingPrice());
        product.setTaxRate(request.getTaxRate());
        product.setTrackingType(request.getTrackingType());
        product.setImageUrl(request.getImageUrl());
        product.setReorderPoint(request.getReorderPoint());
        product.setMaxStock(request.getMaxStock());
        product.setWarrantyMonths(request.getWarrantyMonths());
        productRepository.save(product);
        if (!Objects.equals(oldUnitOfMeasureId, request.getUnitOfMeasureId())) {
            reconcileBaseUnitChange(product.getId(), request.getUnitOfMeasureId());
        }
        return getProduct(id);
    }

    // The product's own unit of measure just changed, so whichever ProductUom
    // row(s) were flagged as "the base unit" (one per variant scope — see
    // ProductUomServiceImpl.ensureBaseUnitRow) no longer describe it. Their
    // conversionFactor of 1 was only ever valid against the *old* base, so
    // it can't simply be repointed at the new one — demote and deactivate
    // instead of leaving a second, contradictory "Base" row lying around
    // that also silently blocks deletion (see ProductUomServiceImpl.delete).
    private void reconcileBaseUnitChange(Long productId, Long newUnitOfMeasureId) {
        for (ProductUom row : productUomRepository.findByProductIdAndBaseUnitTrue(productId)) {
            if (row.getUnitOfMeasureId().equals(newUnitOfMeasureId)) continue;
            row.setBaseUnit(false);
            row.setDefaultPurchase(false);
            row.setDefaultSales(false);
            row.setActive(false);
            productUomRepository.save(row);
        }
    }

    @Override
    @Transactional
    public ProductResponse updateStatus(Long id, UpdateProductStatusRequest request) {
        Product product = find(id);
        product.setStatus(request.getStatus());
        productRepository.save(product);
        return getProduct(id);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        find(id);
        if (productVariantRepository.existsByProductId(id)) {
            throw new AppException(HttpStatus.CONFLICT, "Cannot delete a product that has variants — delete them first");
        }
        productRepository.deleteById(id);
    }

    private void validateReferences(Long companyId, Long categoryId, Long brandId, Long typeId,
                                     Long unitOfMeasureId, Long supplierId) {
        if (categoryId != null && !categoryRepository.existsById(categoryId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Category not found with id: " + categoryId);
        }
        if (brandId != null && !brandRepository.existsById(brandId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Brand not found with id: " + brandId);
        }
        if (typeId != null && !typeRepository.existsById(typeId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Type not found with id: " + typeId);
        }
        if (!unitOfMeasureRepository.existsById(unitOfMeasureId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Unit of measure not found with id: " + unitOfMeasureId);
        }
        if (supplierId != null) requireSameCompany(supplierRepository.findById(supplierId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Supplier not found with id: " + supplierId))
                .getCompanyId(), companyId, "Supplier");
    }

    private void requireSameCompany(Long referenceCompanyId, Long companyId, String label) {
        if (!referenceCompanyId.equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, label + " does not belong to the selected company");
        }
    }

    private Company requireCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private String companyNameOf(Long companyId) {
        return companyRepository.findById(companyId).map(Company::getName).orElse(null);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private Product find(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Product not found with id: " + id));
    }

    private ProductResponse toResponse(Product product, String companyName, String categoryName, String brandName,
                                        String typeName, UnitOfMeasure uom, UnitOfMeasure purchaseUnit, UnitOfMeasure salesUnit,
                                        String supplierName) {
        return ProductResponse.builder()
                .id(product.getId())
                .companyId(product.getCompanyId())
                .companyName(companyName)
                .categoryId(product.getCategoryId())
                .categoryName(categoryName)
                .brandId(product.getBrandId())
                .brandName(brandName)
                .typeId(product.getTypeId())
                .typeName(typeName)
                .unitOfMeasureId(product.getUnitOfMeasureId())
                .unitOfMeasureName(uom == null ? null : uom.getName())
                .unitOfMeasureAbbreviation(uom == null ? null : uom.getAbbreviation())
                .purchaseUnitOfMeasureId(purchaseUnit == null ? null : purchaseUnit.getId())
                .purchaseUnitOfMeasureAbbreviation(purchaseUnit == null ? null : purchaseUnit.getAbbreviation())
                .salesUnitOfMeasureId(salesUnit == null ? null : salesUnit.getId())
                .salesUnitOfMeasureAbbreviation(salesUnit == null ? null : salesUnit.getAbbreviation())
                .supplierId(product.getSupplierId())
                .supplierName(supplierName)
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .barcode(product.getBarcode())
                .costPrice(product.getCostPrice())
                .sellingPrice(product.getSellingPrice())
                .taxRate(product.getTaxRate())
                .status(product.getStatus().name())
                // Existing rows created before this field was added read back
                // as null — treat that the same as NONE rather than crashing.
                .trackingType((product.getTrackingType() == null ? ProductTrackingType.NONE : product.getTrackingType()).name())
                .imageUrl(product.getImageUrl())
                .reorderPoint(product.getReorderPoint() == null ? BigDecimal.ZERO : product.getReorderPoint())
                .maxStock(product.getMaxStock() == null ? BigDecimal.ZERO : product.getMaxStock())
                .warrantyMonths(product.getWarrantyMonths())
                .build();
    }
}
