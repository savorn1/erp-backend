package com.example.erp.service.impl;

import com.example.erp.dto.AssignEmployeesRequest;
import com.example.erp.dto.CreateDepartmentRequest;
import com.example.erp.dto.DepartmentFilterRequest;
import com.example.erp.dto.DepartmentResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateDepartmentRequest;
import com.example.erp.dto.UpdateDepartmentStatusRequest;
import com.example.erp.entity.Department;
import com.example.erp.entity.User;
import com.example.erp.exception.AppException;
import com.example.erp.repository.DepartmentRepository;
import com.example.erp.repository.UserRepository;
import com.example.erp.service.DepartmentService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponse> listDepartments(DepartmentFilterRequest filter) {
        List<Specification<Department>> conditions = new ArrayList<>();

        if (filter.getName() != null && !filter.getName().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        }
        if (filter.getParentDepartmentId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("parentDepartmentId"), filter.getParentDepartmentId()));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        Specification<Department> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<Department> departments = departmentRepository.findAll(spec, pageable);
        List<Department> content = departments.getContent();

        List<Long> parentIds = content.stream().map(Department::getParentDepartmentId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> parentNames = departmentRepository.findAllById(parentIds).stream()
                .collect(Collectors.toMap(Department::getId, Department::getName));

        List<Long> managerIds = content.stream().map(Department::getManagerId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> managerUsernames = userRepository.findAllById(managerIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        Map<Long, Long> employeeCounts = content.stream()
                .collect(Collectors.toMap(Department::getId, d -> userRepository.countByDepartmentId(d.getId())));

        return PageResponse.of(departments.map(d -> toResponse(d,
                d.getParentDepartmentId() == null ? null : parentNames.get(d.getParentDepartmentId()),
                d.getManagerId() == null ? null : managerUsernames.get(d.getManagerId()),
                employeeCounts.getOrDefault(d.getId(), 0L))));
    }

    @Override
    public DepartmentResponse getDepartment(Long id) {
        Department department = findDepartment(id);
        return toResponse(department,
                department.getParentDepartmentId() == null ? null : departmentNameOf(department.getParentDepartmentId()),
                usernameOf(department.getManagerId()),
                userRepository.countByDepartmentId(id));
    }

    @Override
    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        Department parent = request.getParentDepartmentId() == null ? null : requireParent(request.getParentDepartmentId());
        User manager = request.getManagerId() == null ? null : requireManager(request.getManagerId());
        if (departmentRepository.existsByName(request.getName())) {
            throw new AppException(HttpStatus.CONFLICT, "Department name already taken: " + request.getName());
        }

        Department department = Department.builder()
                .name(request.getName())
                .parentDepartmentId(request.getParentDepartmentId())
                .managerId(request.getManagerId())
                .build();
        departmentRepository.save(department);
        return toResponse(department, parent == null ? null : parent.getName(),
                manager == null ? null : manager.getUsername(), 0);
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request) {
        Department department = findDepartment(id);
        Department parent = request.getParentDepartmentId() == null ? null : requireParent(request.getParentDepartmentId());
        User manager = request.getManagerId() == null ? null : requireManager(request.getManagerId());
        if (departmentRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "Department name already taken: " + request.getName());
        }
        assertNoCycle(id, request.getParentDepartmentId());

        department.setName(request.getName());
        department.setParentDepartmentId(request.getParentDepartmentId());
        department.setManagerId(request.getManagerId());
        departmentRepository.save(department);
        return toResponse(department, parent == null ? null : parent.getName(),
                manager == null ? null : manager.getUsername(), userRepository.countByDepartmentId(id));
    }

    @Override
    @Transactional
    public DepartmentResponse updateStatus(Long id, UpdateDepartmentStatusRequest request) {
        Department department = findDepartment(id);
        department.setActive(request.getActive());
        departmentRepository.save(department);
        return toResponse(department,
                department.getParentDepartmentId() == null ? null : departmentNameOf(department.getParentDepartmentId()),
                usernameOf(department.getManagerId()),
                userRepository.countByDepartmentId(id));
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        findDepartment(id);
        if (departmentRepository.existsByParentDepartmentId(id)) {
            throw new AppException(HttpStatus.CONFLICT, "Cannot delete a department that has sub-departments — reassign or remove them first");
        }
        if (userRepository.existsByDepartmentId(id)) {
            throw new AppException(HttpStatus.CONFLICT, "Cannot delete a department that has employees assigned — reassign them first");
        }
        departmentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void assignEmployees(Long id, AssignEmployeesRequest request) {
        findDepartment(id);
        List<User> users = userRepository.findAllById(request.getUserIds());
        if (users.size() != new HashSet<>(request.getUserIds()).size()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "One or more users were not found");
        }
        users.forEach(u -> u.setDepartmentId(id));
        userRepository.saveAll(users);
    }

    @Override
    @Transactional
    public void unassignEmployee(Long id, Long userId) {
        findDepartment(id);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found with id: " + userId));
        if (!id.equals(user.getDepartmentId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "User is not assigned to this department");
        }
        user.setDepartmentId(null);
        userRepository.save(user);
    }

    // Walks up from the candidate new parent's own parent chain — if it ever
    // reaches `departmentId`, that parent is (transitively) a descendant of the
    // department being updated, so accepting it would close a cycle.
    private void assertNoCycle(Long departmentId, Long newParentId) {
        if (newParentId == null) return;
        if (newParentId.equals(departmentId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "A department cannot be its own parent");
        }
        Set<Long> visited = new HashSet<>();
        Long current = newParentId;
        while (current != null) {
            if (!visited.add(current)) break;
            if (current.equals(departmentId)) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Cannot set parent — this would create a circular department hierarchy");
            }
            current = departmentRepository.findById(current).map(Department::getParentDepartmentId).orElse(null);
        }
    }

    private Department requireParent(Long parentId) {
        return departmentRepository.findById(parentId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Parent department not found with id: " + parentId));
    }

    private User requireManager(Long managerId) {
        return userRepository.findById(managerId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Manager not found with id: " + managerId));
    }

    private String departmentNameOf(Long departmentId) {
        return departmentRepository.findById(departmentId).map(Department::getName).orElse(null);
    }

    private String usernameOf(Long userId) {
        return userId == null ? null : userRepository.findById(userId).map(User::getUsername).orElse(null);
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Department not found with id: " + id));
    }

    private DepartmentResponse toResponse(Department department, String parentDepartmentName,
                                           String managerUsername, long employeeCount) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .parentDepartmentId(department.getParentDepartmentId())
                .parentDepartmentName(parentDepartmentName)
                .managerId(department.getManagerId())
                .managerUsername(managerUsername)
                .employeeCount(employeeCount)
                .active(department.isActive())
                .build();
    }
}
