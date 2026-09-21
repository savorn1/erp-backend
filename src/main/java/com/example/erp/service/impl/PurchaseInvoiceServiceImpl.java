package com.example.erp.service.impl;

import com.example.erp.dto.BalanceAdjustmentRequest;
import com.example.erp.dto.CreatePurchaseInvoiceRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PurchaseInvoiceAgingFilterRequest;
import com.example.erp.dto.PurchaseInvoiceAgingReportResponse;
import com.example.erp.dto.PurchaseInvoiceAgingRowResponse;
import com.example.erp.dto.PurchaseInvoiceFilterRequest;
import com.example.erp.dto.PurchaseInvoiceLineResponse;
import com.example.erp.dto.PurchaseInvoiceResponse;
import com.example.erp.entity.BalanceAdjustmentType;
import com.example.erp.entity.Company;
import com.example.erp.entity.GoodsReceipt;
import com.example.erp.entity.GoodsReceiptLine;
import com.example.erp.entity.GoodsReceiptStatus;
import com.example.erp.entity.Product;
import com.example.erp.entity.PurchaseCreditNote;
import com.example.erp.entity.PurchaseInvoice;
import com.example.erp.entity.PurchaseInvoiceLine;
import com.example.erp.entity.PurchaseInvoiceStatus;
import com.example.erp.entity.PurchaseOrder;
import com.example.erp.entity.PurchaseOrderLine;
import com.example.erp.entity.PurchaseOrderStatus;
import com.example.erp.entity.QualityCheckStatus;
import com.example.erp.entity.Supplier;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.GoodsReceiptLineRepository;
import com.example.erp.repository.GoodsReceiptRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.PurchaseCreditNoteRepository;
import com.example.erp.repository.PurchaseInvoiceLineRepository;
import com.example.erp.repository.PurchaseInvoiceRepository;
import com.example.erp.repository.PurchaseOrderLineRepository;
import com.example.erp.repository.PurchaseOrderRepository;
import com.example.erp.entity.SupplierPaymentAllocation;
import com.example.erp.repository.SupplierPaymentAllocationRepository;
import com.example.erp.repository.SupplierRepository;
import com.example.erp.service.AutoPostingService;
import com.example.erp.service.PurchaseInvoiceService;
import com.example.erp.service.SupplierService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseInvoiceServiceImpl implements PurchaseInvoiceService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final PurchaseInvoiceLineRepository purchaseInvoiceLineRepository;
    private final CompanyRepository companyRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierService supplierService;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final GoodsReceiptLineRepository goodsReceiptLineRepository;
    private final ProductRepository productRepository;
    private final PurchaseCreditNoteRepository purchaseCreditNoteRepository;
    private final SupplierPaymentAllocationRepository supplierPaymentAllocationRepository;
    private final AutoPostingService autoPostingService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PurchaseInvoiceResponse> listPurchaseInvoices(PurchaseInvoiceFilterRequest filter) {
        List<Specification<PurchaseInvoice>> conditions = new ArrayList<>();
        if (filter.getInvoiceNumber() != null && !filter.getInvoiceNumber().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("invoiceNumber")), "%" + filter.getInvoiceNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getSupplierId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("supplierId"), filter.getSupplierId()));
        }
        if (filter.getPurchaseOrderId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("purchaseOrderId"), filter.getPurchaseOrderId()));
        }
        if (filter.getGoodsReceiptId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("goodsReceiptId"), filter.getGoodsReceiptId()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        Specification<PurchaseInvoice> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<PurchaseInvoice> page = purchaseInvoiceRepository.findAll(spec, pageable);
        List<PurchaseInvoice> content = page.getContent();
        List<Long> invoiceIds = content.stream().map(PurchaseInvoice::getId).toList();
        Map<Long, List<PurchaseInvoiceLine>> linesByInvoiceId = new HashMap<>();
        for (Long invoiceId : invoiceIds) {
            linesByInvoiceId.put(invoiceId, purchaseInvoiceLineRepository.findByPurchaseInvoiceId(invoiceId));
        }

        return PageResponse.of(page.map(inv -> toResponse(inv, linesByInvoiceId.getOrDefault(inv.getId(), List.of()))));
    }

    @Override
    public PurchaseInvoiceResponse getPurchaseInvoice(Long id) {
        PurchaseInvoice invoice = find(id);
        return toResponse(invoice, purchaseInvoiceLineRepository.findByPurchaseInvoiceId(id));
    }

    @Override
    @Transactional
    public PurchaseInvoiceResponse createFromPurchaseOrder(Long purchaseOrderId, CreatePurchaseInvoiceRequest request, String actingUsername) {
        PurchaseOrder po = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Purchase order not found with id: " + purchaseOrderId));
        if (po.getStatus() != PurchaseOrderStatus.SENT && po.getStatus() != PurchaseOrderStatus.PARTIALLY_RECEIVED
                && po.getStatus() != PurchaseOrderStatus.RECEIVED) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Purchase order must be sent to the supplier before it can be invoiced (current status: " + po.getStatus() + ")");
        }
        List<PurchaseOrderLine> poLines = purchaseOrderLineRepository.findByPurchaseOrderId(po.getId());
        if (poLines.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Purchase order has no lines to invoice");
        }

        PurchaseInvoice invoice = PurchaseInvoice.builder()
                .companyId(po.getCompanyId())
                .supplierId(po.getSupplierId())
                .purchaseOrderId(po.getId())
                .invoiceDate(request.getInvoiceDate())
                .dueDate(request.getDueDate())
                .notes(request.getNotes())
                .createdBy(actingUsername)
                .build();
        purchaseInvoiceRepository.save(invoice);
        invoice.setInvoiceNumber("PINV-" + String.format("%06d", invoice.getId()));
        purchaseInvoiceRepository.save(invoice);

        List<PurchaseInvoiceLine> lines = poLines.stream()
                .map(l -> PurchaseInvoiceLine.builder()
                        .purchaseInvoiceId(invoice.getId())
                        .productId(l.getProductId())
                        .quantity(l.getQuantityOrdered())
                        .unitCost(l.getUnitCost())
                        .discountPercent(l.getDiscountPercent())
                        .taxRate(l.getTaxRate())
                        .build())
                .toList();
        lines = purchaseInvoiceLineRepository.saveAll(lines);

        return toResponse(invoice, lines);
    }

    @Override
    @Transactional
    public PurchaseInvoiceResponse createFromGoodsReceipt(Long goodsReceiptId, CreatePurchaseInvoiceRequest request, String actingUsername) {
        GoodsReceipt receipt = goodsReceiptRepository.findById(goodsReceiptId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Goods receipt not found with id: " + goodsReceiptId));
        if (receipt.getStatus() != GoodsReceiptStatus.COMPLETED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Every line on this receipt must be quality-checked before it can be invoiced");
        }
        PurchaseOrder po = purchaseOrderRepository.findById(receipt.getPurchaseOrderId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Purchase order not found with id: " + receipt.getPurchaseOrderId()));

        List<GoodsReceiptLine> receiptLines = goodsReceiptLineRepository.findByGoodsReceiptId(receipt.getId()).stream()
                .filter(l -> l.getQualityStatus() == QualityCheckStatus.PASSED)
                .toList();
        if (receiptLines.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Nothing to invoice — no lines on this receipt passed quality check");
        }
        Map<Long, PurchaseOrderLine> poLinesById = purchaseOrderLineRepository.findByPurchaseOrderId(po.getId()).stream()
                .collect(Collectors.toMap(PurchaseOrderLine::getId, l -> l));

        PurchaseInvoice invoice = PurchaseInvoice.builder()
                .companyId(receipt.getCompanyId())
                .supplierId(po.getSupplierId())
                .purchaseOrderId(po.getId())
                .goodsReceiptId(receipt.getId())
                .invoiceDate(request.getInvoiceDate())
                .dueDate(request.getDueDate())
                .notes(request.getNotes())
                .createdBy(actingUsername)
                .build();
        purchaseInvoiceRepository.save(invoice);
        invoice.setInvoiceNumber("PINV-" + String.format("%06d", invoice.getId()));
        purchaseInvoiceRepository.save(invoice);

        List<PurchaseInvoiceLine> lines = receiptLines.stream()
                .map(rl -> {
                    PurchaseOrderLine poLine = poLinesById.get(rl.getPurchaseOrderLineId());
                    return PurchaseInvoiceLine.builder()
                            .purchaseInvoiceId(invoice.getId())
                            .productId(rl.getProductId())
                            .quantity(rl.getQuantityReceived())
                            .unitCost(poLine == null ? BigDecimal.ZERO : poLine.getUnitCost())
                            .discountPercent(poLine == null ? BigDecimal.ZERO : poLine.getDiscountPercent())
                            .taxRate(poLine == null ? BigDecimal.ZERO : poLine.getTaxRate())
                            .build();
                })
                .toList();
        lines = purchaseInvoiceLineRepository.saveAll(lines);

        return toResponse(invoice, lines);
    }

    @Override
    @Transactional
    public PurchaseInvoiceResponse approvePurchaseInvoice(Long id, String actingUsername) {
        PurchaseInvoice invoice = find(id);
        if (!invoice.getStatus().canTransitionTo(PurchaseInvoiceStatus.APPROVED)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft purchase invoices can be approved");
        }
        List<PurchaseInvoiceLine> lines = purchaseInvoiceLineRepository.findByPurchaseInvoiceId(id);
        BigDecimal[] totals = computeTotals(lines);
        BigDecimal netAmount = totals[0].subtract(totals[1]);
        BigDecimal taxAmount = totals[2];
        BigDecimal totalAmount = totals[3];

        invoice.setStatus(PurchaseInvoiceStatus.APPROVED);
        purchaseInvoiceRepository.save(invoice);

        BalanceAdjustmentRequest chargeRequest = new BalanceAdjustmentRequest();
        chargeRequest.setType(BalanceAdjustmentType.CHARGE);
        chargeRequest.setAmount(totalAmount);
        chargeRequest.setNote("Purchase invoice " + invoice.getInvoiceNumber());
        supplierService.adjustBalance(invoice.getSupplierId(), chargeRequest, actingUsername);

        autoPostingService.postPurchaseInvoiceApproval(invoice, netAmount, taxAmount, totalAmount, actingUsername);

        return toResponse(invoice, lines);
    }

    @Override
    @Transactional
    public PurchaseInvoiceResponse cancelPurchaseInvoice(Long id, String actingUsername) {
        PurchaseInvoice invoice = find(id);
        if (!invoice.getStatus().canTransitionTo(PurchaseInvoiceStatus.CANCELLED)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft or approved purchase invoices can be cancelled");
        }
        List<PurchaseInvoiceLine> lines = purchaseInvoiceLineRepository.findByPurchaseInvoiceId(id);

        if (invoice.getStatus() == PurchaseInvoiceStatus.APPROVED) {
            BigDecimal totalAmount = computeTotals(lines)[3];
            BalanceAdjustmentRequest reverseRequest = new BalanceAdjustmentRequest();
            reverseRequest.setType(BalanceAdjustmentType.PAYMENT);
            reverseRequest.setAmount(totalAmount);
            reverseRequest.setNote("Reversal of cancelled purchase invoice " + invoice.getInvoiceNumber());
            supplierService.adjustBalance(invoice.getSupplierId(), reverseRequest, actingUsername);
            autoPostingService.reverseAutoEntry("PURCHASE_INVOICE", invoice.getId(), actingUsername);
        }

        // Captured before the overwrite — this is the only record of how far the workflow got.
        invoice.setCancelledFromStatus(invoice.getStatus());
        invoice.setStatus(PurchaseInvoiceStatus.CANCELLED);
        purchaseInvoiceRepository.save(invoice);
        return toResponse(invoice, lines);
    }

    @Override
    @Transactional
    public void deletePurchaseInvoice(Long id) {
        PurchaseInvoice invoice = find(id);
        if (invoice.getStatus() != PurchaseInvoiceStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft purchase invoices can be deleted");
        }
        purchaseInvoiceLineRepository.deleteByPurchaseInvoiceId(id);
        purchaseInvoiceRepository.deleteById(id);
    }

    // Returns [subtotal, discountAmount, taxAmount, totalAmount].
    private BigDecimal[] computeTotals(List<PurchaseInvoiceLine> lines) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        for (PurchaseInvoiceLine line : lines) {
            BigDecimal lineSubtotal = line.getQuantity().multiply(line.getUnitCost());
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

    private PurchaseInvoice find(Long id) {
        return purchaseInvoiceRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Purchase invoice not found with id: " + id));
    }

    private PurchaseInvoiceResponse toResponse(PurchaseInvoice invoice, List<PurchaseInvoiceLine> lines) {
        String companyName = companyRepository.findById(invoice.getCompanyId()).map(Company::getName).orElse(null);
        String supplierName = supplierRepository.findById(invoice.getSupplierId()).map(Supplier::getName).orElse(null);
        String poNumber = purchaseOrderRepository.findById(invoice.getPurchaseOrderId()).map(PurchaseOrder::getPoNumber).orElse(null);
        String receiptNumber = invoice.getGoodsReceiptId() == null ? null
                : goodsReceiptRepository.findById(invoice.getGoodsReceiptId()).map(GoodsReceipt::getReceiptNumber).orElse(null);

        Map<Long, Product> products = lines.isEmpty() ? Map.of() : productRepository.findAllById(
                lines.stream().map(PurchaseInvoiceLine::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<PurchaseInvoiceLineResponse> lineResponses = lines.stream()
                .map(line -> {
                    Product product = products.get(line.getProductId());
                    BigDecimal lineSubtotal = line.getQuantity().multiply(line.getUnitCost());
                    BigDecimal discountAmount = lineSubtotal.multiply(line.getDiscountPercent()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
                    BigDecimal afterDiscount = lineSubtotal.subtract(discountAmount);
                    BigDecimal taxAmount = afterDiscount.multiply(line.getTaxRate()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
                    return PurchaseInvoiceLineResponse.builder()
                            .id(line.getId())
                            .productId(line.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productSku(product == null ? null : product.getSku())
                            .quantity(line.getQuantity())
                            .unitCost(line.getUnitCost())
                            .discountPercent(line.getDiscountPercent())
                            .discountAmount(discountAmount)
                            .taxRate(line.getTaxRate())
                            .taxAmount(taxAmount)
                            .lineTotal(afterDiscount.add(taxAmount))
                            .build();
                })
                .toList();

        BigDecimal[] totals = computeTotals(lines);
        BigDecimal creditedAmount = purchaseCreditNoteRepository.findByPurchaseInvoiceId(invoice.getId()).stream()
                .map(PurchaseCreditNote::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // Net of payment allocations (payments minus any refunds against them).
        BigDecimal paidAmount = supplierPaymentAllocationRepository.findByPurchaseInvoiceId(invoice.getId()).stream()
                .map(SupplierPaymentAllocation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal outstandingAmount = totals[3].subtract(creditedAmount).subtract(paidAmount);
        String paymentStatus = outstandingAmount.compareTo(BigDecimal.ZERO) <= 0
                ? (totals[3].compareTo(BigDecimal.ZERO) == 0 ? "UNPAID" : "PAID")
                : (paidAmount.compareTo(BigDecimal.ZERO) > 0 ? "PARTIALLY_PAID" : "UNPAID");
        int daysOverdue = daysOverdue(invoice.getDueDate(), outstandingAmount, LocalDate.now());

        return PurchaseInvoiceResponse.builder()
                .id(invoice.getId())
                .companyId(invoice.getCompanyId())
                .companyName(companyName)
                .supplierId(invoice.getSupplierId())
                .supplierName(supplierName)
                .purchaseOrderId(invoice.getPurchaseOrderId())
                .poNumber(poNumber)
                .goodsReceiptId(invoice.getGoodsReceiptId())
                .receiptNumber(receiptNumber)
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceDate(invoice.getInvoiceDate())
                .dueDate(invoice.getDueDate())
                .status(invoice.getStatus().name())
                .cancelledFromStatus(invoice.getCancelledFromStatus() == null ? null : invoice.getCancelledFromStatus().name())
                .notes(invoice.getNotes())
                .createdBy(invoice.getCreatedBy())
                .subtotal(totals[0])
                .discountAmount(totals[1])
                .taxAmount(totals[2])
                .totalAmount(totals[3])
                .creditedAmount(creditedAmount)
                .paidAmount(paidAmount)
                .outstandingAmount(outstandingAmount)
                .paymentStatus(paymentStatus)
                .overdue(daysOverdue > 0)
                .daysOverdue(daysOverdue)
                .lines(lineResponses)
                .build();
    }

    // 0 when there's nothing left to pay, no due date is set, or the due date
    // hasn't passed yet as of `asOf`.
    private int daysOverdue(LocalDate dueDate, BigDecimal outstandingAmount, LocalDate asOf) {
        if (dueDate == null || outstandingAmount.compareTo(BigDecimal.ZERO) <= 0) return 0;
        long days = ChronoUnit.DAYS.between(dueDate, asOf);
        return days > 0 ? (int) days : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseInvoiceAgingReportResponse agingReport(PurchaseInvoiceAgingFilterRequest filter) {
        LocalDate asOfDate = filter.getAsOfDate() != null ? filter.getAsOfDate() : LocalDate.now();

        List<Specification<PurchaseInvoice>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> cb.equal(root.get("status"), PurchaseInvoiceStatus.APPROVED));
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getSupplierId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("supplierId"), filter.getSupplierId()));
        }
        List<PurchaseInvoice> invoices = purchaseInvoiceRepository.findAll(Specification.allOf(conditions));

        Map<Long, PurchaseInvoiceAgingRowResponse> rowsBySupplier = new java.util.LinkedHashMap<>();
        PurchaseInvoiceAgingRowResponse grandTotal = PurchaseInvoiceAgingRowResponse.builder()
                .supplierId(null).supplierName("Total")
                .current(BigDecimal.ZERO).days1To30(BigDecimal.ZERO).days31To60(BigDecimal.ZERO)
                .days61To90(BigDecimal.ZERO).days90Plus(BigDecimal.ZERO).total(BigDecimal.ZERO)
                .build();

        for (PurchaseInvoice invoice : invoices) {
            List<PurchaseInvoiceLine> lines = purchaseInvoiceLineRepository.findByPurchaseInvoiceId(invoice.getId());
            BigDecimal[] totals = computeTotals(lines);
            BigDecimal credited = purchaseCreditNoteRepository.findByPurchaseInvoiceId(invoice.getId()).stream()
                    .map(PurchaseCreditNote::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal paid = supplierPaymentAllocationRepository.findByPurchaseInvoiceId(invoice.getId()).stream()
                    .map(SupplierPaymentAllocation::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal outstanding = totals[3].subtract(credited).subtract(paid);
            if (outstanding.compareTo(BigDecimal.ZERO) <= 0) continue;

            long days = invoice.getDueDate() == null ? 0 : Math.max(0, ChronoUnit.DAYS.between(invoice.getDueDate(), asOfDate));

            PurchaseInvoiceAgingRowResponse row = rowsBySupplier.computeIfAbsent(invoice.getSupplierId(), id -> PurchaseInvoiceAgingRowResponse.builder()
                    .supplierId(id)
                    .supplierName(supplierRepository.findById(id).map(Supplier::getName).orElse(null))
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

        return PurchaseInvoiceAgingReportResponse.builder()
                .asOfDate(asOfDate)
                .rows(new ArrayList<>(rowsBySupplier.values()))
                .totals(grandTotal)
                .build();
    }
}
