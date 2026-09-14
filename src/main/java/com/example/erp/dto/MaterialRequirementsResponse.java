package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialRequirementsResponse {

    // Live snapshot of DRAFT/RELEASED orders — not date-ranged.
    private List<MaterialRequirementRowResponse> rows;
    private long shortageCount;
}
