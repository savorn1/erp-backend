package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerGroupResponse {

    private Long id;
    private String name;
    private Long priceGroupId;
    private String priceGroupName;
    private boolean active;
}
