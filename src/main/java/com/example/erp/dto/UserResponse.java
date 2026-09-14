package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
    private boolean enabled;
    private Long companyId;
    private String companyName;
    private Long branchId;
    private String branchName;
    private Long departmentId;
    private String departmentName;
}
