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
public class NotificationSummaryResponse {

    private List<NotificationItem> items;
    // Number of active alert categories, not a sum of their individual
    // counts/amounts — those are different units and summing them wouldn't
    // mean anything.
    private int totalCount;
}
