package com.example.erp.service.impl;

import com.example.erp.dto.BalanceAdjustmentRequest;
import com.example.erp.dto.CreateInvoiceRequest;
import com.example.erp.dto.InvoiceAgingFilterRequest;
import com.example.erp.dto.InvoiceAgingReportResponse;
import com.example.erp.dto.InvoiceAgingRowResponse;
import com.example.erp.dto.InvoiceFilterRequest;
import com.example.erp.dto.InvoiceLineResponse;
import com.example.erp.dto.InvoiceResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.SendDocumentEmailRequest;
import com.example.erp.entity.BalanceAdjustmentType;
import com.example.erp.entity.Company;
import com.example.erp.entity.CreditNote;
import com.example.erp.entity.Customer;
import com.example.erp.entity.Delivery;
import com.example.erp.entity.DeliveryLine;
import com.example.erp.entity.DeliveryStatus;
import com.example.erp.entity.Invoice;
import com.example.erp.entity.InvoiceLine;
import com.example.erp.entity.InvoiceStatus;
import com.example.erp.entity.Product;
import com.example.erp.entity.SalesOrder;
import com.example.erp.entity.SalesOrderLine;
import com.example.erp.entity.SalesOrderStatus;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.CreditNoteRepository;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.DeliveryLineRepository;
import com.example.erp.repository.DeliveryRepository;
import com.example.erp.entity.PaymentAllocation;
import com.example.erp.repository.InvoiceLineRepository;
import com.example.erp.repository.InvoiceRepository;
import com.example.erp.repository.PaymentAllocationRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.SalesOrderLineRepository;
import com.example.erp.repository.SalesOrderRepository;
import com.example.erp.service.AutoPostingService;
import com.example.erp.service.CustomerService;
import com.example.erp.service.EmailService;
import com.example.erp.service.InvoiceService;
import com.example.erp.service.PdfRenderService;
import com.example.erp.util.DocumentPdfHtml;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryLineRepository deliveryLineRepository;
    private final ProductRepository productRepository;
    private final CreditNoteRepository creditNoteRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final AutoPostingService autoPostingService;
    private final PdfRenderService pdfRenderService;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse> listInvoices(InvoiceFilterRequest filter) {
        List<Specification<Invoice>> conditions = new ArrayList<>();
        if (filter.getInvoiceNumber() != null && !filter.getInvoiceNumber().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("invoiceNumber")), "%" + filter.getInvoiceNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getCustomerId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("customerId"), filter.getCustomerId()));
        }
        if (filter.getSalesOrderId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("salesOrderId"), filter.getSalesOrderId()));
        }
        if (filter.getDeliveryId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("deliveryId"), filter.getDeliveryId()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        Specification<Invoice> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<Invoice> page = invoiceRepository.findAll(spec, pageable);
        List<Invoice> content = page.getContent();
        List<Long> invoiceIds = content.stream().map(Invoice::getId).toList();
        Map<Long, List<InvoiceLine>> linesByInvoiceId = invoiceIds.isEmpty() ? Map.of() : allLinesGroupedByInvoice(invoiceIds);

        return PageResponse.of(page.map(inv -> toResponse(inv, linesByInvoiceId.getOrDefault(inv.getId(), List.of()))));
    }

    private Map<Long, List<InvoiceLine>> allLinesGroupedByInvoice(List<Long> invoiceIds) {
        return invoiceLineRepository.findByInvoiceIdIn(invoiceIds).stream()
                .collect(Collectors.groupingBy(InvoiceLine::getInvoiceId));
    }

    @Override
    public InvoiceResponse getInvoice(Long id) {
        Invoice invoice = find(id);
        return toResponse(invoice, invoiceLineRepository.findByInvoiceId(id));
    }

    @Override
    @Transactional
    public InvoiceResponse createFromSalesOrder(Long salesOrderId, CreateInvoiceRequest request, String actingUsername) {
        SalesOrder so = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Sales order not found with id: " + salesOrderId));
        if (so.getStatus() != SalesOrderStatus.CONFIRMED && so.getStatus() != SalesOrderStatus.PARTIALLY_DELIVERED
                && so.getStatus() != SalesOrderStatus.DELIVERED) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Sales order must be confirmed before it can be invoiced (current status: " + so.getStatus() + ")");
        }
        List<SalesOrderLine> soLines = salesOrderLineRepository.findBySalesOrderId(so.getId());
        if (soLines.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Sales order has no lines to invoice");
        }
        requireForeignCurrencyPair(request.getForeignCurrency(), request.getExchangeRate());

        Invoice invoice = Invoice.builder()
                .companyId(so.getCompanyId())
                .customerId(so.getCustomerId())
                .salesOrderId(so.getId())
                .invoiceDate(request.getInvoiceDate())
                .dueDate(request.getDueDate())
                .notes(request.getNotes())
                .createdBy(actingUsername)
                .foreignCurrency(request.getForeignCurrency())
                .exchangeRate(request.getExchangeRate())
                .build();
        invoiceRepository.save(invoice);
        invoice.setInvoiceNumber("INV-" + String.format("%06d", invoice.getId()));
        invoiceRepository.save(invoice);

        List<InvoiceLine> lines = soLines.stream()
                .map(l -> InvoiceLine.builder()
                        .invoiceId(invoice.getId())
                        .productId(l.getProductId())
                        .quantity(l.getQuantityOrdered())
                        .unitPrice(l.getUnitPrice())
                        .discountPercent(l.getDiscountPercent())
                        .taxRate(l.getTaxRate())
                        .build())
                .toList();
        lines = invoiceLineRepository.saveAll(lines);

        return toResponse(invoice, lines);
    }

    @Override
    @Transactional
    public InvoiceResponse createFromDelivery(Long deliveryId, CreateInvoiceRequest request, String actingUsername) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Delivery not found with id: " + deliveryId));
        if (delivery.getStatus() != DeliveryStatus.SHIPPED && delivery.getStatus() != DeliveryStatus.DELIVERED) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Delivery must be shipped before it can be invoiced (current status: " + delivery.getStatus() + ")");
        }
        SalesOrder so = salesOrderRepository.findById(delivery.getSalesOrderId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Sales order not found with id: " + delivery.getSalesOrderId()));
        List<DeliveryLine> deliveryLines = deliveryLineRepository.findByDeliveryId(delivery.getId());
        if (deliveryLines.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Delivery has no lines to invoice");
        }
        Map<Long, SalesOrderLine> soLinesById = salesOrderLineRepository.findBySalesOrderId(so.getId()).stream()
                .collect(Collectors.toMap(SalesOrderLine::getId, l -> l));
        requireForeignCurrencyPair(request.getForeignCurrency(), request.getExchangeRate());

        Invoice invoice = Invoice.builder()
                .companyId(so.getCompanyId())
                .customerId(so.getCustomerId())
                .salesOrderId(so.getId())
                .deliveryId(delivery.getId())
                .invoiceDate(request.getInvoiceDate())
                .dueDate(request.getDueDate())
                .notes(request.getNotes())
                .createdBy(actingUsername)
                .foreignCurrency(request.getForeignCurrency())
                .exchangeRate(request.getExchangeRate())
                .build();
        invoiceRepository.save(invoice);
        invoice.setInvoiceNumber("INV-" + String.format("%06d", invoice.getId()));
        invoiceRepository.save(invoice);

        List<InvoiceLine> lines = deliveryLines.stream()
                .map(dl -> {
                    SalesOrderLine soLine = soLinesById.get(dl.getSalesOrderLineId());
                    return InvoiceLine.builder()
                            .invoiceId(invoice.getId())
                            .productId(dl.getProductId())
                            .quantity(dl.getQuantityDelivered())
                            .unitPrice(soLine == null ? BigDecimal.ZERO : soLine.getUnitPrice())
                            .discountPercent(soLine == null ? BigDecimal.ZERO : soLine.getDiscountPercent())
                            .taxRate(soLine == null ? BigDecimal.ZERO : soLine.getTaxRate())
                            .build();
                })
                .toList();
        lines = invoiceLineRepository.saveAll(lines);

        return toResponse(invoice, lines);
    }

    @Override
    @Transactional
    public InvoiceResponse approveInvoice(Long id, String actingUsername) {
        Invoice invoice = find(id);
        if (!invoice.getStatus().canTransitionTo(InvoiceStatus.APPROVED)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft invoices can be approved");
        }
        List<InvoiceLine> lines = invoiceLineRepository.findByInvoiceId(id);
        BigDecimal[] totals = computeTotals(lines);
        BigDecimal netAmount = totals[0].subtract(totals[1]);
        BigDecimal taxAmount = totals[2];
        BigDecimal totalAmount = totals[3];

        invoice.setStatus(InvoiceStatus.APPROVED);
        invoiceRepository.save(invoice);

        BalanceAdjustmentRequest chargeRequest = new BalanceAdjustmentRequest();
        chargeRequest.setType(BalanceAdjustmentType.CHARGE);
        chargeRequest.setAmount(totalAmount);
        chargeRequest.setNote("Invoice " + invoice.getInvoiceNumber());
        customerService.adjustBalance(invoice.getCustomerId(), chargeRequest, actingUsername);

        autoPostingService.postInvoiceApproval(invoice, netAmount, taxAmount, totalAmount, actingUsername);

        return toResponse(invoice, lines);
    }

    @Override
    @Transactional
    public InvoiceResponse cancelInvoice(Long id, String actingUsername) {
        Invoice invoice = find(id);
        if (!invoice.getStatus().canTransitionTo(InvoiceStatus.CANCELLED)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft or approved invoices can be cancelled");
        }
        List<InvoiceLine> lines = invoiceLineRepository.findByInvoiceId(id);

        if (invoice.getStatus() == InvoiceStatus.APPROVED) {
            BigDecimal totalAmount = computeTotals(lines)[3];
            BalanceAdjustmentRequest reverseRequest = new BalanceAdjustmentRequest();
            reverseRequest.setType(BalanceAdjustmentType.PAYMENT);
            reverseRequest.setAmount(totalAmount);
            reverseRequest.setNote("Reversal of cancelled invoice " + invoice.getInvoiceNumber());
            customerService.adjustBalance(invoice.getCustomerId(), reverseRequest, actingUsername);
            autoPostingService.reverseAutoEntry("INVOICE", invoice.getId(), actingUsername);
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoiceRepository.save(invoice);
        return toResponse(invoice, lines);
    }

    @Override
    @Transactional
    public void deleteInvoice(Long id) {
        Invoice invoice = find(id);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft invoices can be deleted");
        }
        invoiceLineRepository.deleteByInvoiceId(id);
        invoiceRepository.deleteById(id);
    }

    @Override
    public void emailInvoice(Long id, SendDocumentEmailRequest request) {
        InvoiceResponse invoice = getInvoice(id);
        Customer customer = customerRepository.findById(invoice.getCustomerId()).orElse(null);
        String to = request.getTo() != null && !request.getTo().isBlank() ? request.getTo()
                : customer != null ? customer.getEmail() : null;
        if (to == null || to.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Customer has no email on file — provide one to send to");
        }
        Company company = companyRepository.findById(invoice.getCompanyId()).orElse(null);
        String html = buildInvoiceHtml(company, invoice);
        byte[] pdf = pdfRenderService.renderHtmlToPdf(html);

        String subject = request.getSubject() != null && !request.getSubject().isBlank()
                ? request.getSubject() : "Invoice " + invoice.getInvoiceNumber();
        String body = request.getMessage() != null && !request.getMessage().isBlank()
                ? request.getMessage() : "Please find attached invoice " + invoice.getInvoiceNumber() + ".";
        emailService.sendWithAttachment(to, subject, body, pdf, invoice.getInvoiceNumber() + ".pdf", "application/pdf");
    }

    private String buildInvoiceHtml(Company company, InvoiceResponse invoice) {
        String metaHtml = "Date: " + invoice.getInvoiceDate()
                + (invoice.getDueDate() != null ? "<br/>Due: " + invoice.getDueDate() : "");

        StringBuilder table = new StringBuilder();
        table.append("<table><thead><tr><th>Product</th><th>Qty</th><th class=\"num\">Unit price</th>")
                .append("<th class=\"num\">Tax</th><th class=\"num\">Line total</th></tr></thead><tbody>");
        for (InvoiceLineResponse line : invoice.getLines()) {
            table.append("<tr><td>").append(DocumentPdfHtml.escape(line.getProductName())).append("</td>")
                    .append("<td>").append(line.getQuantity()).append("</td>")
                    .append("<td class=\"num\">").append(line.getUnitPrice()).append("</td>")
                    .append("<td class=\"num\">").append(line.getTaxAmount()).append("</td>")
                    .append("<td class=\"num\">").append(line.getLineTotal()).append("</td></tr>");
        }
        table.append("</tbody></table>");

        StringBuilder totals = new StringBuilder("<div class=\"totals\">");
        totals.append("<div><span>Subtotal</span><span>").append(invoice.getSubtotal()).append("</span></div>");
        if (invoice.getDiscountAmount() != null && invoice.getDiscountAmount().signum() > 0) {
            totals.append("<div><span>Discount</span><span>-").append(invoice.getDiscountAmount()).append("</span></div>");
        }
        if (invoice.getTaxAmount() != null && invoice.getTaxAmount().signum() > 0) {
            totals.append("<div><span>Tax</span><span>").append(invoice.getTaxAmount()).append("</span></div>");
        }
        totals.append("<div class=\"grand\"><span>Total</span><span>").append(invoice.getTotalAmount()).append("</span></div>");
        totals.append("</div>");

        String partyHtml = DocumentPdfHtml.escape(invoice.getCustomerName());

        return DocumentPdfHtml.render(company, "INVOICE", invoice.getInvoiceNumber(), metaHtml,
                "Bill to", partyHtml, table.toString(), totals.toString());
    }

    // Returns [subtotal, discountAmount, taxAmount, totalAmount].
    private BigDecimal[] computeTotals(List<InvoiceLine> lines) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        for (InvoiceLine line : lines) {
            BigDecimal lineSubtotal = line.getQuantity().multiply(line.getUnitPrice());
            BigDecimal lineDiscount = lineSubtotal.multiply(line.getDiscountPercent()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
            BigDecimal afterDiscount = lineSubtotal.subtract(lineDiscount);
            BigDecimal lineTax = afterDiscount.multiply(line.getTaxRate()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
            subtotal = subtotal.add(lineSubtotal);
            discountAmount = discountAmount.add(lineDiscount);
            taxAmount = taxAmount.add(lineTax);
        }
        BigDecimal totalAmount = subtotal.subtract(discountAmount).add(taxAmount);
        return new BigDecimal[]{subtotal, discountAmount, taxAmount, totalAmount};
    }

    private Invoice find(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Invoice not found with id: " + id));
    }

    private void requireForeignCurrencyPair(String foreignCurrency, BigDecimal exchangeRate) {
        boolean hasCurrency = foreignCurrency != null && !foreignCurrency.isBlank();
        boolean hasRate = exchangeRate != null;
        if (hasCurrency != hasRate) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Both foreign currency and exchange rate are required together");
        }
    }

    private InvoiceResponse toResponse(Invoice invoice, List<InvoiceLine> lines) {
        String companyName = companyRepository.findById(invoice.getCompanyId()).map(Company::getName).orElse(null);
        String customerName = customerRepository.findById(invoice.getCustomerId()).map(Customer::getName).orElse(null);
        String soNumber = invoice.getSalesOrderId() == null ? null
                : salesOrderRepository.findById(invoice.getSalesOrderId()).map(SalesOrder::getSoNumber).orElse(null);
        String deliveryNumber = invoice.getDeliveryId() == null ? null
                : deliveryRepository.findById(invoice.getDeliveryId()).map(Delivery::getDeliveryNumber).orElse(null);

        Map<Long, Product> products = lines.isEmpty() ? Map.of() : productRepository.findAllById(
                lines.stream().map(InvoiceLine::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<InvoiceLineResponse> lineResponses = lines.stream()
                .map(line -> {
                    Product product = products.get(line.getProductId());
                    BigDecimal lineSubtotal = line.getQuantity().multiply(line.getUnitPrice());
                    BigDecimal discountAmount = lineSubtotal.multiply(line.getDiscountPercent()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
                    BigDecimal afterDiscount = lineSubtotal.subtract(discountAmount);
                    BigDecimal taxAmount = afterDiscount.multiply(line.getTaxRate()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
                    return InvoiceLineResponse.builder()
                            .id(line.getId())
                            .productId(line.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productSku(product == null ? null : product.getSku())
                            .quantity(line.getQuantity())
                            .unitPrice(line.getUnitPrice())
                            .discountPercent(line.getDiscountPercent())
                            .discountAmount(discountAmount)
                            .taxRate(line.getTaxRate())
                            .taxAmount(taxAmount)
                            .lineTotal(afterDiscount.add(taxAmount))
                            .build();
                })
                .toList();

        BigDecimal[] totals = computeTotals(lines);
        BigDecimal creditedAmount = creditNoteRepository.findByInvoiceId(invoice.getId()).stream()
                .map(CreditNote::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // Net of payments minus any refunds against them — see
        // PaymentAllocation's own signed-amount comment.
        BigDecimal paidAmount = paymentAllocationRepository.findByInvoiceId(invoice.getId()).stream()
                .map(PaymentAllocation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal outstandingAmount = totals[3].subtract(creditedAmount).subtract(paidAmount);
        String paymentStatus = outstandingAmount.compareTo(BigDecimal.ZERO) <= 0
                ? (totals[3].compareTo(BigDecimal.ZERO) == 0 ? "UNPAID" : "PAID")
                : (paidAmount.compareTo(BigDecimal.ZERO) > 0 ? "PARTIALLY_PAID" : "UNPAID");
        int daysOverdue = daysOverdue(invoice.getDueDate(), outstandingAmount, LocalDate.now());
        BigDecimal foreignTotalAmount = invoice.getExchangeRate() != null
                ? totals[3].divide(invoice.getExchangeRate(), 4, RoundingMode.HALF_UP)
                : null;

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .companyId(invoice.getCompanyId())
                .companyName(companyName)
                .customerId(invoice.getCustomerId())
                .customerName(customerName)
                .salesOrderId(invoice.getSalesOrderId())
                .soNumber(soNumber)
                .deliveryId(invoice.getDeliveryId())
                .deliveryNumber(deliveryNumber)
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceDate(invoice.getInvoiceDate())
                .dueDate(invoice.getDueDate())
                .status(invoice.getStatus().name())
                .notes(invoice.getNotes())
                .createdBy(invoice.getCreatedBy())
                .subtotal(totals[0])
                .discountAmount(totals[1])
                .taxAmount(totals[2])
                .totalAmount(totals[3])
                .foreignCurrency(invoice.getForeignCurrency())
                .exchangeRate(invoice.getExchangeRate())
                .foreignTotalAmount(foreignTotalAmount)
                .creditedAmount(creditedAmount)
                .paidAmount(paidAmount)
                .outstandingAmount(outstandingAmount)
                .paymentStatus(paymentStatus)
                .overdue(daysOverdue > 0)
                .daysOverdue(daysOverdue)
                .lines(lineResponses)
                .build();
    }

    // 0 when there's nothing left to collect, no due date is set, or the due
    // date hasn't passed yet as of `asOf`.
    private int daysOverdue(LocalDate dueDate, BigDecimal outstandingAmount, LocalDate asOf) {
        if (dueDate == null || outstandingAmount.compareTo(BigDecimal.ZERO) <= 0) return 0;
        long days = ChronoUnit.DAYS.between(dueDate, asOf);
        return days > 0 ? (int) days : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceAgingReportResponse agingReport(InvoiceAgingFilterRequest filter) {
        LocalDate asOfDate = filter.getAsOfDate() != null ? filter.getAsOfDate() : LocalDate.now();

        List<Specification<Invoice>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> cb.equal(root.get("status"), InvoiceStatus.APPROVED));
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getCustomerId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("customerId"), filter.getCustomerId()));
        }
        List<Invoice> invoices = invoiceRepository.findAll(Specification.allOf(conditions));
        List<Long> invoiceIds = invoices.stream().map(Invoice::getId).toList();
        Map<Long, List<InvoiceLine>> linesByInvoiceId = invoiceIds.isEmpty() ? Map.of() : allLinesGroupedByInvoice(invoiceIds);
        Map<Long, List<CreditNote>> creditNotesByInvoiceId = invoiceIds.isEmpty() ? Map.of()
                : creditNoteRepository.findByInvoiceIdIn(invoiceIds).stream().collect(Collectors.groupingBy(CreditNote::getInvoiceId));
        Map<Long, List<PaymentAllocation>> allocationsByInvoiceId = invoiceIds.isEmpty() ? Map.of()
                : paymentAllocationRepository.findByInvoiceIdIn(invoiceIds).stream().collect(Collectors.groupingBy(PaymentAllocation::getInvoiceId));

        Map<Long, InvoiceAgingRowResponse> rowsByCustomer = new java.util.LinkedHashMap<>();
        InvoiceAgingRowResponse grandTotal = InvoiceAgingRowResponse.builder()
                .customerId(null).customerName("Total")
                .current(BigDecimal.ZERO).days1To30(BigDecimal.ZERO).days31To60(BigDecimal.ZERO)
                .days61To90(BigDecimal.ZERO).days90Plus(BigDecimal.ZERO).total(BigDecimal.ZERO)
                .build();

        for (Invoice invoice : invoices) {
            List<InvoiceLine> lines = linesByInvoiceId.getOrDefault(invoice.getId(), List.of());
            BigDecimal[] totals = computeTotals(lines);
            BigDecimal credited = creditNotesByInvoiceId.getOrDefault(invoice.getId(), List.of()).stream()
                    .map(CreditNote::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal paid = allocationsByInvoiceId.getOrDefault(invoice.getId(), List.of()).stream()
                    .map(PaymentAllocation::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal outstanding = totals[3].subtract(credited).subtract(paid);
            if (outstanding.compareTo(BigDecimal.ZERO) <= 0) continue;

            long days = invoice.getDueDate() == null ? 0 : Math.max(0, ChronoUnit.DAYS.between(invoice.getDueDate(), asOfDate));

            InvoiceAgingRowResponse row = rowsByCustomer.computeIfAbsent(invoice.getCustomerId(), id -> InvoiceAgingRowResponse.builder()
                    .customerId(id)
                    .customerName(customerRepository.findById(id).map(Customer::getName).orElse(null))
                    .current(BigDecimal.ZERO).days1To30(BigDecimal.ZERO).days31To60(BigDecimal.ZERO)
                    .days61To90(BigDecimal.ZERO).days90Plus(BigDecimal.ZERO).total(BigDecimal.ZERO)
                    .build());

            if (days <= 0) row.setCurrent(row.getCurrent().add(outstanding));
            else if (days <= 30) row.setDays1To30(row.getDays1To30().add(outstanding));
            else if (days <= 60) row.setDays31To60(row.getDays31To60().add(outstanding));
            else if (days <= 90) row.setDays61To90(row.getDays61To90().add(outstanding));
            else row.setDays90Plus(row.getDays90Plus().add(outstanding));
            row.setTotal(row.getTotal().add(outstanding));

            if (days <= 0) grandTotal.setCurrent(grandTotal.getCurrent().add(outstanding));
            else if (days <= 30) grandTotal.setDays1To30(grandTotal.getDays1To30().add(outstanding));
            else if (days <= 60) grandTotal.setDays31To60(grandTotal.getDays31To60().add(outstanding));
            else if (days <= 90) grandTotal.setDays61To90(grandTotal.getDays61To90().add(outstanding));
            else grandTotal.setDays90Plus(grandTotal.getDays90Plus().add(outstanding));
            grandTotal.setTotal(grandTotal.getTotal().add(outstanding));
        }

        return InvoiceAgingReportResponse.builder()
                .asOfDate(asOfDate)
                .rows(new ArrayList<>(rowsByCustomer.values()))
                .totals(grandTotal)
                .build();
    }
}
