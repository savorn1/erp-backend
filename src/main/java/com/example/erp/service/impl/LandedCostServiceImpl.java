package com.example.erp.service.impl;

import com.example.erp.dto.CreateLandedCostRequest;
import com.example.erp.dto.LandedCostAllocationResponse;
import com.example.erp.dto.LandedCostFilterRequest;
import com.example.erp.dto.LandedCostResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.entity.GoodsReceipt;
import com.example.erp.entity.GoodsReceiptLine;
import com.example.erp.entity.GoodsReceiptStatus;
import com.example.erp.entity.LandedCost;
import com.example.erp.entity.LandedCostAllocation;
import com.example.erp.entity.LandedCostAllocationMethod;
import com.example.erp.entity.Product;
import com.example.erp.exception.AppException;
import com.example.erp.repository.GoodsReceiptLineRepository;
import com.example.erp.repository.GoodsReceiptRepository;
import com.example.erp.repository.LandedCostAllocationRepository;
import com.example.erp.repository.LandedCostRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.service.LandedCostService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LandedCostServiceImpl implements LandedCostService {

    private final LandedCostRepository landedCostRepository;
    private final LandedCostAllocationRepository landedCostAllocationRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final GoodsReceiptLineRepository goodsReceiptLineRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LandedCostResponse> listLandedCosts(LandedCostFilterRequest filter) {
        List<Specification<LandedCost>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getGoodsReceiptId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("goodsReceiptId"), filter.getGoodsReceiptId()));
        }
        if (filter.getCostType() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("costType"), filter.getCostType()));
        }
        Specification<LandedCost> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<LandedCost> page = landedCostRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    public LandedCostResponse getLandedCost(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public LandedCostResponse createLandedCost(CreateLandedCostRequest request, String actingUsername) {
        GoodsReceipt receipt = goodsReceiptRepository.findById(request.getGoodsReceiptId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Goods receipt not found with id: " + request.getGoodsReceiptId()));
        if (receipt.getStatus() != GoodsReceiptStatus.COMPLETED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Landed costs can only be allocated once the goods receipt is completed");
        }

        List<GoodsReceiptLine> lines = goodsReceiptLineRepository.findByGoodsReceiptId(receipt.getId()).stream()
                .filter(l -> baseQuantity(l).compareTo(BigDecimal.ZERO) > 0)
                .toList();
        if (lines.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "This goods receipt has no received quantity to allocate cost onto");
        }

        Map<GoodsReceiptLine, BigDecimal> weights = lines.stream().collect(Collectors.toMap(
                l -> l,
                l -> request.getAllocationMethod() == LandedCostAllocationMethod.BY_QUANTITY
                        ? baseQuantity(l)
                        : baseQuantity(l).multiply(l.getUnitCost() == null ? BigDecimal.ZERO : l.getUnitCost())
        ));
        BigDecimal totalWeight = weights.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "This receipt has no cost data to allocate by value — use allocation by quantity instead");
        }

        LandedCost landedCost = LandedCost.builder()
                .companyId(receipt.getCompanyId())
                .goodsReceiptId(receipt.getId())
                .costType(request.getCostType())
                .amount(request.getAmount())
                .allocationMethod(request.getAllocationMethod())
                .costDate(request.getCostDate())
                .reference(request.getReference())
                .notes(request.getNotes())
                .createdBy(actingUsername)
                .build();
        landedCostRepository.save(landedCost);

        Map<Long, Product> products = productRepository.findAllById(
                lines.stream().map(GoodsReceiptLine::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<LandedCostAllocation> allocations = new ArrayList<>();
        BigDecimal allocatedSoFar = BigDecimal.ZERO;
        for (int i = 0; i < lines.size(); i++) {
            GoodsReceiptLine line = lines.get(i);
            BigDecimal quantity = baseQuantity(line);
            BigDecimal allocatedAmount;
            if (i == lines.size() - 1) {
                allocatedAmount = request.getAmount().subtract(allocatedSoFar).setScale(4, RoundingMode.HALF_UP);
            } else {
                BigDecimal ratio = weights.get(line).divide(totalWeight, 8, RoundingMode.HALF_UP);
                allocatedAmount = request.getAmount().multiply(ratio).setScale(4, RoundingMode.HALF_UP);
            }
            allocatedSoFar = allocatedSoFar.add(allocatedAmount);

            BigDecimal originalUnitCost = line.getUnitCost();
            BigDecimal newUnitCost = (originalUnitCost == null ? BigDecimal.ZERO : originalUnitCost)
                    .add(allocatedAmount.divide(quantity, 4, RoundingMode.HALF_UP));

            allocations.add(landedCostAllocationRepository.save(LandedCostAllocation.builder()
                    .landedCostId(landedCost.getId())
                    .goodsReceiptLineId(line.getId())
                    .productId(line.getProductId())
                    .quantity(quantity)
                    .originalUnitCost(originalUnitCost)
                    .allocatedAmount(allocatedAmount)
                    .newUnitCost(newUnitCost)
                    .build()));

            Product product = products.get(line.getProductId());
            if (product != null) {
                product.setCostPrice(newUnitCost);
                productRepository.save(product);
            }
        }

        return toResponse(landedCost, allocations);
    }

    private BigDecimal baseQuantity(GoodsReceiptLine line) {
        BigDecimal conversionFactor = line.getConversionFactor() != null ? line.getConversionFactor() : BigDecimal.ONE;
        return line.getQuantityReceived().multiply(conversionFactor);
    }

    private LandedCost find(Long id) {
        return landedCostRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Landed cost not found with id: " + id));
    }

    private LandedCostResponse toResponse(LandedCost landedCost) {
        return toResponse(landedCost, landedCostAllocationRepository.findByLandedCostId(landedCost.getId()));
    }

    private LandedCostResponse toResponse(LandedCost landedCost, List<LandedCostAllocation> allocations) {
        GoodsReceipt receipt = goodsReceiptRepository.findById(landedCost.getGoodsReceiptId()).orElse(null);
        Map<Long, Product> products = allocations.isEmpty() ? Map.of() : productRepository.findAllById(
                allocations.stream().map(LandedCostAllocation::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<LandedCostAllocationResponse> allocationResponses = allocations.stream().map(a -> {
            Product product = products.get(a.getProductId());
            return LandedCostAllocationResponse.builder()
                    .productId(a.getProductId())
                    .productName(product == null ? null : product.getName())
                    .productSku(product == null ? null : product.getSku())
                    .goodsReceiptLineId(a.getGoodsReceiptLineId())
                    .quantity(a.getQuantity())
                    .originalUnitCost(a.getOriginalUnitCost())
                    .allocatedAmount(a.getAllocatedAmount())
                    .newUnitCost(a.getNewUnitCost())
                    .build();
        }).toList();

        return LandedCostResponse.builder()
                .id(landedCost.getId())
                .companyId(landedCost.getCompanyId())
                .goodsReceiptId(landedCost.getGoodsReceiptId())
                .goodsReceiptNumber(receipt == null ? null : receipt.getReceiptNumber())
                .costType(landedCost.getCostType())
                .amount(landedCost.getAmount())
                .allocationMethod(landedCost.getAllocationMethod())
                .costDate(landedCost.getCostDate())
                .reference(landedCost.getReference())
                .notes(landedCost.getNotes())
                .createdBy(landedCost.getCreatedBy())
                .allocations(allocationResponses)
                .build();
    }
}
