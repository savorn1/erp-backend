package com.example.erp.dto;

import com.example.erp.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Email
    private String email;

    @NotNull
    private Role role = Role.USER;

    private boolean enabled = true;

    // Optional — org assignment can also be set later via UpdateUserRequest.
    private Long companyId;
    private Long branchId;
    private Long departmentId;

    // Only meaningful when role == USER — see PermissionAuthorizationManager.
    // Can also be set/changed later via PUT /{id}/custom-role.
    private Long customRoleId;
}
