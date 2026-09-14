package com.example.erp.service.impl;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.WarehouseZoneFilterRequest;
import com.example.erp.dto.WarehouseZoneRequest;
import com.example.erp.dto.WarehouseZoneResponse;
import com.example.erp.entity.Warehouse;
import com.example.erp.entity.WarehouseZone;
import com.example.erp.exception.AppException;
import com.example.erp.repository.WarehouseBinRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.repository.WarehouseZoneRepository;
import com.example.erp.service.WarehouseZoneService;
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
public class WarehouseZoneServiceImpl implements WarehouseZoneService {

    private final WarehouseZoneRepository zoneRepository;
    private final WarehouseBinRepository binRepository;
    private final WarehouseRepository warehouseRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WarehouseZoneResponse> list(WarehouseZoneFilterRequest filter) {
        List<Specification<WarehouseZone>> conditions = new ArrayList<>();
        if (filter.getName() != null && !filter.getName().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        }
        if (filter.getWarehouseId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("warehouseId"), filter.getWarehouseId()));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        Specification<WarehouseZone> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<WarehouseZone> page = zoneRepository.findAll(spec, pageable);
        List<WarehouseZone> content = page.getContent();

        Map<Long, String> warehouseNames = warehouseRepository.findAllById(
                content.stream().map(WarehouseZone::getWarehouseId).distinct().toList()
        ).stream().collect(Collectors.toMap(Warehouse::getId, Warehouse::getName));
        Map<Long, Long> binCounts = content.stream()
                .collect(Collectors.toMap(WarehouseZone::getId, z -> binRepository.countByZoneId(z.getId())));

        return PageResponse.of(page.map(z -> toResponse(z, warehouseNames.get(z.getWarehouseId()), binCounts.getOrDefault(z.getId(), 0L))));
    }

    @Override
    public WarehouseZoneResponse get(Long id) {
        WarehouseZone zone = find(id);
        return toResponse(zone, warehouseNameOf(zone.getWarehouseId()), binRepository.countByZoneId(id));
    }

    @Override
    @Transactional
    public WarehouseZoneResponse create(WarehouseZoneRequest request) {
        Warehouse warehouse = requireWarehouse(request.getWarehouseId());
        if (zoneRepository.existsByWarehouseIdAndName(request.getWarehouseId(), request.getName())) {
            throw new AppException(HttpStatus.CONFLICT, "Zone name already taken in this warehouse: " + request.getName());
        }
        WarehouseZone zone = WarehouseZone.builder()
                .warehouseId(request.getWarehouseId())
                .name(request.getName())
                .description(request.getDescription())
                .active(request.isActive())
                .build();
        zoneRepository.save(zone);
        return toResponse(zone, warehouse.getName(), 0);
    }

    @Override
    @Transactional
    public WarehouseZoneResponse update(Long id, WarehouseZoneRequest request) {
        WarehouseZone zone = find(id);
        Warehouse warehouse = requireWarehouse(request.getWarehouseId());
        if (zoneRepository.existsByWarehouseIdAndNameAndIdNot(request.getWarehouseId(), request.getName(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "Zone name already taken in this warehouse: " + request.getName());
        }
        zone.setWarehouseId(request.getWarehouseId());
        zone.setName(request.getName());
        zone.setDescription(request.getDescription());
        zone.setActive(request.isActive());
        zoneRepository.save(zone);
        return toResponse(zone, warehouse.getName(), binRepository.countByZoneId(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        find(id);
        if (binRepository.existsByZoneId(id)) {
            throw new AppException(HttpStatus.CONFLICT, "Cannot delete a zone that has shelves/bins — delete them first");
        }
        zoneRepository.deleteById(id);
    }

    private Warehouse requireWarehouse(Long warehouseId) {
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Warehouse not found with id: " + warehouseId));
    }

    private String warehouseNameOf(Long warehouseId) {
        return warehouseRepository.findById(warehouseId).map(Warehouse::getName).orElse(null);
    }

    private WarehouseZone find(Long id) {
        return zoneRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Zone not found with id: " + id));
    }

    private WarehouseZoneResponse toResponse(WarehouseZone zone, String warehouseName, long binCount) {
        return WarehouseZoneResponse.builder()
                .id(zone.getId())
                .warehouseId(zone.getWarehouseId())
                .warehouseName(warehouseName)
                .name(zone.getName())
                .description(zone.getDescription())
                .active(zone.isActive())
                .binCount(binCount)
                .build();
    }
}
