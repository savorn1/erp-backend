package com.example.erp.dto;

import com.example.erp.entity.MachineStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class MachineFilterRequest {

    private String name;
    private Long companyId;
    private Long workCenterId;
    private MachineStatus status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
