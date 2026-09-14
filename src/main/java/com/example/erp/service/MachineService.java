package com.example.erp.service;

import com.example.erp.dto.CreateMachineRequest;
import com.example.erp.dto.MachineFilterRequest;
import com.example.erp.dto.MachineResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateMachineRequest;
import com.example.erp.dto.UpdateMachineStatusRequest;

public interface MachineService {

    PageResponse<MachineResponse> listMachines(MachineFilterRequest filter);

    MachineResponse getMachine(Long id);

    MachineResponse createMachine(CreateMachineRequest request);

    MachineResponse updateMachine(Long id, UpdateMachineRequest request);

    MachineResponse updateStatus(Long id, UpdateMachineStatusRequest request);

    void deleteMachine(Long id);
}
