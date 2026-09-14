package com.example.erp.service.impl;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.WarehouseBinFilterRequest;
import com.example.erp.dto.WarehouseBinRequest;
import com.example.erp.dto.WarehouseBinResponse;
import com.example.erp.entity.Warehouse;
import com.example.erp.entity.WarehouseBin;
import com.example.erp.entity.WarehouseZone;
import com.example.erp.exception.AppException;
import com.example.erp.repository.WarehouseBinRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.repository.WarehouseZoneRepository;
import com.example.erp.service.WarehouseBinService;
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
public class WarehouseBinServiceImpl implements WarehouseBinService {

    private final WarehouseBinRepository binRepository;
    private final WarehouseZoneRepository zoneRepository;
    private final WarehouseRepository warehouseRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WarehouseBinResponse> list(WarehouseBinFilterRequest filter) {
        List<Specification<WarehouseBin>> conditions = new ArrayList<>();
        if (filter.getName() != null && !filter.getName().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        }
        if (filter.getZoneId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("zoneId"), filter.getZoneId()));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        Specification<WarehouseBin> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<WarehouseBin> page = binRepository.findAll(spec, pageable);
        List<WarehouseBin> content = page.getContent();

        Map<Long, WarehouseZone> zones = zoneRepository.findAllById(
                content.stream().map(WarehouseBin::getZoneId).distinct().toList()
        ).stream().collect(Collectors.toMap(WarehouseZone::getId, z -> z));
        Map<Long, String> warehouseNames = warehouseRepository.findAllById(
                zones.values().stream().map(WarehouseZone::getWarehouseId).distinct().toList()
        ).stream().collect(Collectors.toMap(Warehouse::getId, Warehouse::getName));

        return PageResponse.of(page.map(b -> {
            WarehouseZone zone = zones.get(b.getZoneId());
            return toResponse(b, zone, zone == null ? null : warehouseNames.get(zone.getWarehouseId()));
        }));
    }

    @Override
    public WarehouseBinResponse get(Long id) {
        WarehouseBin bin = find(id);
        WarehouseZone zone = zoneRepository.findById(bin.getZoneId()).orElse(null);
        String warehouseName = zone == null ? null : warehouseNameOf(zone.getWarehouseId());
        return toResponse(bin, zone, warehouseName);
    }

    @Override
    @Transactional
    public WarehouseBinResponse create(WarehouseBinRequest request) {
        WarehouseZone zone = requireZone(request.getZoneId());
        if (binRepository.existsByZoneIdAndName(request.getZoneId(), request.getName())) {
            throw new AppException(HttpStatus.CONFLICT, "Bin name already taken in this zone: " + request.getName());
        }
        WarehouseBin bin = WarehouseBin.builder()
                .zoneId(request.getZoneId())
                .name(request.getName())
                .active(request.isActive())
                .build();
        binRepository.save(bin);
        return toResponse(bin, zone, warehouseNameOf(zone.getWarehouseId()));
    }

    @Override
    @Transactional
    public WarehouseBinResponse update(Long id, WarehouseBinRequest request) {
        WarehouseBin bin = find(id);
        WarehouseZone zone = requireZone(request.getZoneId());
        if (binRepository.existsByZoneIdAndNameAndIdNot(request.getZoneId(), request.getName(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "Bin name already taken in this zone: " + request.getName());
        }
        bin.setZoneId(request.getZoneId());
        bin.setName(request.getName());
        bin.setActive(request.isActive());
        binRepository.save(bin);
        return toResponse(bin, zone, warehouseNameOf(zone.getWarehouseId()));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        binRepository.delete(find(id));
    }

    private WarehouseZone requireZone(Long zoneId) {
        return zoneRepository.findById(zoneId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Zone not found with id: " + zoneId));
    }

    private String warehouseNameOf(Long warehouseId) {
        return warehouseRepository.findById(warehouseId).map(Warehouse::getName).orElse(null);
    }

    private WarehouseBin find(Long id) {
        return binRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Bin not found with id: " + id));
    }

    private WarehouseBinResponse toResponse(WarehouseBin bin, WarehouseZone zone, String warehouseName) {
        return WarehouseBinResponse.builder()
                .id(bin.getId())
                .zoneId(bin.getZoneId())
                .zoneName(zone == null ? null : zone.getName())
                .warehouseId(zone == null ? null : zone.getWarehouseId())
                .warehouseName(warehouseName)
                .name(bin.getName())
                .active(bin.isActive())
                .build();
    }
}
