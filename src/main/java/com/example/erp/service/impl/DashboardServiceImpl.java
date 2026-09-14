package com.example.erp.service.impl;

import com.example.erp.dto.DashboardSummaryFilterRequest;
import com.example.erp.dto.DashboardSummaryResponse;
import com.example.erp.dto.DashboardTrendFilterRequest;
import com.example.erp.dto.DashboardTrendPointResponse;
import com.example.erp.dto.DashboardTrendResponse;
import com.example.erp.dto.InventoryOverviewFilterRequest;
import com.example.erp.entity.BankAccount;
import com.example.erp.entity.CreditNote;
import com.example.erp.entity.Invoice;
import com.example.erp.entity.InvoiceLine;
import com.example.erp.entity.InvoiceStatus;
import com.example.erp.entity.PaymentAllocation;
import com.example.erp.entity.PurchaseCreditNote;
import com.example.erp.entity.PurchaseInvoice;
import com.example.erp.entity.PurchaseInvoiceLine;
import com.example.erp.entity.PurchaseInvoiceStatus;
import com.example.erp.entity.PurchaseOrder;
import com.example.erp.entity.PurchaseOrderLine;
import com.example.erp.entity.PurchaseOrderStatus;
import com.example.erp.entity.SalesOrder;
import com.example.erp.entity.SalesOrderLine;
import com.example.erp.entity.SalesOrderStatus;
import com.example.erp.entity.SupplierPaymentAllocation;
import com.example.erp.repository.BankAccountRepository;
import com.example.erp.repository.CreditNoteRepository;
import com.example.erp.repository.InvoiceLineRepository;
import com.example.erp.repository.InvoiceRepository;
import com.example.erp.repository.PaymentAllocationRepository;
import com.example.erp.repository.PurchaseCreditNoteRepository;
import com.example.erp.repository.PurchaseInvoiceLineRepository;
import com.example.erp.repository.PurchaseInvoiceRepository;
import com.example.erp.repository.PurchaseOrderLineRepository;
import com.example.erp.repository.PurchaseOrderRepository;
import com.example.erp.repository.SalesOrderLineRepository;
import com.example.erp.repository.SalesOrderRepository;
import com.example.erp.repository.SupplierPaymentAllocationRepository;
import com.example.erp.service.DashboardService;
import com.example.erp.service.InventoryOverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Sums real transactional data rather than the general ledger — see
// FinancialReportService's own comment on why Journal-Entry-derived reports
// stay sparse until something posts to them automatically. A dashboard needs
// numbers that reflect what's actually happening today.
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final CreditNoteRepository creditNoteRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final PurchaseInvoiceLineRepository purchaseInvoiceLineRepository;
    private final PurchaseCreditNoteRepository purchaseCreditNoteRepository;
    private final SupplierPaymentAllocationRepository supplierPaymentAllocationRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final BankAccountRepository bankAccountRepository;
    private final InventoryOverviewService inventoryOverviewService;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary(DashboardSummaryFilterRequest filter) {
        BigDecimal revenue = revenueTotal(filter);
        BigDecimal expense = expenseTotal(filter);
        BigDecimal sales = salesTotal(filter);
        BigDecimal purchase = purchaseTotal(filter);

        return DashboardSummaryResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .revenue(revenue)
                .expense(expense)
                .profit(revenue.subtract(expense))
                .cash(cashTotal(filter.getCompanyId()))
                .receivable(receivableTotal(filter.getCompanyId()))
                .payable(payableTotal(filter.getCompanyId()))
                .inventoryValue(inventoryValueTotal(filter.getCompanyId()))
                .sales(sales)
                .purchase(purchase)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardTrendResponse trend(DashboardTrendFilterRequest filter) {
        int months = filter.getMonths() != null ? Math.max(1, Math.min(filter.getMonths(), 24)) : 6;
        YearMonth endMonth = YearMonth.now();
        YearMonth startMonth = endMonth.minusMonths(months - 1L);
        LocalDate dateFrom = startMonth.atDay(1);
        LocalDate dateTo = endMonth.atEndOfMonth();

        Map<YearMonth, BigDecimal> salesByMonth = new HashMap<>();
        Set<SalesOrderStatus> bookedSales = Set.of(SalesOrderStatus.CONFIRMED, SalesOrderStatus.PARTIALLY_DELIVERED, SalesOrderStatus.DELIVERED);
        List<Specification<SalesOrder>> salesConditions = new ArrayList<>();
        salesConditions.add((root, query, cb) -> root.get("status").in(bookedSales));
        salesConditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("orderDate"), dateFrom));
        salesConditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("orderDate"), dateTo));
        if (filter.getCompanyId() != null) salesConditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        for (SalesOrder order : salesOrderRepository.findAll(Specification.allOf(salesConditions))) {
            BigDecimal total = salesOrderTotal(salesOrderLineRepository.findBySalesOrderId(order.getId()));
            salesByMonth.merge(YearMonth.from(order.getOrderDate()), total, BigDecimal::add);
        }

        Map<YearMonth, BigDecimal> purchaseByMonth = new HashMap<>();
        Set<PurchaseOrderStatus> bookedPurchases = Set.of(PurchaseOrderStatus.SENT, PurchaseOrderStatus.PARTIALLY_RECEIVED, PurchaseOrderStatus.RECEIVED);
        List<Specification<PurchaseOrder>> purchaseConditions = new ArrayList<>();
        purchaseConditions.add((root, query, cb) -> root.get("status").in(bookedPurchases));
        purchaseConditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("orderDate"), dateFrom));
        purchaseConditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("orderDate"), dateTo));
        if (filter.getCompanyId() != null) purchaseConditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        for (PurchaseOrder order : purchaseOrderRepository.findAll(Specification.allOf(purchaseConditions))) {
            BigDecimal total = purchaseOrderTotal(purchaseOrderLineRepository.findByPurchaseOrderId(order.getId()));
            purchaseByMonth.merge(YearMonth.from(order.getOrderDate()), total, BigDecimal::add);
        }

        List<DashboardTrendPointResponse> points = new ArrayList<>();
        for (YearMonth m = startMonth; !m.isAfter(endMonth); m = m.plusMonths(1)) {
            points.add(DashboardTrendPointResponse.builder()
                    .month(m.toString())
                    .sales(salesByMonth.getOrDefault(m, BigDecimal.ZERO))
                    .purchase(purchaseByMonth.getOrDefault(m, BigDecimal.ZERO))
                    .build());
        }

        return DashboardTrendResponse.builder().months(points).build();
    }

    private BigDecimal revenueTotal(DashboardSummaryFilterRequest filter) {
        List<Specification<Invoice>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> cb.equal(root.get("status"), InvoiceStatus.APPROVED));
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getDateFrom() != null) conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("invoiceDate"), filter.getDateFrom()));
        if (filter.getDateTo() != null) conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("invoiceDate"), filter.getDateTo()));

        BigDecimal total = BigDecimal.ZERO;
        for (Invoice invoice : invoiceRepository.findAll(Specification.allOf(conditions))) {
            total = total.add(invoiceTotal(invoiceLineRepository.findByInvoiceId(invoice.getId())));
        }
        return total;
    }

    private BigDecimal expenseTotal(DashboardSummaryFilterRequest filter) {
        List<Specification<PurchaseInvoice>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> cb.equal(root.get("status"), PurchaseInvoiceStatus.APPROVED));
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getDateFrom() != null) conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("invoiceDate"), filter.getDateFrom()));
        if (filter.getDateTo() != null) conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("invoiceDate"), filter.getDateTo()));

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseInvoice invoice : purchaseInvoiceRepository.findAll(Specification.allOf(conditions))) {
            total = total.add(purchaseInvoiceTotal(purchaseInvoiceLineRepository.findByPurchaseInvoiceId(invoice.getId())));
        }
        return total;
    }

    private BigDecimal salesTotal(DashboardSummaryFilterRequest filter) {
        Set<SalesOrderStatus> booked = Set.of(SalesOrderStatus.CONFIRMED, SalesOrderStatus.PARTIALLY_DELIVERED, SalesOrderStatus.DELIVERED);
        List<Specification<SalesOrder>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> root.get("status").in(booked));
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getDateFrom() != null) conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("orderDate"), filter.getDateFrom()));
        if (filter.getDateTo() != null) conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("orderDate"), filter.getDateTo()));

        BigDecimal total = BigDecimal.ZERO;
        for (SalesOrder order : salesOrderRepository.findAll(Specification.allOf(conditions))) {
            total = total.add(salesOrderTotal(salesOrderLineRepository.findBySalesOrderId(order.getId())));
        }
        return total;
    }

    private BigDecimal purchaseTotal(DashboardSummaryFilterRequest filter) {
        Set<PurchaseOrderStatus> booked = Set.of(PurchaseOrderStatus.SENT, PurchaseOrderStatus.PARTIALLY_RECEIVED, PurchaseOrderStatus.RECEIVED);
        List<Specification<PurchaseOrder>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> root.get("status").in(booked));
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getDateFrom() != null) conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("orderDate"), filter.getDateFrom()));
        if (filter.getDateTo() != null) conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("orderDate"), filter.getDateTo()));

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrder order : purchaseOrderRepository.findAll(Specification.allOf(conditions))) {
            total = total.add(purchaseOrderTotal(purchaseOrderLineRepository.findByPurchaseOrderId(order.getId())));
        }
        return total;
    }

    private BigDecimal cashTotal(Long companyId) {
        BigDecimal total = BigDecimal.ZERO;
        for (BankAccount account : bankAccountRepository.findAll()) {
            if (companyId != null && !companyId.equals(account.getCompanyId())) continue;
            total = total.add(account.getCurrentBalance());
        }
        return total;
    }

    private BigDecimal receivableTotal(Long companyId) {
        List<Specification<Invoice>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> cb.equal(root.get("status"), InvoiceStatus.APPROVED));
        if (companyId != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), companyId));

        BigDecimal total = BigDecimal.ZERO;
        for (Invoice invoice : invoiceRepository.findAll(Specification.allOf(conditions))) {
            BigDecimal totalAmount = invoiceTotal(invoiceLineRepository.findByInvoiceId(invoice.getId()));
            BigDecimal credited = creditNoteRepository.findByInvoiceId(invoice.getId()).stream()
                    .map(CreditNote::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal paid = paymentAllocationRepository.findByInvoiceId(invoice.getId()).stream()
                    .map(PaymentAllocation::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            total = total.add(totalAmount.subtract(credited).subtract(paid));
        }
        return total;
    }

    private BigDecimal payableTotal(Long companyId) {
        List<Specification<PurchaseInvoice>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> cb.equal(root.get("status"), PurchaseInvoiceStatus.APPROVED));
        if (companyId != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), companyId));

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseInvoice invoice : purchaseInvoiceRepository.findAll(Specification.allOf(conditions))) {
            BigDecimal totalAmount = purchaseInvoiceTotal(purchaseInvoiceLineRepository.findByPurchaseInvoiceId(invoice.getId()));
            BigDecimal credited = purchaseCreditNoteRepository.findByPurchaseInvoiceId(invoice.getId()).stream()
                    .map(PurchaseCreditNote::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal paid = supplierPaymentAllocationRepository.findByPurchaseInvoiceId(invoice.getId()).stream()
                    .map(SupplierPaymentAllocation::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            total = total.add(totalAmount.subtract(credited).subtract(paid));
        }
        return total;
    }

    private BigDecimal inventoryValueTotal(Long companyId) {
        InventoryOverviewFilterRequest filter = new InventoryOverviewFilterRequest();
        filter.setCompanyId(companyId);
        filter.setSize(1_000_000);
        return inventoryOverviewService.getOverview(filter).getData().stream()
                .map(row -> row.getValuationValue() == null ? BigDecimal.ZERO : row.getValuationValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal invoiceTotal(List<InvoiceLine> lines) {
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceLine line : lines) {
            total = total.add(lineTotal(line.getQuantity(), line.getUnitPrice(), line.getDiscountPercent(), line.getTaxRate()));
        }
        return total;
    }

    private BigDecimal purchaseInvoiceTotal(List<PurchaseInvoiceLine> lines) {
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseInvoiceLine line : lines) {
            total = total.add(lineTotal(line.getQuantity(), line.getUnitCost(), line.getDiscountPercent(), line.getTaxRate()));
        }
        return total;
    }

    private BigDecimal salesOrderTotal(List<SalesOrderLine> lines) {
        BigDecimal total = BigDecimal.ZERO;
        for (SalesOrderLine line : lines) {
            total = total.add(lineTotal(line.getQuantityOrdered(), line.getUnitPrice(), line.getDiscountPercent(), line.getTaxRate()));
        }
        return total;
    }

    private BigDecimal purchaseOrderTotal(List<PurchaseOrderLine> lines) {
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderLine line : lines) {
            total = total.add(lineTotal(line.getQuantityOrdered(), line.getUnitCost(), line.getDiscountPercent(), line.getTaxRate()));
        }
        return total;
    }

    // Same subtotal -> discount -> tax formula every line-item entity in this
    // codebase uses (see e.g. InvoiceServiceImpl.computeTotals).
    private BigDecimal lineTotal(BigDecimal quantity, BigDecimal unitAmount, BigDecimal discountPercent, BigDecimal taxRate) {
        BigDecimal lineSubtotal = quantity.multiply(unitAmount);
        BigDecimal discount = lineSubtotal.multiply(discountPercent).divide(HUNDRED, 4, RoundingMode.HALF_UP);
        BigDecimal afterDiscount = lineSubtotal.subtract(discount);
        BigDecimal tax = afterDiscount.multiply(taxRate).divide(HUNDRED, 4, RoundingMode.HALF_UP);
        return afterDiscount.add(tax);
    }
}
