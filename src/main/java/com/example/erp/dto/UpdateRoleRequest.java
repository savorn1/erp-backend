package com.example.erp.dto;

import com.example.erp.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRoleRequest {

    @NotNull
    private Role role;
}
