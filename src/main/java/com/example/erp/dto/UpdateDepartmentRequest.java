package com.example.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateDepartmentRequest {

    @NotBlank
    private String name;

    private Long parentDepartmentId;

    private Long managerId;
}
