package com.example.erp.dto;

import com.example.erp.entity.Role;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class UserFilterRequest {

    private String username;
    private Role role;
    private Boolean enabled;
    private Long departmentId;
    private Long companyId;
    private Long branchId;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
