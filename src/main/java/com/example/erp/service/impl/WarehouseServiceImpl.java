package com.example.erp.service.impl;

import com.example.erp.dto.CreateWarehouseRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateWarehouseRequest;
import com.example.erp.dto.UpdateWarehouseStatusRequest;
import com.example.erp.dto.WarehouseFilterRequest;
import com.example.erp.dto.WarehouseResponse;
import com.example.erp.entity.Company;
import com.example.erp.entity.User;
import com.example.erp.entity.Warehouse;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.UserRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.repository.WarehouseZoneRepository;
import com.example.erp.service.WarehouseService;
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
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseZoneRepository zoneRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WarehouseResponse> listWarehouses(WarehouseFilterRequest filter) {
        List<Specification<Warehouse>> conditions = new ArrayList<>();
        if (filter.getName() != null && !filter.getName().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        Specification<Warehouse> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<Warehouse> page = warehouseRepository.findAll(spec, pageable);
        List<Warehouse> content = page.getContent();

        Map<Long, String> companyNames = companyRepository.findAllById(
                content.stream().map(Warehouse::getCompanyId).distinct().toList()
        ).stream().collect(Collectors.toMap(Company::getId, Company::getName));
        Map<Long, String> managerUsernames = userRepository.findAllById(
                content.stream().map(Warehouse::getManagerId).filter(java.util.Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(User::getId, User::getUsername));
        Map<Long, Long> zoneCounts = content.stream()
                .collect(Collectors.toMap(Warehouse::getId, w -> zoneRepository.countByWarehouseId(w.getId())));

        return PageResponse.of(page.map(w -> toResponse(w,
                companyNames.get(w.getCompanyId()),
                w.getManagerId() == null ? null : managerUsernames.get(w.getManagerId()),
                zoneCounts.getOrDefault(w.getId(), 0L))));
    }

    @Override
    public WarehouseResponse getWarehouse(Long id) {
        Warehouse warehouse = find(id);
        return toResponse(warehouse,
                companyNameOf(warehouse.getCompanyId()),
                usernameOf(warehouse.getManagerId()),
                zoneRepository.countByWarehouseId(id));
    }

    @Override
    @Transactional
    public WarehouseResponse createWarehouse(CreateWarehouseRequest request) {
        Company company = requireCompany(request.getCompanyId());
        User manager = request.getManagerId() == null ? null : requireManager(request.getManagerId());
        if (warehouseRepository.existsByCompanyIdAndName(request.getCompanyId(), request.getName())) {
            throw new AppException(HttpStatus.CONFLICT, "Warehouse name already taken in this company: " + request.getName());
        }

        Warehouse warehouse = Warehouse.builder()
                .companyId(request.getCompanyId())
                .name(request.getName())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .managerId(request.getManagerId())
                .phone(request.getPhone())
                .email(request.getEmail())
                .timezone(request.getTimezone())
                .build();
        warehouseRepository.save(warehouse);
        return toResponse(warehouse, company.getName(), manager == null ? null : manager.getUsername(), 0);
    }

    @Override
    @Transactional
    public WarehouseResponse updateWarehouse(Long id, UpdateWarehouseRequest request) {
        Warehouse warehouse = find(id);
        Company company = requireCompany(request.getCompanyId());
        User manager = request.getManagerId() == null ? null : requireManager(request.getManagerId());
        if (warehouseRepository.existsByCompanyIdAndNameAndIdNot(request.getCompanyId(), request.getName(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "Warehouse name already taken in this company: " + request.getName());
        }

        warehouse.setCompanyId(request.getCompanyId());
        warehouse.setName(request.getName());
        warehouse.setAddressLine1(request.getAddressLine1());
        warehouse.setAddressLine2(request.getAddressLine2());
        warehouse.setCity(request.getCity());
        warehouse.setState(request.getState());
        warehouse.setPostalCode(request.getPostalCode());
        warehouse.setCountry(request.getCountry());
        warehouse.setManagerId(request.getManagerId());
        warehouse.setPhone(request.getPhone());
        warehouse.setEmail(request.getEmail());
        warehouse.setTimezone(request.getTimezone());
        warehouseRepository.save(warehouse);
        return toResponse(warehouse, company.getName(), manager == null ? null : manager.getUsername(),
                zoneRepository.countByWarehouseId(id));
    }

    @Override
    @Transactional
    public WarehouseResponse updateStatus(Long id, UpdateWarehouseStatusRequest request) {
        Warehouse warehouse = find(id);
        warehouse.setActive(request.getActive());
        warehouseRepository.save(warehouse);
        return toResponse(warehouse, companyNameOf(warehouse.getCompanyId()), usernameOf(warehouse.getManagerId()),
                zoneRepository.countByWarehouseId(id));
    }

    @Override
    @Transactional
    public void deleteWarehouse(Long id) {
        find(id);
        if (zoneRepository.existsByWarehouseId(id)) {
            throw new AppException(HttpStatus.CONFLICT, "Cannot delete a warehouse that has zones — delete them first");
        }
        warehouseRepository.deleteById(id);
    }

    private Company requireCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private User requireManager(Long managerId) {
        return userRepository.findById(managerId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Manager not found with id: " + managerId));
    }

    private String companyNameOf(Long companyId) {
        return companyRepository.findById(companyId).map(Company::getName).orElse(null);
    }

    private String usernameOf(Long userId) {
        return userId == null ? null : userRepository.findById(userId).map(User::getUsername).orElse(null);
    }

    private Warehouse find(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Warehouse not found with id: " + id));
    }

    private WarehouseResponse toResponse(Warehouse warehouse, String companyName, String managerUsername, long zoneCount) {
        return WarehouseResponse.builder()
                .id(warehouse.getId())
                .companyId(warehouse.getCompanyId())
                .companyName(companyName)
                .name(warehouse.getName())
                .addressLine1(warehouse.getAddressLine1())
                .addressLine2(warehouse.getAddressLine2())
                .city(warehouse.getCity())
                .state(warehouse.getState())
                .postalCode(warehouse.getPostalCode())
                .country(warehouse.getCountry())
                .managerId(warehouse.getManagerId())
                .managerUsername(managerUsername)
                .phone(warehouse.getPhone())
                .email(warehouse.getEmail())
                .timezone(warehouse.getTimezone())
                .active(warehouse.isActive())
                .zoneCount(zoneCount)
                .build();
    }
}
