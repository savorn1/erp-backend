package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long workCenterId;
    private String workCenterName;
    private String name;
    private String code;
    private String status;
    private BigDecimal costPerHour;
}
