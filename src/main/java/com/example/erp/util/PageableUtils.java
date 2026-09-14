package com.example.erp.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

// Shared page/size/sortBy/sortOrder -> Pageable conversion, used by every
// filter request so the "1-based page number, default-desc sort" convention
// stays in one place.
public final class PageableUtils {

    private PageableUtils() {
    }

    public static Pageable of(int page, int size, String sortBy, String sortOrder) {
        Sort sort = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(Math.max(page - 1, 0), size, sort);
    }
}
