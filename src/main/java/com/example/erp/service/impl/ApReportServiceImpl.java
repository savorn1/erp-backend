package com.example.erp.service.impl;

import com.example.erp.dto.ApDetailResponse;
import com.example.erp.dto.ApDetailRowResponse;
import com.example.erp.dto.ApPaymentResponse;
import com.example.erp.dto.ApPaymentRowResponse;
import com.example.erp.dto.ApReportFilterRequest;
import com.example.erp.dto.ApSummaryResponse;
import com.example.erp.dto.SupplierBalanceResponse;
import com.example.erp.dto.SupplierBalanceRowResponse;
import com.example.erp.dto.SupplierStatementLineResponse;
import com.example.erp.dto.SupplierStatementResponse;
import com.example.erp.entity.PaymentType;
import com.example.erp.entity.Product;
import com.example.erp.entity.PurchaseCreditNote;
import com.example.erp.entity.PurchaseInvoice;
import com.example.erp.entity.PurchaseInvoiceLine;
import com.example.erp.entity.PurchaseInvoiceStatus;
import com.example.erp.entity.Supplier;
import com.example.erp.entity.SupplierPayment;
import com.example.erp.entity.SupplierPaymentAllocation;
import com.example.erp.exception.AppException;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.PurchaseCreditNoteRepository;
import com.example.erp.repository.PurchaseInvoiceLineRepository;
import com.example.erp.repository.PurchaseInvoiceRepository;
import com.example.erp.repository.SupplierPaymentAllocationRepository;
import com.example.erp.repository.SupplierPaymentRepository;
import com.example.erp.repository.SupplierRepository;
import com.example.erp.service.ApReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ApReportServiceImpl implements ApReportService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final PurchaseInvoiceLineRepository purchaseInvoiceLineRepository;
    private final PurchaseCreditNoteRepository purchaseCreditNoteRepository;
    private final SupplierPaymentAllocationRepository supplierPaymentAllocationRepository;
    private final SupplierPaymentRepository supplierPaymentRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public ApSummaryResponse summary(ApReportFilterRequest filter) {
        LocalDate asOfDate = filter.getAsOfDate() != null ? filter.getAsOfDate() : LocalDate.now();

        BigDecimal totalOutstanding = BigDecimal.ZERO;
        BigDecimal totalOverdue = BigDecimal.ZERO;
        long invoiceCount = 0;
        long overdueInvoiceCount = 0;
        long oldestOverdueDays = 0;
        Set<Long> suppliers = new HashSet<>();
        Set<Long> overdueSuppliers = new HashSet<>();

        for (PurchaseInvoice invoice : approvedInvoices(filter.getCompanyId())) {
            BigDecimal outstanding = outstandingAmount(invoice);
            if (outstanding.signum() <= 0) continue;

            long days = daysOverdue(invoice.getDueDate(), asOfDate);
            invoiceCount++;
            totalOutstanding = totalOutstanding.add(outstanding);
            suppliers.add(invoice.getSupplierId());
            if (days > 0) {
                overdueInvoiceCount++;
                totalOverdue = totalOverdue.add(outstanding);
                overdueSuppliers.add(invoice.getSupplierId());
                oldestOverdueDays = Math.max(oldestOverdueDays, days);
            }
        }

        return ApSummaryResponse.builder()
                .asOfDate(asOfDate)
                .totalOutstanding(totalOutstanding)
                .totalOverdue(totalOverdue)
                .invoiceCount(invoiceCount)
                .supplierCount(suppliers.size())
                .overdueInvoiceCount(overdueInvoiceCount)
                .overdueSupplierCount(overdueSuppliers.size())
                .oldestOverdueDays(oldestOverdueDays)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApDetailResponse detail(ApReportFilterRequest filter) {
        LocalDate asOfDate = filter.getAsOfDate() != null ? filter.getAsOfDate() : LocalDate.now();

        List<ApDetailRowResponse> rows = new ArrayList<>();
        BigDecimal totalOutstanding = BigDecimal.ZERO;
        for (PurchaseInvoice invoice : approvedInvoices(filter.getCompanyId())) {
            if (filter.getSupplierId() != null && !filter.getSupplierId().equals(invoice.getSupplierId())) continue;
            BigDecimal outstanding = outstandingAmount(invoice);
            if (outstanding.signum() <= 0) continue;

            Supplier supplier = supplierRepository.findById(invoice.getSupplierId()).orElse(null);
            long days = daysOverdue(invoice.getDueDate(), asOfDate);
            for (PurchaseInvoiceLine line : purchaseInvoiceLineRepository.findByPurchaseInvoiceId(invoice.getId())) {
                Product product = productRepository.findById(line.getProductId()).orElse(null);
                BigDecimal lineTotal = lineTotal(line);
                rows.add(ApDetailRowResponse.builder()
                        .invoiceId(invoice.getId())
                        .invoiceNumber(invoice.getInvoiceNumber())
                        .invoiceDate(invoice.getInvoiceDate())
                        .dueDate(invoice.getDueDate())
                        .daysOverdue(days)
                        .supplierId(invoice.getSupplierId())
                        .supplierName(supplier == null ? null : supplier.getName())
                        .productId(line.getProductId())
                        .productName(product == null ? null : product.getName())
                        .productSku(product == null ? null : product.getSku())
                        .quantity(line.getQuantity())
                        .unitCost(line.getUnitCost())
                        .lineTotal(lineTotal)
                        .invoiceOutstanding(outstanding)
                        .build());
            }
            totalOutstanding = totalOutstanding.add(outstanding);
        }
        rows.sort(Comparator.comparing(ApDetailRowResponse::getDaysOverdue).reversed());

        return ApDetailResponse.builder().asOfDate(asOfDate).rows(rows).totalOutstanding(totalOutstanding).build();
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierBalanceResponse supplierBalance(ApReportFilterRequest filter) {
        LocalDate asOfDate = filter.getAsOfDate() != null ? filter.getAsOfDate() : LocalDate.now();

        Map<Long, SupplierBalanceRowResponse> bySupplier = new java.util.LinkedHashMap<>();
        for (PurchaseInvoice invoice : approvedInvoices(filter.getCompanyId())) {
            BigDecimal outstanding = outstandingAmount(invoice);
            if (outstanding.signum() <= 0) continue;

            SupplierBalanceRowResponse row = bySupplier.computeIfAbsent(invoice.getSupplierId(), id -> SupplierBalanceRowResponse.builder()
                    .supplierId(id)
                    .supplierName(supplierRepository.findById(id).map(Supplier::getName).orElse(null))
                    .invoiceCount(0)
                    .outstandingAmount(BigDecimal.ZERO)
                    .oldestDueDate(null)
                    .build());
            row.setInvoiceCount(row.getInvoiceCount() + 1);
            row.setOutstandingAmount(row.getOutstandingAmount().add(outstanding));
            if (invoice.getDueDate() != null && (row.getOldestDueDate() == null || invoice.getDueDate().isBefore(row.getOldestDueDate()))) {
                row.setOldestDueDate(invoice.getDueDate());
            }
        }

        List<SupplierBalanceRowResponse> rows = new ArrayList<>(bySupplier.values());
        rows.sort(Comparator.comparing(SupplierBalanceRowResponse::getOutstandingAmount).reversed());
        BigDecimal totalOutstanding = rows.stream().map(SupplierBalanceRowResponse::getOutstandingAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        return SupplierBalanceResponse.builder().asOfDate(asOfDate).rows(rows).totalOutstanding(totalOutstanding).build();
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierStatementResponse supplierStatement(ApReportFilterRequest filter) {
        if (filter.getSupplierId() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "supplierId is required");
        }
        Supplier supplier = supplierRepository.findById(filter.getSupplierId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Supplier not found with id: " + filter.getSupplierId()));

        record Entry(LocalDate date, String type, String reference, BigDecimal debit, BigDecimal credit) {
        }
        List<Entry> entries = new ArrayList<>();

        List<Specification<PurchaseInvoice>> invoiceConditions = new ArrayList<>();
        invoiceConditions.add((root, query, cb) -> cb.equal(root.get("status"), PurchaseInvoiceStatus.APPROVED));
        invoiceConditions.add((root, query, cb) -> cb.equal(root.get("supplierId"), filter.getSupplierId()));
        for (PurchaseInvoice invoice : purchaseInvoiceRepository.findAll(Specification.allOf(invoiceConditions))) {
            BigDecimal total = lineTotal(purchaseInvoiceLineRepository.findByPurchaseInvoiceId(invoice.getId()));
            entries.add(new Entry(invoice.getInvoiceDate(), "Invoice", invoice.getInvoiceNumber(), total, BigDecimal.ZERO));
        }

        List<Specification<SupplierPayment>> paymentConditions = new ArrayList<>();
        paymentConditions.add((root, query, cb) -> cb.equal(root.get("supplierId"), filter.getSupplierId()));
        paymentConditions.add((root, query, cb) -> cb.equal(root.get("type"), PaymentType.PAYMENT));
        for (SupplierPayment payment : supplierPaymentRepository.findAll(Specification.allOf(paymentConditions))) {
            entries.add(new Entry(payment.getPaymentDate(), "Payment", payment.getPaymentNumber(), BigDecimal.ZERO, payment.getAmount()));
        }

        List<Specification<PurchaseCreditNote>> creditNoteConditions = new ArrayList<>();
        creditNoteConditions.add((root, query, cb) -> cb.equal(root.get("supplierId"), filter.getSupplierId()));
        for (PurchaseCreditNote creditNote : purchaseCreditNoteRepository.findAll(Specification.allOf(creditNoteConditions))) {
            entries.add(new Entry(creditNote.getCreditNoteDate(), "Credit Note", creditNote.getCreditNoteNumber(), BigDecimal.ZERO, creditNote.getAmount()));
        }

        entries.sort(Comparator.comparing(Entry::date));

        BigDecimal openingBalance = BigDecimal.ZERO;
        List<SupplierStatementLineResponse> lines = new ArrayList<>();
        BigDecimal running = BigDecimal.ZERO;
        for (Entry entry : entries) {
            BigDecimal delta = entry.debit().subtract(entry.credit());
            if (filter.getDateFrom() != null && entry.date().isBefore(filter.getDateFrom())) {
                openingBalance = openingBalance.add(delta);
                continue;
            }
            if (filter.getDateTo() != null && entry.date().isAfter(filter.getDateTo())) {
                continue;
            }
            running = (lines.isEmpty() ? openingBalance : running).add(delta);
            lines.add(SupplierStatementLineResponse.builder()
                    .date(entry.date())
                    .type(entry.type())
                    .reference(entry.reference())
                    .debit(entry.debit())
                    .credit(entry.credit())
                    .runningBalance(running)
                    .build());
        }

        return SupplierStatementResponse.builder()
                .supplierId(supplier.getId())
                .supplierName(supplier.getName())
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .openingBalance(openingBalance)
                .lines(lines)
                .closingBalance(lines.isEmpty() ? openingBalance : lines.get(lines.size() - 1).getRunningBalance())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApPaymentResponse payments(ApReportFilterRequest filter) {
        List<Specification<SupplierPayment>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> cb.equal(root.get("type"), PaymentType.PAYMENT));
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getSupplierId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("supplierId"), filter.getSupplierId()));
        if (filter.getDateFrom() != null) conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("paymentDate"), filter.getDateFrom()));
        if (filter.getDateTo() != null) conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("paymentDate"), filter.getDateTo()));
        List<SupplierPayment> payments = supplierPaymentRepository.findAll(Specification.allOf(conditions));

        Map<Long, Supplier> suppliers = supplierRepository.findAllById(
                payments.stream().map(SupplierPayment::getSupplierId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Supplier::getId, s -> s));

        List<ApPaymentRowResponse> rows = new ArrayList<>();
        BigDecimal totalPaid = BigDecimal.ZERO;
        for (SupplierPayment payment : payments) {
            Supplier supplier = suppliers.get(payment.getSupplierId());
            rows.add(ApPaymentRowResponse.builder()
                    .paymentId(payment.getId())
                    .paymentNumber(payment.getPaymentNumber())
                    .paymentDate(payment.getPaymentDate())
                    .supplierId(payment.getSupplierId())
                    .supplierName(supplier == null ? null : supplier.getName())
                    .method(payment.getMethod() == null ? null : payment.getMethod().name())
                    .amount(payment.getAmount())
                    .build());
            totalPaid = totalPaid.add(payment.getAmount());
        }
        rows.sort(Comparator.comparing(ApPaymentRowResponse::getPaymentDate).reversed());

        return ApPaymentResponse.builder().dateFrom(filter.getDateFrom()).dateTo(filter.getDateTo()).rows(rows).totalPaid(totalPaid).build();
    }

    private List<PurchaseInvoice> approvedInvoices(Long companyId) {
        List<Specification<PurchaseInvoice>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> cb.equal(root.get("status"), PurchaseInvoiceStatus.APPROVED));
        if (companyId != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), companyId));
        return purchaseInvoiceRepository.findAll(Specification.allOf(conditions));
    }

    private BigDecimal outstandingAmount(PurchaseInvoice invoice) {
        BigDecimal total = lineTotal(purchaseInvoiceLineRepository.findByPurchaseInvoiceId(invoice.getId()));
        BigDecimal credited = purchaseCreditNoteRepository.findByPurchaseInvoiceId(invoice.getId()).stream()
                .map(PurchaseCreditNote::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paid = supplierPaymentAllocationRepository.findByPurchaseInvoiceId(invoice.getId()).stream()
                .map(SupplierPaymentAllocation::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.subtract(credited).subtract(paid);
    }

    private long daysOverdue(LocalDate dueDate, LocalDate asOfDate) {
        if (dueDate == null) return 0;
        long days = ChronoUnit.DAYS.between(dueDate, asOfDate);
        return Math.max(0, days);
    }

    private BigDecimal lineTotal(List<PurchaseInvoiceLine> lines) {
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseInvoiceLine line : lines) {
            total = total.add(lineTotal(line));
        }
        return total;
    }

    private BigDecimal lineTotal(PurchaseInvoiceLine line) {
        BigDecimal lineSubtotal = line.getQuantity().multiply(line.getUnitCost());
        BigDecimal discount = lineSubtotal.multiply(line.getDiscountPercent()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
        BigDecimal afterDiscount = lineSubtotal.subtract(discount);
        BigDecimal tax = afterDiscount.multiply(line.getTaxRate()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
        return afterDiscount.add(tax);
    }
}
