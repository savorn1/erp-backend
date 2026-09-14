package com.example.erp.dto;

import com.example.erp.entity.MachineStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMachineStatusRequest {

    @NotNull
    private MachineStatus status;
}
