package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.AssignEmployeesRequest;
import com.example.erp.dto.CreateDepartmentRequest;
import com.example.erp.dto.DepartmentFilterRequest;
import com.example.erp.dto.DepartmentResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateDepartmentRequest;
import com.example.erp.dto.UpdateDepartmentStatusRequest;
import com.example.erp.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Admin-only department management — each department belongs to a Company and
// optionally a parent Department (org hierarchy) and a manager User. Employee
// assignment sets the User's own departmentId (see AssignEmployeesRequest);
// there's no separate join table since an employee belongs to at most one
// department at a time.
@RestController
@RequestMapping("/api/admin/departments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<PageResponse<DepartmentResponse>> list(@ModelAttribute DepartmentFilterRequest filter) {
        return ResponseEntity.ok(departmentService.listDepartments(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getDepartment(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponse>> create(@Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Department created", departmentService.createDepartment(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> update(@PathVariable Long id,
                                                                    @Valid @RequestBody UpdateDepartmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Department updated", departmentService.updateDepartment(id, request)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateStatus(@PathVariable Long id,
                                                                          @Valid @RequestBody UpdateDepartmentStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", departmentService.updateStatus(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("Department deleted", null));
    }

    @PostMapping("/{id}/employees")
    public ResponseEntity<ApiResponse<Void>> assignEmployees(@PathVariable Long id,
                                                                @Valid @RequestBody AssignEmployeesRequest request) {
        departmentService.assignEmployees(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employees assigned", null));
    }

    @DeleteMapping("/{id}/employees/{userId}")
    public ResponseEntity<ApiResponse<Void>> unassignEmployee(@PathVariable Long id, @PathVariable Long userId) {
        departmentService.unassignEmployee(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Employee unassigned", null));
    }
}
