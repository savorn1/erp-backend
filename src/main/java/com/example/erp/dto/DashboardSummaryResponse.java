package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;

    // Sum of approved sales Invoice totals in the period — recognized income.
    private BigDecimal revenue;
    // Sum of approved PurchaseInvoice totals in the period — recognized cost.
    private BigDecimal expense;
    // revenue - expense.
    private BigDecimal profit;
    // Sum of every BankAccount's currentBalance, right now.
    private BigDecimal cash;
    // Sum of outstandingAmount across every approved, unpaid sales Invoice, right now.
    private BigDecimal receivable;
    // Sum of outstandingAmount across every approved, unpaid PurchaseInvoice, right now.
    private BigDecimal payable;
    // Sum of InventoryOverview's valuationValue (currentStock * unitCost) across every product/warehouse, right now.
    private BigDecimal inventoryValue;
    // Sum of SalesOrder totals booked in the period (CONFIRMED/PARTIALLY_DELIVERED/DELIVERED) — order-level activity, not yet necessarily invoiced.
    private BigDecimal sales;
    // Sum of PurchaseOrder totals booked in the period (SENT/PARTIALLY_RECEIVED/RECEIVED) — order-level activity, not yet necessarily billed.
    private BigDecimal purchase;
}
