package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Rows for every product/warehouse with currentStock > 0 — sorted by
// outboundQuantityInWindow descending. Fast Moving reads the top of this
// list, Slow Moving the bottom (excluding zero), Dead Stock the rows with
// zero outbound and lastOutboundDate outside the window (or never).
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTurnoverResponse {

    private List<StockTurnoverRowResponse> rows;
    private int windowDays;
}
