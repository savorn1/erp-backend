package com.example.erp.service;

import com.example.erp.dto.ChangePasswordRequest;
import com.example.erp.dto.CreateUserRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ResetPasswordRequest;
import com.example.erp.dto.UpdateProfileRequest;
import com.example.erp.dto.UpdateCustomRoleRequest;
import com.example.erp.dto.UpdateRoleRequest;
import com.example.erp.dto.UpdateStatusRequest;
import com.example.erp.dto.UpdateUserRequest;
import com.example.erp.dto.UserFilterRequest;
import com.example.erp.dto.UserResponse;

import java.util.List;

public interface UserService {

    PageResponse<UserResponse> listUsers(UserFilterRequest filter);

    UserResponse getUser(Long id);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    UserResponse updateRole(Long id, UpdateRoleRequest request, String actingUsername);

    UserResponse updateCustomRole(Long id, UpdateCustomRoleRequest request, String actingUsername);

    UserResponse updateStatus(Long id, UpdateStatusRequest request, String actingUsername);

    void resetPassword(Long id, ResetPasswordRequest request, String actingUsername);

    void deleteUser(Long id, String actingUsername);

    // Revokes the user's refresh tokens without touching role/status/password —
    // the current access token stays valid until it naturally expires, but the
    // next refresh attempt fails and forces a re-login.
    void forceLogout(Long id);

    void bulkForceLogout(List<Long> ids);

    UserResponse updateProfile(Long id, UpdateProfileRequest request);

    void changePassword(Long id, ChangePasswordRequest request);
}
