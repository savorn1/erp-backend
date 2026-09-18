package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// One alert category currently needing attention. Exactly one of
// count/amount is set — count-based categories (low stock, pending
// approvals) carry count; the two overdue-invoice categories carry amount
// instead, formatted by the frontend the same way every other money value
// is. `label` is a plain description the frontend prefixes with the count/
// amount, e.g. "5" + "products below reorder point".
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationItem {

    private String type;
    private String label;
    private Integer count;
    private BigDecimal amount;
    private String link;
}
