package com.example.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDepartmentRequest {

    @NotBlank
    private String name;

    private Long parentDepartmentId;

    private Long managerId;
}
