package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreateMachineRequest;
import com.example.erp.dto.MachineFilterRequest;
import com.example.erp.dto.MachineResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateMachineRequest;
import com.example.erp.dto.UpdateMachineStatusRequest;
import com.example.erp.service.MachineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/machines")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class MachineController {

    private final MachineService machineService;

    @GetMapping
    public ResponseEntity<PageResponse<MachineResponse>> list(@ModelAttribute MachineFilterRequest filter) {
        return ResponseEntity.ok(machineService.listMachines(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MachineResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(machineService.getMachine(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MachineResponse>> create(@Valid @RequestBody CreateMachineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Machine created", machineService.createMachine(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MachineResponse>> update(@PathVariable Long id, @Valid @RequestBody UpdateMachineRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Machine updated", machineService.updateMachine(id, request)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<MachineResponse>> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateMachineStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", machineService.updateStatus(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        machineService.deleteMachine(id);
        return ResponseEntity.ok(ApiResponse.success("Machine deleted", null));
    }
}
