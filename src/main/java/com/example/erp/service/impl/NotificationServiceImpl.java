package com.example.erp.service.impl;

import com.example.erp.dto.InvoiceAgingFilterRequest;
import com.example.erp.dto.InvoiceAgingReportResponse;
import com.example.erp.dto.InvoiceAgingRowResponse;
import com.example.erp.dto.InventoryOverviewFilterRequest;
import com.example.erp.dto.LowStockResponse;
import com.example.erp.dto.NotificationItem;
import com.example.erp.dto.NotificationSummaryResponse;
import com.example.erp.dto.PurchaseInvoiceAgingFilterRequest;
import com.example.erp.dto.PurchaseInvoiceAgingReportResponse;
import com.example.erp.dto.PurchaseInvoiceAgingRowResponse;
import com.example.erp.entity.Lead;
import com.example.erp.entity.LeadStatus;
import com.example.erp.entity.PurchaseOrderStatus;
import com.example.erp.entity.PurchaseRequestStatus;
import com.example.erp.entity.SalesOrderStatus;
import com.example.erp.entity.StockAdjustmentStatus;
import com.example.erp.entity.Ticket;
import com.example.erp.entity.TicketStatus;
import com.example.erp.repository.LeadRepository;
import com.example.erp.repository.PurchaseOrderRepository;
import com.example.erp.repository.PurchaseRequestRepository;
import com.example.erp.repository.SalesOrderRepository;
import com.example.erp.repository.StockAdjustmentRepository;
import com.example.erp.repository.TicketRepository;
import com.example.erp.service.InventoryReportService;
import com.example.erp.service.InvoiceService;
import com.example.erp.service.NotificationService;
import com.example.erp.service.PurchaseInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final InventoryReportService inventoryReportService;
    private final InvoiceService invoiceService;
    private final PurchaseInvoiceService purchaseInvoiceService;
    private final SalesOrderRepository salesOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final TicketRepository ticketRepository;
    private final LeadRepository leadRepository;

    @Override
    public NotificationSummaryResponse getSummary() {
        List<NotificationItem> items = new ArrayList<>();

        LowStockResponse lowStock = inventoryReportService.lowStock(new InventoryOverviewFilterRequest());
        if (lowStock.getCount() > 0) {
            items.add(NotificationItem.builder()
                    .type("LOW_STOCK")
                    .label("products below reorder point")
                    .count(lowStock.getCount())
                    .link("/reports/inventory/low-stock")
                    .build());
        }

        InvoiceAgingReportResponse arAging = invoiceService.agingReport(new InvoiceAgingFilterRequest());
        BigDecimal overdueAr = overdueAmount(arAging.getTotals());
        if (overdueAr.signum() > 0) {
            long overdueCustomers = arAging.getRows().stream()
                    .filter(r -> r.getCustomerId() != null && overdueAmount(r).signum() > 0)
                    .count();
            items.add(NotificationItem.builder()
                    .type("OVERDUE_INVOICES")
                    .label("overdue across " + overdueCustomers + " customer" + (overdueCustomers == 1 ? "" : "s"))
                    .amount(overdueAr)
                    .link("/reports/accounts-receivable/summary")
                    .build());
        }

        PurchaseInvoiceAgingReportResponse apAging = purchaseInvoiceService.agingReport(new PurchaseInvoiceAgingFilterRequest());
        BigDecimal overdueAp = overdueAmount(apAging.getTotals());
        if (overdueAp.signum() > 0) {
            long overdueSuppliers = apAging.getRows().stream()
                    .filter(r -> r.getSupplierId() != null && overdueAmount(r).signum() > 0)
                    .count();
            items.add(NotificationItem.builder()
                    .type("OVERDUE_PURCHASE_INVOICES")
                    .label("overdue across " + overdueSuppliers + " supplier" + (overdueSuppliers == 1 ? "" : "s"))
                    .amount(overdueAp)
                    .link("/reports/accounts-payable/summary")
                    .build());
        }

        addPendingApproval(items, "PENDING_SALES_ORDERS", "sales orders awaiting approval",
                "/sales-orders", salesOrderRepository.countByStatus(SalesOrderStatus.SUBMITTED));
        addPendingApproval(items, "PENDING_PURCHASE_ORDERS", "purchase orders awaiting approval",
                "/purchase-orders", purchaseOrderRepository.countByStatus(PurchaseOrderStatus.SUBMITTED));
        addPendingApproval(items, "PENDING_PURCHASE_REQUESTS", "purchase requests awaiting approval",
                "/purchase-requests", purchaseRequestRepository.countByStatus(PurchaseRequestStatus.SUBMITTED));
        addPendingApproval(items, "PENDING_STOCK_ADJUSTMENTS", "stock adjustments awaiting approval",
                "/stock-adjustments", stockAdjustmentRepository.countByStatus(StockAdjustmentStatus.PENDING));

        long overdueTickets = ticketRepository.findByStatusIn(List.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS)).stream()
                .filter(Ticket::isOverdue)
                .count();
        addPendingApproval(items, "OVERDUE_TICKETS", "tickets overdue", "/tickets?overdue=true", overdueTickets);

        long leadsFollowUpDue = leadRepository.findByStatusNotIn(List.of(LeadStatus.WON, LeadStatus.LOST)).stream()
                .filter(Lead::isFollowUpDue)
                .count();
        addPendingApproval(items, "LEADS_FOLLOW_UP_DUE", "leads due for follow-up", "/leads?followUpDue=true", leadsFollowUpDue);

        return NotificationSummaryResponse.builder()
                .items(items)
                .totalCount(items.size())
                .build();
    }

    private void addPendingApproval(List<NotificationItem> items, String type, String label, String link, long count) {
        if (count <= 0) return;
        items.add(NotificationItem.builder()
                .type(type)
                .label(label)
                .count((int) count)
                .link(link)
                .build());
    }

    private BigDecimal overdueAmount(InvoiceAgingRowResponse row) {
        return row.getDays1To30().add(row.getDays31To60()).add(row.getDays61To90()).add(row.getDays90Plus());
    }

    private BigDecimal overdueAmount(PurchaseInvoiceAgingRowResponse row) {
        return row.getDays1To30().add(row.getDays31To60()).add(row.getDays61To90()).add(row.getDays90Plus());
    }
}
