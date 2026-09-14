package com.example.erp.dto;

import lombok.Data;

@Data
public class AssignLeadRequest {

    // Null unassigns the lead.
    private Long assignedToUserId;
}
