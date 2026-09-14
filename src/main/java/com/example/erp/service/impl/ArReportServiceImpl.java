package com.example.erp.service.impl;

import com.example.erp.dto.ArBadDebtResponse;
import com.example.erp.dto.ArBadDebtRowResponse;
import com.example.erp.dto.ArCollectionResponse;
import com.example.erp.dto.ArCollectionRowResponse;
import com.example.erp.dto.ArDetailResponse;
import com.example.erp.dto.ArDetailRowResponse;
import com.example.erp.dto.ArReportFilterRequest;
import com.example.erp.dto.ArSummaryResponse;
import com.example.erp.dto.CustomerBalanceResponse;
import com.example.erp.dto.CustomerBalanceRowResponse;
import com.example.erp.dto.CustomerStatementLineResponse;
import com.example.erp.dto.CustomerStatementResponse;
import com.example.erp.entity.CreditNote;
import com.example.erp.entity.Customer;
import com.example.erp.entity.Invoice;
import com.example.erp.entity.InvoiceLine;
import com.example.erp.entity.InvoiceStatus;
import com.example.erp.entity.Payment;
import com.example.erp.entity.PaymentAllocation;
import com.example.erp.entity.PaymentType;
import com.example.erp.entity.Product;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CreditNoteRepository;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.InvoiceLineRepository;
import com.example.erp.repository.InvoiceRepository;
import com.example.erp.repository.PaymentAllocationRepository;
import com.example.erp.repository.PaymentRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.service.ArReportService;
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
public class ArReportServiceImpl implements ArReportService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int DEFAULT_BAD_DEBT_THRESHOLD_DAYS = 90;

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final CreditNoteRepository creditNoteRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public ArSummaryResponse summary(ArReportFilterRequest filter) {
        LocalDate asOfDate = filter.getAsOfDate() != null ? filter.getAsOfDate() : LocalDate.now();

        BigDecimal totalOutstanding = BigDecimal.ZERO;
        BigDecimal totalOverdue = BigDecimal.ZERO;
        long invoiceCount = 0;
        long overdueInvoiceCount = 0;
        long oldestOverdueDays = 0;
        Set<Long> customers = new HashSet<>();
        Set<Long> overdueCustomers = new HashSet<>();

        for (Invoice invoice : approvedInvoices(filter.getCompanyId())) {
            BigDecimal outstanding = outstandingAmount(invoice);
            if (outstanding.signum() <= 0) continue;

            long days = daysOverdue(invoice.getDueDate(), asOfDate);
            invoiceCount++;
            totalOutstanding = totalOutstanding.add(outstanding);
            customers.add(invoice.getCustomerId());
            if (days > 0) {
                overdueInvoiceCount++;
                totalOverdue = totalOverdue.add(outstanding);
                overdueCustomers.add(invoice.getCustomerId());
                oldestOverdueDays = Math.max(oldestOverdueDays, days);
            }
        }

        return ArSummaryResponse.builder()
                .asOfDate(asOfDate)
                .totalOutstanding(totalOutstanding)
                .totalOverdue(totalOverdue)
                .invoiceCount(invoiceCount)
                .customerCount(customers.size())
                .overdueInvoiceCount(overdueInvoiceCount)
                .overdueCustomerCount(overdueCustomers.size())
                .oldestOverdueDays(oldestOverdueDays)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ArDetailResponse detail(ArReportFilterRequest filter) {
        LocalDate asOfDate = filter.getAsOfDate() != null ? filter.getAsOfDate() : LocalDate.now();

        List<ArDetailRowResponse> rows = new ArrayList<>();
        BigDecimal totalOutstanding = BigDecimal.ZERO;
        for (Invoice invoice : approvedInvoices(filter.getCompanyId())) {
            if (filter.getCustomerId() != null && !filter.getCustomerId().equals(invoice.getCustomerId())) continue;
            BigDecimal outstanding = outstandingAmount(invoice);
            if (outstanding.signum() <= 0) continue;

            Customer customer = customerRepository.findById(invoice.getCustomerId()).orElse(null);
            long days = daysOverdue(invoice.getDueDate(), asOfDate);
            for (InvoiceLine line : invoiceLineRepository.findByInvoiceId(invoice.getId())) {
                Product product = productRepository.findById(line.getProductId()).orElse(null);
                BigDecimal lineTotal = lineTotal(line);
                rows.add(ArDetailRowResponse.builder()
                        .invoiceId(invoice.getId())
                        .invoiceNumber(invoice.getInvoiceNumber())
                        .invoiceDate(invoice.getInvoiceDate())
                        .dueDate(invoice.getDueDate())
                        .daysOverdue(days)
                        .customerId(invoice.getCustomerId())
                        .customerName(customer == null ? null : customer.getName())
                        .productId(line.getProductId())
                        .productName(product == null ? null : product.getName())
                        .productSku(product == null ? null : product.getSku())
                        .quantity(line.getQuantity())
                        .unitPrice(line.getUnitPrice())
                        .lineTotal(lineTotal)
                        .invoiceOutstanding(outstanding)
                        .build());
            }
            totalOutstanding = totalOutstanding.add(outstanding);
        }
        rows.sort(Comparator.comparing(ArDetailRowResponse::getDaysOverdue).reversed());

        return ArDetailResponse.builder().asOfDate(asOfDate).rows(rows).totalOutstanding(totalOutstanding).build();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerBalanceResponse customerBalance(ArReportFilterRequest filter) {
        LocalDate asOfDate = filter.getAsOfDate() != null ? filter.getAsOfDate() : LocalDate.now();

        Map<Long, CustomerBalanceRowResponse> byCustomer = new java.util.LinkedHashMap<>();
        for (Invoice invoice : approvedInvoices(filter.getCompanyId())) {
            BigDecimal outstanding = outstandingAmount(invoice);
            if (outstanding.signum() <= 0) continue;

            CustomerBalanceRowResponse row = byCustomer.computeIfAbsent(invoice.getCustomerId(), id -> CustomerBalanceRowResponse.builder()
                    .customerId(id)
                    .customerName(customerRepository.findById(id).map(Customer::getName).orElse(null))
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

        List<CustomerBalanceRowResponse> rows = new ArrayList<>(byCustomer.values());
        rows.sort(Comparator.comparing(CustomerBalanceRowResponse::getOutstandingAmount).reversed());
        BigDecimal totalOutstanding = rows.stream().map(CustomerBalanceRowResponse::getOutstandingAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        return CustomerBalanceResponse.builder().asOfDate(asOfDate).rows(rows).totalOutstanding(totalOutstanding).build();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerStatementResponse customerStatement(ArReportFilterRequest filter) {
        if (filter.getCustomerId() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "customerId is required");
        }
        Customer customer = customerRepository.findById(filter.getCustomerId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Customer not found with id: " + filter.getCustomerId()));

        record Entry(LocalDate date, String type, String reference, BigDecimal debit, BigDecimal credit) {
        }
        List<Entry> entries = new ArrayList<>();

        List<Specification<Invoice>> invoiceConditions = new ArrayList<>();
        invoiceConditions.add((root, query, cb) -> cb.equal(root.get("status"), InvoiceStatus.APPROVED));
        invoiceConditions.add((root, query, cb) -> cb.equal(root.get("customerId"), filter.getCustomerId()));
        for (Invoice invoice : invoiceRepository.findAll(Specification.allOf(invoiceConditions))) {
            BigDecimal total = lineTotal(invoiceLineRepository.findByInvoiceId(invoice.getId()));
            entries.add(new Entry(invoice.getInvoiceDate(), "Invoice", invoice.getInvoiceNumber(), total, BigDecimal.ZERO));
        }

        List<Specification<Payment>> paymentConditions = new ArrayList<>();
        paymentConditions.add((root, query, cb) -> cb.equal(root.get("customerId"), filter.getCustomerId()));
        paymentConditions.add((root, query, cb) -> cb.equal(root.get("type"), PaymentType.PAYMENT));
        for (Payment payment : paymentRepository.findAll(Specification.allOf(paymentConditions))) {
            entries.add(new Entry(payment.getPaymentDate(), "Payment", payment.getPaymentNumber(), BigDecimal.ZERO, payment.getAmount()));
        }

        List<Specification<CreditNote>> creditNoteConditions = new ArrayList<>();
        creditNoteConditions.add((root, query, cb) -> cb.equal(root.get("customerId"), filter.getCustomerId()));
        for (CreditNote creditNote : creditNoteRepository.findAll(Specification.allOf(creditNoteConditions))) {
            entries.add(new Entry(creditNote.getCreditNoteDate(), "Credit Note", creditNote.getCreditNoteNumber(), BigDecimal.ZERO, creditNote.getAmount()));
        }

        entries.sort(Comparator.comparing(Entry::date));

        BigDecimal openingBalance = BigDecimal.ZERO;
        List<CustomerStatementLineResponse> lines = new ArrayList<>();
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
            lines.add(CustomerStatementLineResponse.builder()
                    .date(entry.date())
                    .type(entry.type())
                    .reference(entry.reference())
                    .debit(entry.debit())
                    .credit(entry.credit())
                    .runningBalance(running)
                    .build());
        }

        return CustomerStatementResponse.builder()
                .customerId(customer.getId())
                .customerName(customer.getName())
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .openingBalance(openingBalance)
                .lines(lines)
                .closingBalance(lines.isEmpty() ? openingBalance : lines.get(lines.size() - 1).getRunningBalance())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ArCollectionResponse collections(ArReportFilterRequest filter) {
        List<Specification<Payment>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> cb.equal(root.get("type"), PaymentType.PAYMENT));
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getCustomerId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("customerId"), filter.getCustomerId()));
        if (filter.getDateFrom() != null) conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("paymentDate"), filter.getDateFrom()));
        if (filter.getDateTo() != null) conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("paymentDate"), filter.getDateTo()));
        List<Payment> payments = paymentRepository.findAll(Specification.allOf(conditions));

        Map<Long, Customer> customers = customerRepository.findAllById(
                payments.stream().map(Payment::getCustomerId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Customer::getId, c -> c));

        List<ArCollectionRowResponse> rows = new ArrayList<>();
        BigDecimal totalCollected = BigDecimal.ZERO;
        for (Payment payment : payments) {
            Customer customer = customers.get(payment.getCustomerId());
            rows.add(ArCollectionRowResponse.builder()
                    .paymentId(payment.getId())
                    .paymentNumber(payment.getPaymentNumber())
                    .paymentDate(payment.getPaymentDate())
                    .customerId(payment.getCustomerId())
                    .customerName(customer == null ? null : customer.getName())
                    .method(payment.getMethod() == null ? null : payment.getMethod().name())
                    .amount(payment.getAmount())
                    .build());
            totalCollected = totalCollected.add(payment.getAmount());
        }
        rows.sort(Comparator.comparing(ArCollectionRowResponse::getPaymentDate).reversed());

        return ArCollectionResponse.builder().dateFrom(filter.getDateFrom()).dateTo(filter.getDateTo()).rows(rows).totalCollected(totalCollected).build();
    }

    @Override
    @Transactional(readOnly = true)
    public ArBadDebtResponse badDebt(ArReportFilterRequest filter) {
        LocalDate asOfDate = filter.getAsOfDate() != null ? filter.getAsOfDate() : LocalDate.now();
        int thresholdDays = filter.getThresholdDays() != null ? filter.getThresholdDays() : DEFAULT_BAD_DEBT_THRESHOLD_DAYS;

        List<ArBadDebtRowResponse> rows = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Invoice invoice : approvedInvoices(filter.getCompanyId())) {
            BigDecimal outstanding = outstandingAmount(invoice);
            if (outstanding.signum() <= 0) continue;
            long days = daysOverdue(invoice.getDueDate(), asOfDate);
            if (days < thresholdDays) continue;

            Customer customer = customerRepository.findById(invoice.getCustomerId()).orElse(null);
            rows.add(ArBadDebtRowResponse.builder()
                    .invoiceId(invoice.getId())
                    .invoiceNumber(invoice.getInvoiceNumber())
                    .invoiceDate(invoice.getInvoiceDate())
                    .dueDate(invoice.getDueDate())
                    .daysOverdue(days)
                    .customerId(invoice.getCustomerId())
                    .customerName(customer == null ? null : customer.getName())
                    .outstandingAmount(outstanding)
                    .build());
            totalAmount = totalAmount.add(outstanding);
        }
        rows.sort(Comparator.comparing(ArBadDebtRowResponse::getDaysOverdue).reversed());

        return ArBadDebtResponse.builder()
                .asOfDate(asOfDate)
                .thresholdDays(thresholdDays)
                .rows(rows)
                .invoiceCount(rows.size())
                .totalAmount(totalAmount)
                .build();
    }

    private List<Invoice> approvedInvoices(Long companyId) {
        List<Specification<Invoice>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> cb.equal(root.get("status"), InvoiceStatus.APPROVED));
        if (companyId != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), companyId));
        return invoiceRepository.findAll(Specification.allOf(conditions));
    }

    private BigDecimal outstandingAmount(Invoice invoice) {
        BigDecimal total = lineTotal(invoiceLineRepository.findByInvoiceId(invoice.getId()));
        BigDecimal credited = creditNoteRepository.findByInvoiceId(invoice.getId()).stream()
                .map(CreditNote::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paid = paymentAllocationRepository.findByInvoiceId(invoice.getId()).stream()
                .map(PaymentAllocation::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.subtract(credited).subtract(paid);
    }

    private long daysOverdue(LocalDate dueDate, LocalDate asOfDate) {
        if (dueDate == null) return 0;
        long days = ChronoUnit.DAYS.between(dueDate, asOfDate);
        return Math.max(0, days);
    }

    private BigDecimal lineTotal(List<InvoiceLine> lines) {
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceLine line : lines) {
            total = total.add(lineTotal(line));
        }
        return total;
    }

    private BigDecimal lineTotal(InvoiceLine line) {
        BigDecimal lineSubtotal = line.getQuantity().multiply(line.getUnitPrice());
        BigDecimal discount = lineSubtotal.multiply(line.getDiscountPercent()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
        BigDecimal afterDiscount = lineSubtotal.subtract(discount);
        BigDecimal tax = afterDiscount.multiply(line.getTaxRate()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
        return afterDiscount.add(tax);
    }
}
