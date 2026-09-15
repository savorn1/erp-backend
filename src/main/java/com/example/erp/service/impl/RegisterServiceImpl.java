package com.example.erp.service.impl;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.RegisterFilterRequest;
import com.example.erp.dto.RegisterRequest;
import com.example.erp.dto.RegisterResponse;
import com.example.erp.entity.Company;
import com.example.erp.entity.PosSession;
import com.example.erp.entity.PosSessionStatus;
import com.example.erp.entity.Register;
import com.example.erp.entity.Warehouse;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.PosSaleRepository;
import com.example.erp.repository.PosSessionRepository;
import com.example.erp.repository.RegisterRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.service.RegisterService;
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

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private final RegisterRepository registerRepository;
    private final CompanyRepository companyRepository;
    private final WarehouseRepository warehouseRepository;
    private final PosSessionRepository posSessionRepository;
    private final PosSaleRepository posSaleRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RegisterResponse> list(RegisterFilterRequest filter) {
        List<Specification<Register>> conditions = new ArrayList<>();
        if (filter.getCode() != null && !filter.getCode().isBlank()) {
            conditions.add((root, query, cb) -> cb.like(cb.lower(root.get("code")), "%" + filter.getCode().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getActive() != null) conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        Specification<Register> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<Register> page = registerRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public RegisterResponse get(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public RegisterResponse create(RegisterRequest request) {
        requireCompany(request.getCompanyId());
        requireWarehouse(request.getWarehouseId(), request.getCompanyId());
        if (registerRepository.existsByCompanyIdAndCode(request.getCompanyId(), request.getCode())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "A register with code '" + request.getCode() + "' already exists for this company");
        }
        Register register = Register.builder()
                .companyId(request.getCompanyId())
                .warehouseId(request.getWarehouseId())
                .code(request.getCode())
                .name(request.getName())
                .active(request.isActive())
                .build();
        return toResponse(registerRepository.save(register));
    }

    @Override
    @Transactional
    public RegisterResponse update(Long id, RegisterRequest request) {
        Register register = find(id);
        requireWarehouse(request.getWarehouseId(), request.getCompanyId());
        if (registerRepository.existsByCompanyIdAndCodeAndIdNot(request.getCompanyId(), request.getCode(), id)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "A register with code '" + request.getCode() + "' already exists for this company");
        }
        register.setWarehouseId(request.getWarehouseId());
        register.setCode(request.getCode());
        register.setName(request.getName());
        register.setActive(request.isActive());
        return toResponse(registerRepository.save(register));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Register register = find(id);
        if (!posSaleRepository.findAll(saleByRegisterSpec(id)).isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delete a register that already has POS sales recorded against it");
        }
        registerRepository.delete(register);
    }

    private Specification<com.example.erp.entity.PosSale> saleByRegisterSpec(Long registerId) {
        return (root, query, cb) -> cb.equal(root.get("registerId"), registerId);
    }

    private RegisterResponse toResponse(Register register) {
        Company company = companyRepository.findById(register.getCompanyId()).orElse(null);
        Warehouse warehouse = warehouseRepository.findById(register.getWarehouseId()).orElse(null);
        PosSession openSession = posSessionRepository.findByRegisterIdAndStatus(register.getId(), PosSessionStatus.OPEN).orElse(null);
        return RegisterResponse.builder()
                .id(register.getId())
                .companyId(register.getCompanyId())
                .companyName(company == null ? null : company.getName())
                .warehouseId(register.getWarehouseId())
                .warehouseName(warehouse == null ? null : warehouse.getName())
                .code(register.getCode())
                .name(register.getName())
                .active(register.isActive())
                .hasOpenSession(openSession != null)
                .openPosSessionId(openSession == null ? null : openSession.getId())
                .build();
    }

    private Register find(Long id) {
        return registerRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Register not found with id: " + id));
    }

    private void requireCompany(Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private void requireWarehouse(Long warehouseId, Long companyId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Warehouse not found with id: " + warehouseId));
        if (!warehouse.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Warehouse does not belong to the selected company");
        }
    }
}
