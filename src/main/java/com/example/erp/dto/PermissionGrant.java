package com.example.erp.dto;

import com.example.erp.entity.PermissionAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionGrant {

    @NotBlank
    private String module;

    @NotNull
    private PermissionAction action;
}
