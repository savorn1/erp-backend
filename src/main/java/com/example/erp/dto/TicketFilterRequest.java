package com.example.erp.dto;

import com.example.erp.entity.TicketPriority;
import com.example.erp.entity.TicketStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class TicketFilterRequest {

    private Long companyId;
    private Long customerId;
    private TicketStatus status;
    private TicketPriority priority;
    private Long assignedToUserId;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
