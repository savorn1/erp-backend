package com.example.erp.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.erp.dto.BillOfMaterialFilterRequest;
import com.example.erp.dto.BillOfMaterialLineRequest;
import com.example.erp.dto.BillOfMaterialLineResponse;
import com.example.erp.dto.BillOfMaterialResponse;
import com.example.erp.dto.BomVersionRowResponse;
import com.example.erp.dto.CreateBillOfMaterialRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateBillOfMaterialRequest;
import com.example.erp.entity.BillOfMaterial;
import com.example.erp.entity.BillOfMaterialLine;
import com.example.erp.entity.BillOfMaterialStatus;
import com.example.erp.entity.Company;
import com.example.erp.entity.Product;
import com.example.erp.entity.UnitOfMeasure;
import com.example.erp.exception.AppException;
import com.example.erp.repository.BillOfMaterialLineRepository;
import com.example.erp.repository.BillOfMaterialRepository;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.ManufacturingOrderRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.UnitOfMeasureRepository;
import com.example.erp.service.BillOfMaterialService;
import com.example.erp.util.PageableUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BillOfMaterialServiceImpl implements BillOfMaterialService {

    private final BillOfMaterialRepository bomRepository;
    private final BillOfMaterialLineRepository lineRepository;
    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final ManufacturingOrderRepository manufacturingOrderRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BillOfMaterialResponse> listBoms(BillOfMaterialFilterRequest filter) {
        List<Specification<BillOfMaterial>> conditions = new ArrayList<>();
        if (filter.getBomNumber() != null && !filter.getBomNumber().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("bomNumber")), "%" + filter.getBomNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getProductId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("productId"), filter.getProductId()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        Specification<BillOfMaterial> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<BillOfMaterial> page = bomRepository.findAll(spec, pageable);
        List<BillOfMaterial> content = page.getContent();

        List<Long> bomIds = content.stream().map(BillOfMaterial::getId).toList();
        Map<Long, List<BillOfMaterialLine>> linesByBomId = bomIds.isEmpty() ? Map.of() : allLinesGroupedByBom(bomIds);

        return PageResponse.of(page.map(bom -> toFullResponse(bom, linesByBomId.getOrDefault(bom.getId(), List.of()))));
    }

    @Override
    @Transactional(readOnly = true)
    public BillOfMaterialResponse getBom(Long id) {
        BillOfMaterial bom = find(id);
        return toFullResponse(bom, lineRepository.findByBomId(id));
    }

    @Override
    @Transactional
    public BillOfMaterialResponse createBom(CreateBillOfMaterialRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());
        Product product = requireProduct(request.getProductId(), request.getCompanyId());
        validateLines(request.getLines(), request.getCompanyId(), product.getId());

        BillOfMaterial bom = BillOfMaterial.builder()
                .companyId(request.getCompanyId())
                .productId(request.getProductId())
                .name(request.getName())
                .outputQuantity(request.getOutputQuantity())
                .notes(request.getNotes())
                .createdBy(actingUsername)
                .build();
        bomRepository.save(bom);
        bom.setBomNumber("BOM-" + String.format("%06d", bom.getId()));
        bomRepository.save(bom);

        List<BillOfMaterialLine> lines = saveLines(bom.getId(), request.getLines());
        return toFullResponse(bom, lines);
    }

    @Override
    @Transactional
    public BillOfMaterialResponse updateBom(Long id, UpdateBillOfMaterialRequest request) {
        BillOfMaterial bom = find(id);
        validateLines(request.getLines(), bom.getCompanyId(), bom.getProductId());

        bom.setName(request.getName());
        bom.setOutputQuantity(request.getOutputQuantity());
        bom.setNotes(request.getNotes());
        bomRepository.save(bom);

        lineRepository.deleteByBomId(id);
        List<BillOfMaterialLine> lines = saveLines(id, request.getLines());
        return toFullResponse(bom, lines);
    }

    @Override
    @Transactional
    public BillOfMaterialResponse activateBom(Long id) {
        BillOfMaterial bom = find(id);
        bom.setStatus(BillOfMaterialStatus.ACTIVE);
        bomRepository.save(bom);
        return toFullResponse(bom, lineRepository.findByBomId(id));
    }

    @Override
    @Transactional
    public BillOfMaterialResponse deactivateBom(Long id) {
        BillOfMaterial bom = find(id);
        bom.setStatus(BillOfMaterialStatus.INACTIVE);
        bomRepository.save(bom);
        return toFullResponse(bom, lineRepository.findByBomId(id));
    }

    @Override
    @Transactional
    public BillOfMaterialResponse createNewVersion(Long id, String actingUsername) {
        BillOfMaterial original = find(id);
        List<BillOfMaterialLine> originalLines = lineRepository.findByBomId(id);

        BillOfMaterial newVersion = BillOfMaterial.builder()
                .companyId(original.getCompanyId())
                .productId(original.getProductId())
                .name(original.getName())
                .outputQuantity(original.getOutputQuantity())
                .notes(original.getNotes())
                .version((original.getVersion() == null ? 1 : original.getVersion()) + 1)
                .previousVersionId(original.getId())
                .createdBy(actingUsername)
                .build();
        bomRepository.save(newVersion);
        newVersion.setBomNumber("BOM-" + String.format("%06d", newVersion.getId()));
        bomRepository.save(newVersion);

        List<BillOfMaterialLine> newLines = originalLines.stream()
                .map(l -> BillOfMaterialLine.builder()
                        .bomId(newVersion.getId())
                        .componentProductId(l.getComponentProductId())
                        .quantity(l.getQuantity())
                        .scrapPercent(l.getScrapPercent())
                        .build())
                .toList();
        lineRepository.saveAll(newLines);

        original.setSupersededByBomId(newVersion.getId());
        original.setStatus(BillOfMaterialStatus.INACTIVE);
        bomRepository.save(original);

        return toFullResponse(newVersion, newLines);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BomVersionRowResponse> getVersionHistory(Long id) {
        BillOfMaterial requested = find(id);

        Map<Long, BillOfMaterial> chain = new HashMap<>();
        chain.put(requested.getId(), requested);

        BillOfMaterial cursor = requested;
        while (cursor.getPreviousVersionId() != null) {
            BillOfMaterial previous = bomRepository.findById(cursor.getPreviousVersionId()).orElse(null);
            if (previous == null || chain.containsKey(previous.getId())) break;
            chain.put(previous.getId(), previous);
            cursor = previous;
        }
        cursor = requested;
        while (cursor.getSupersededByBomId() != null) {
            BillOfMaterial next = bomRepository.findById(cursor.getSupersededByBomId()).orElse(null);
            if (next == null || chain.containsKey(next.getId())) break;
            chain.put(next.getId(), next);
            cursor = next;
        }

        return chain.values().stream()
                .sorted(Comparator.comparing(b -> b.getVersion() == null ? 1 : b.getVersion()))
                .map(b -> BomVersionRowResponse.builder()
                        .id(b.getId())
                        .bomNumber(b.getBomNumber())
                        .version(b.getVersion())
                        .status(b.getStatus().name())
                        .createdBy(b.getCreatedBy())
                        .createdAt(b.getCreatedAt())
                        .current(b.getId().equals(requested.getId()))
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void deleteBom(Long id) {
        BillOfMaterial bom = find(id);
        if (manufacturingOrderRepository.exists((root, query, cb) -> cb.equal(root.get("bomId"), bom.getId()))) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delete a BOM that already has manufacturing orders against it — deactivate it instead");
        }
        lineRepository.deleteByBomId(id);
        bomRepository.delete(bom);
    }

    private void validateLines(List<BillOfMaterialLineRequest> lines, Long companyId, Long finishedProductId) {
        List<Long> componentIds = lines.stream().map(BillOfMaterialLineRequest::getComponentProductId).distinct().toList();
        Map<Long, Product> products = productRepository.findAllById(componentIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        for (BillOfMaterialLineRequest line : lines) {
            if (line.getComponentProductId().equals(finishedProductId)) {
                throw new AppException(HttpStatus.BAD_REQUEST, "A product cannot be a component of its own BOM");
            }
            Product product = products.get(line.getComponentProductId());
            if (product == null) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Product not found with id: " + line.getComponentProductId());
            }
            if (!product.getCompanyId().equals(companyId)) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Component does not belong to the selected company: " + product.getName());
            }
        }
    }

    private List<BillOfMaterialLine> saveLines(Long bomId, List<BillOfMaterialLineRequest> requests) {
        List<BillOfMaterialLine> lines = requests.stream()
                .map(r -> BillOfMaterialLine.builder()
                        .bomId(bomId)
                        .componentProductId(r.getComponentProductId())
                        .quantity(r.getQuantity())
                        .scrapPercent(r.getScrapPercent())
                        .build())
                .toList();
        return lineRepository.saveAll(lines);
    }

    private Map<Long, List<BillOfMaterialLine>> allLinesGroupedByBom(List<Long> bomIds) {
        List<BillOfMaterialLine> all = new ArrayList<>();
        for (Long bomId : bomIds) {
            all.addAll(lineRepository.findByBomId(bomId));
        }
        return all.stream().collect(Collectors.groupingBy(BillOfMaterialLine::getBomId));
    }

    private BillOfMaterialResponse toFullResponse(BillOfMaterial bom, List<BillOfMaterialLine> lines) {
        Company company = companyRepository.findById(bom.getCompanyId()).orElse(null);
        Product product = productRepository.findById(bom.getProductId()).orElse(null);
        UnitOfMeasure productUnit = product == null ? null : unitOfMeasureRepository.findById(product.getUnitOfMeasureId()).orElse(null);

        Map<Long, Product> components = productRepository.findAllById(
                lines.stream().map(BillOfMaterialLine::getComponentProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, UnitOfMeasure> units = unitOfMeasureRepository.findAllById(
                components.values().stream().map(Product::getUnitOfMeasureId).distinct().toList()
        ).stream().collect(Collectors.toMap(UnitOfMeasure::getId, u -> u));

        List<BillOfMaterialLineResponse> lineResponses = lines.stream()
                .map(l -> {
                    Product componentProduct = components.get(l.getComponentProductId());
                    UnitOfMeasure unit = componentProduct == null ? null : units.get(componentProduct.getUnitOfMeasureId());
                    return BillOfMaterialLineResponse.builder()
                            .id(l.getId())
                            .componentProductId(l.getComponentProductId())
                            .componentProductName(componentProduct == null ? null : componentProduct.getName())
                            .componentProductSku(componentProduct == null ? null : componentProduct.getSku())
                            .unitOfMeasureId(componentProduct == null ? null : componentProduct.getUnitOfMeasureId())
                            .unitOfMeasureAbbreviation(unit == null ? null : unit.getAbbreviation())
                            .quantity(l.getQuantity())
                            .scrapPercent(l.getScrapPercent())
                            .build();
                })
                .toList();

        return BillOfMaterialResponse.builder()
                .id(bom.getId())
                .companyId(bom.getCompanyId())
                .companyName(company == null ? null : company.getName())
                .productId(bom.getProductId())
                .productName(product == null ? null : product.getName())
                .productSku(product == null ? null : product.getSku())
                .bomNumber(bom.getBomNumber())
                .name(bom.getName())
                .outputQuantity(bom.getOutputQuantity())
                .unitOfMeasureId(product == null ? null : product.getUnitOfMeasureId())
                .unitOfMeasureAbbreviation(productUnit == null ? null : productUnit.getAbbreviation())
                .status(bom.getStatus().name())
                .version(bom.getVersion())
                .previousVersionId(bom.getPreviousVersionId())
                .supersededByBomId(bom.getSupersededByBomId())
                .notes(bom.getNotes())
                .createdBy(bom.getCreatedBy())
                .createdAt(bom.getCreatedAt())
                .lines(lineResponses)
                .build();
    }

    private BillOfMaterial find(Long id) {
        return bomRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Bill of materials not found with id: " + id));
    }

    private Company requireCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private Product requireProduct(Long productId, Long companyId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Product not found with id: " + productId));
        if (!product.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Product does not belong to the selected company");
        }
        return product;
    }
}
