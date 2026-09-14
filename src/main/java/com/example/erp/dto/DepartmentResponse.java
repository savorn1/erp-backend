package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {

    private Long id;
    private String name;
    private Long parentDepartmentId;
    private String parentDepartmentName;
    private Long managerId;
    private String managerUsername;
    private long employeeCount;
    private boolean active;
}
