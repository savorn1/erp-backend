package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductTypeResponse {

    private Long id;
    // Null on types created before codes existed, until they're edited.
    private String code;
    private String name;
    private boolean active;
}
