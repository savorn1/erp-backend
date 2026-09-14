package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineUtilizationResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    // Work orders assigned to a specific machine only (machineId not null),
    // completed, sorted by totalActualHours descending.
    private List<MachineUtilizationRowResponse> rows;
}
