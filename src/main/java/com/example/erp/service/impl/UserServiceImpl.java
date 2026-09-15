package com.example.erp.service.impl;

import com.example.erp.dto.ChangePasswordRequest;
import com.example.erp.dto.CreateUserRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ResetPasswordRequest;
import com.example.erp.dto.UpdateCustomRoleRequest;
import com.example.erp.dto.UpdateProfileRequest;
import com.example.erp.dto.UpdateRoleRequest;
import com.example.erp.dto.UpdateStatusRequest;
import com.example.erp.dto.UpdateUserRequest;
import com.example.erp.dto.UserFilterRequest;
import com.example.erp.dto.UserResponse;
import com.example.erp.entity.Branch;
import com.example.erp.entity.Company;
import com.example.erp.entity.Department;
import com.example.erp.entity.CustomRole;
import com.example.erp.entity.User;
import com.example.erp.exception.AppException;
import com.example.erp.repository.BranchRepository;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.CustomRoleRepository;
import com.example.erp.repository.DepartmentRepository;
import com.example.erp.repository.RefreshTokenRepository;
import com.example.erp.repository.UserRepository;
import com.example.erp.service.UserService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final DepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;
    private final BranchRepository branchRepository;
    private final CustomRoleRepository customRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(UserFilterRequest filter) {
        List<Specification<User>> conditions = new ArrayList<>();

        if (filter.getUsername() != null && !filter.getUsername().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("username")), "%" + filter.getUsername().toLowerCase() + "%"));
        }
        if (filter.getRole() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("role"), filter.getRole()));
        }
        if (filter.getEnabled() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("enabled"), filter.getEnabled()));
        }
        if (filter.getDepartmentId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("departmentId"), filter.getDepartmentId()));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getBranchId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("branchId"), filter.getBranchId()));
        }
        Specification<User> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<User> users = userRepository.findAll(spec, pageable);
        List<User> content = users.getContent();

        // Batch-resolved rather than looked up per row — see Branch/BranchServiceImpl
        // for the same reasoning (N+1 avoidance on a plain FK column).
        Map<Long, String> departmentNames = departmentRepository.findAllById(
                content.stream().map(User::getDepartmentId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(Department::getId, Department::getName));
        Map<Long, String> companyNames = companyRepository.findAllById(
                content.stream().map(User::getCompanyId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(Company::getId, Company::getName));
        Map<Long, String> branchNames = branchRepository.findAllById(
                content.stream().map(User::getBranchId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(Branch::getId, Branch::getName));
        Map<Long, String> customRoleNames = customRoleRepository.findAllById(
                content.stream().map(User::getCustomRoleId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(CustomRole::getId, CustomRole::getName));

        return PageResponse.of(users.map(u -> toResponse(u,
                u.getCompanyId() == null ? null : companyNames.get(u.getCompanyId()),
                u.getBranchId() == null ? null : branchNames.get(u.getBranchId()),
                u.getDepartmentId() == null ? null : departmentNames.get(u.getDepartmentId()),
                u.getCustomRoleId() == null ? null : customRoleNames.get(u.getCustomRoleId()))));
    }

    @Override
    public UserResponse getUser(Long id) {
        return toResponse(findUser(id));
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(HttpStatus.CONFLICT, "Username already taken: " + request.getUsername());
        }
        String email = request.getEmail();
        if (email != null && !email.isBlank() && userRepository.existsByEmail(email)) {
            throw new AppException(HttpStatus.CONFLICT, "Email already in use: " + email);
        }
        validateAssignments(request.getCompanyId(), request.getBranchId(), request.getDepartmentId());
        if (request.getCustomRoleId() != null) {
            requireCustomRole(request.getCustomRoleId());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(email == null || email.isBlank() ? null : email)
                .role(request.getRole())
                .enabled(request.isEnabled())
                .companyId(request.getCompanyId())
                .branchId(request.getBranchId())
                .departmentId(request.getDepartmentId())
                .customRoleId(request.getCustomRoleId())
                .build();
        userRepository.save(user);
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findUser(id);
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            user.setEmail(null);
        } else {
            if (userRepository.existsByEmailAndIdNot(email, id)) {
                throw new AppException(HttpStatus.CONFLICT, "Email already in use: " + email);
            }
            user.setEmail(email);
        }
        validateAssignments(request.getCompanyId(), request.getBranchId(), request.getDepartmentId());

        user.setCompanyId(request.getCompanyId());
        user.setBranchId(request.getBranchId());
        user.setDepartmentId(request.getDepartmentId());
        userRepository.save(user);
        return toResponse(user);
    }

    private void validateAssignments(Long companyId, Long branchId, Long departmentId) {
        if (companyId != null) {
            requireCompany(companyId);
        }
        if (branchId != null) {
            requireBranch(branchId);
        }
        if (departmentId != null) {
            requireDepartment(departmentId);
        }
    }

    private Company requireCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private Branch requireBranch(Long branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Branch not found with id: " + branchId));
    }

    private Department requireDepartment(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Department not found with id: " + departmentId));
    }

    private CustomRole requireCustomRole(Long customRoleId) {
        return customRoleRepository.findById(customRoleId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Custom role not found with id: " + customRoleId));
    }

    @Override
    @Transactional
    public UserResponse updateCustomRole(Long id, UpdateCustomRoleRequest request, String actingUsername) {
        User user = findUser(id);
        // Same self-escalation guard as updateRole — a USER account with
        // permission to manage other users must not be able to grant itself
        // a more-privileged custom role.
        if (user.getUsername().equals(actingUsername) && !java.util.Objects.equals(user.getCustomRoleId(), request.getCustomRoleId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "You cannot change your own custom role");
        }
        if (request.getCustomRoleId() != null) {
            requireCustomRole(request.getCustomRoleId());
        }
        user.setCustomRoleId(request.getCustomRoleId());
        userRepository.save(user);
        refreshTokenRepository.revokeAllForUser(user.getId());
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateRole(Long id, UpdateRoleRequest request, String actingUsername) {
        User user = findUser(id);
        if (user.getUsername().equals(actingUsername) && user.getRole() != request.getRole()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "You cannot change your own role");
        }
        user.setRole(request.getRole());
        userRepository.save(user);
        refreshTokenRepository.revokeAllForUser(user.getId());
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateStatus(Long id, UpdateStatusRequest request, String actingUsername) {
        User user = findUser(id);
        if (user.getUsername().equals(actingUsername) && !request.getEnabled()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "You cannot deactivate your own account");
        }
        user.setEnabled(request.getEnabled());
        userRepository.save(user);
        if (!user.isEnabled()) {
            refreshTokenRepository.revokeAllForUser(user.getId());
        }
        return toResponse(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long id, ResetPasswordRequest request, String actingUsername) {
        User user = findUser(id);
        if (user.getUsername().equals(actingUsername)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Use change password to update your own password");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenRepository.revokeAllForUser(user.getId());
    }

    @Override
    @Transactional
    public void deleteUser(Long id, String actingUsername) {
        User user = findUser(id);
        if (user.getUsername().equals(actingUsername)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "You cannot delete your own account");
        }
        refreshTokenRepository.revokeAllForUser(user.getId());
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void forceLogout(Long id) {
        findUser(id);
        refreshTokenRepository.revokeAllForUser(id);
    }

    @Override
    @Transactional
    public void bulkForceLogout(List<Long> ids) {
        List<User> users = userRepository.findAllById(ids);
        if (users.size() != new HashSet<>(ids).size()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "One or more users were not found");
        }
        refreshTokenRepository.revokeAllForUsers(ids);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long id, UpdateProfileRequest request) {
        User user = findUser(id);
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            user.setEmail(null);
        } else {
            if (userRepository.existsByEmailAndIdNot(email, id)) {
                throw new AppException(HttpStatus.CONFLICT, "Email already in use: " + email);
            }
            user.setEmail(email);
        }
        userRepository.save(user);
        return toResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        User user = findUser(id);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenRepository.revokeAllForUser(user.getId());
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found with id: " + id));
    }

    private UserResponse toResponse(User user) {
        return toResponse(user, companyNameOf(user.getCompanyId()), branchNameOf(user.getBranchId()), departmentNameOf(user.getDepartmentId()),
                customRoleNameOf(user.getCustomRoleId()));
    }

    private UserResponse toResponse(User user, String companyName, String branchName, String departmentName, String customRoleName) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .enabled(user.isEnabled())
                .companyId(user.getCompanyId())
                .companyName(companyName)
                .branchId(user.getBranchId())
                .branchName(branchName)
                .departmentId(user.getDepartmentId())
                .departmentName(departmentName)
                .customRoleId(user.getCustomRoleId())
                .customRoleName(customRoleName)
                .build();
    }

    private String companyNameOf(Long companyId) {
        return companyId == null ? null : companyRepository.findById(companyId).map(Company::getName).orElse(null);
    }

    private String branchNameOf(Long branchId) {
        return branchId == null ? null : branchRepository.findById(branchId).map(Branch::getName).orElse(null);
    }

    private String departmentNameOf(Long departmentId) {
        return departmentId == null ? null : departmentRepository.findById(departmentId).map(Department::getName).orElse(null);
    }

    private String customRoleNameOf(Long customRoleId) {
        return customRoleId == null ? null : customRoleRepository.findById(customRoleId).map(CustomRole::getName).orElse(null);
    }
}
