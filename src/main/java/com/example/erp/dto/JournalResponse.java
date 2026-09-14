package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private String code;
    private String name;
    private String description;
    private boolean active;
}
