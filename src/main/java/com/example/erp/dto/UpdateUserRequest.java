package com.example.erp.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

// Admin-side general edit — distinct from UpdateRoleRequest/UpdateStatusRequest
// (which stay separate since role/status changes revoke sessions) and from
// UpdateProfileRequest (self-service, current user only).
@Data
public class UpdateUserRequest {

    @Email
    private String email;

    private Long companyId;
    private Long branchId;
    private Long departmentId;
}
