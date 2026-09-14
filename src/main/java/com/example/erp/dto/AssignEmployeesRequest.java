package com.example.erp.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AssignEmployeesRequest {

    @NotEmpty
    private List<Long> userIds;
}
