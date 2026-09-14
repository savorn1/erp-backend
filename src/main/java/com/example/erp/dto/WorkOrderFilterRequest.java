package com.example.erp.dto;

import com.example.erp.entity.WorkOrderStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Data
@ParameterObject
public class WorkOrderFilterRequest {

    private Long manufacturingOrderId;
    private Long workCenterId;
    private Long machineId;
    private WorkOrderStatus status;
    private LocalDate dateFrom;
    private LocalDate dateTo;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
