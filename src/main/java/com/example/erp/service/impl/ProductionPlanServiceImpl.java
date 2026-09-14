package com.example.erp.service.impl;

import com.example.erp.dto.CreateProductionPlanRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ProductionPlanFilterRequest;
import com.example.erp.dto.ProductionPlanResponse;
import com.example.erp.dto.UpdateProductionPlanRequest;
import com.example.erp.entity.Company;
import com.example.erp.entity.ManufacturingOrder;
import com.example.erp.entity.ProductionPlan;
import com.example.erp.entity.ProductionPlanStatus;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.ManufacturingOrderRepository;
import com.example.erp.repository.ProductionPlanRepository;
import com.example.erp.service.ProductionPlanService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductionPlanServiceImpl implements ProductionPlanService {

    private final ProductionPlanRepository planRepository;
    private final CompanyRepository companyRepository;
    private final ManufacturingOrderRepository manufacturingOrderRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductionPlanResponse> listPlans(ProductionPlanFilterRequest filter) {
        List<Specification<ProductionPlan>> conditions = new ArrayList<>();
        if (filter.getPlanNumber() != null && !filter.getPlanNumber().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("planNumber")), "%" + filter.getPlanNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        Specification<ProductionPlan> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<ProductionPlan> page = planRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionPlanResponse getPlan(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public ProductionPlanResponse createPlan(CreateProductionPlanRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());

        ProductionPlan plan = ProductionPlan.builder()
                .companyId(request.getCompanyId())
                .name(request.getName())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .notes(request.getNotes())
                .createdBy(actingUsername)
                .build();
        planRepository.save(plan);
        plan.setPlanNumber("PP-" + String.format("%06d", plan.getId()));
        planRepository.save(plan);
        return toResponse(plan);
    }

    @Override
    @Transactional
    public ProductionPlanResponse updatePlan(Long id, UpdateProductionPlanRequest request) {
        ProductionPlan plan = find(id);
        if (plan.getStatus() != ProductionPlanStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft production plans can be edited");
        }
        plan.setName(request.getName());
        plan.setPeriodStart(request.getPeriodStart());
        plan.setPeriodEnd(request.getPeriodEnd());
        plan.setNotes(request.getNotes());
        planRepository.save(plan);
        return toResponse(plan);
    }

    @Override
    @Transactional
    public ProductionPlanResponse activatePlan(Long id) {
        ProductionPlan plan = find(id);
        if (plan.getStatus() != ProductionPlanStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft production plans can be activated");
        }
        plan.setStatus(ProductionPlanStatus.ACTIVE);
        planRepository.save(plan);
        return toResponse(plan);
    }

    @Override
    @Transactional
    public ProductionPlanResponse closePlan(Long id) {
        ProductionPlan plan = find(id);
        if (plan.getStatus() != ProductionPlanStatus.ACTIVE) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only active production plans can be closed");
        }
        plan.setStatus(ProductionPlanStatus.CLOSED);
        planRepository.save(plan);
        return toResponse(plan);
    }

    @Override
    @Transactional
    public void deletePlan(Long id) {
        ProductionPlan plan = find(id);
        if (plan.getStatus() != ProductionPlanStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft production plans can be deleted");
        }
        if (manufacturingOrderRepository.exists((root, query, cb) -> cb.equal(root.get("productionPlanId"), plan.getId()))) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delete a production plan that already has manufacturing orders linked to it");
        }
        planRepository.delete(plan);
    }

    private ProductionPlanResponse toResponse(ProductionPlan plan) {
        Company company = companyRepository.findById(plan.getCompanyId()).orElse(null);
        List<ManufacturingOrder> orders = manufacturingOrderRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("productionPlanId"), plan.getId()));

        BigDecimal totalPlanned = orders.stream().map(ManufacturingOrder::getPlannedQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalProduced = orders.stream().map(ManufacturingOrder::getProducedQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);

        return ProductionPlanResponse.builder()
                .id(plan.getId())
                .companyId(plan.getCompanyId())
                .companyName(company == null ? null : company.getName())
                .planNumber(plan.getPlanNumber())
                .name(plan.getName())
                .periodStart(plan.getPeriodStart())
                .periodEnd(plan.getPeriodEnd())
                .status(plan.getStatus().name())
                .notes(plan.getNotes())
                .createdBy(plan.getCreatedBy())
                .createdAt(plan.getCreatedAt())
                .orderCount(orders.size())
                .totalPlannedQuantity(totalPlanned)
                .totalProducedQuantity(totalProduced)
                .build();
    }

    private ProductionPlan find(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Production plan not found with id: " + id));
    }

    private void requireCompany(Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }
}
