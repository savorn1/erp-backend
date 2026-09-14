package com.example.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Self-referencing FK forming the org hierarchy — null means a top-level
    // department. Cycle prevention lives in DepartmentServiceImpl, not here.
    @Column(name = "parent_department_id")
    private Long parentDepartmentId;

    // The User who heads this department — optional, same pattern as Branch.managerId.
    @Column(name = "manager_id")
    private Long managerId;

    @Builder.Default
    private boolean active = true;
}
