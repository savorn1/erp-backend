package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long warehouseId;
    private String warehouseName;
    private String code;
    private String name;
    private boolean active;
    // Convenience for the checkout UI — whether this register currently has
    // an OPEN PosSession, and its id if so.
    private boolean hasOpenSession;
    private Long openPosSessionId;
}
