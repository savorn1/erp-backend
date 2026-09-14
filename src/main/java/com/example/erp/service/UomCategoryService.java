package com.example.erp.service;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UomCategoryFilterRequest;
import com.example.erp.dto.UomCategoryRequest;
import com.example.erp.dto.UomCategoryResponse;

import java.util.List;

public interface UomCategoryService {

    PageResponse<UomCategoryResponse> list(UomCategoryFilterRequest filter);

    UomCategoryResponse get(Long id);

    UomCategoryResponse create(UomCategoryRequest request);

    UomCategoryResponse update(Long id, UomCategoryRequest request);

    void delete(Long id);

    // Idempotent — creates the standard QUANTITY (PCS base, Box/Carton),
    // WEIGHT (KG base, Gram/Ton), VOLUME (L base, Milliliter), LENGTH (M
    // base, Centimeter), AREA (M2 base), and TIME (Hour base, Minute/Day)
    // categories, skipping any category code or unit name that already exists.
    List<UomCategoryResponse> seedStandardCategories();
}
