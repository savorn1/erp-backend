package com.example.erp.service.impl;

import com.example.erp.dto.BalanceAdjustmentRequest;
import com.example.erp.dto.CreateSupplierPaymentRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.RefundSupplierPaymentRequest;
import com.example.erp.dto.SupplierPaymentAllocationRequest;
import com.example.erp.dto.SupplierPaymentAllocationResponse;
import com.example.erp.dto.SupplierPaymentFilterRequest;
import com.example.erp.dto.SupplierPaymentResponse;
import com.example.erp.entity.BalanceAdjustmentType;
import com.example.erp.entity.Company;
import com.example.erp.entity.PurchaseCreditNote;
import com.example.erp.entity.PurchaseInvoice;
import com.example.erp.entity.PurchaseInvoiceLine;
import com.example.erp.entity.PurchaseInvoiceStatus;
import com.example.erp.entity.PaymentType;
import com.example.erp.entity.Supplier;
import com.example.erp.entity.SupplierPayment;
import com.example.erp.entity.SupplierPaymentAllocation;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.PurchaseCreditNoteRepository;
import com.example.erp.repository.PurchaseInvoiceLineRepository;
import com.example.erp.repository.PurchaseInvoiceRepository;
import com.example.erp.repository.SupplierPaymentAllocationRepository;
import com.example.erp.repository.SupplierPaymentRepository;
import com.example.erp.repository.SupplierRepository;
import com.example.erp.service.SupplierPaymentService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierPaymentServiceImpl implements SupplierPaymentService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final SupplierPaymentRepository supplierPaymentRepository;
    private final SupplierPaymentAllocationRepository supplierPaymentAllocationRepository;
    private final CompanyRepository companyRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierService supplierService;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final PurchaseInvoiceLineRepository purchaseInvoiceLineRepository;
    private final PurchaseCreditNoteRepository purchaseCreditNoteRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SupplierPaymentResponse> listPayments(SupplierPaymentFilterRequest filter) {
        List<Specification<SupplierPayment>> conditions = new ArrayList<>();
        if (filter.getPaymentNumber() != null && !filter.getPaymentNumber().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("paymentNumber")), "%" + filter.getPaymentNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getSupplierId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("supplierId"), filter.getSupplierId()));
        }
        if (filter.getMethod() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("method"), filter.getMethod()));
        }
        if (filter.getType() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("type"), filter.getType()));
        }
        Specification<SupplierPayment> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<SupplierPayment> page = supplierPaymentRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    public SupplierPaymentResponse getPayment(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public SupplierPaymentResponse recordPayment(CreateSupplierPaymentRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());
        requireSupplier(request.getSupplierId(), request.getCompanyId());

        BigDecimal total = BigDecimal.ZERO;
        for (SupplierPaymentAllocationRequest allocationRequest : request.getAllocations()) {
            PurchaseInvoice invoice = purchaseInvoiceRepository.findById(allocationRequest.getPurchaseInvoiceId())
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST,
                            "Purchase invoice not found with id: " + allocationRequest.getPurchaseInvoiceId()));
            if (!invoice.getSupplierId().equals(request.getSupplierId())) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Purchase invoice " + invoice.getInvoiceNumber() + " does not belong to the selected supplier");
            }
            if (invoice.getStatus() != PurchaseInvoiceStatus.APPROVED) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Only approved purchase invoices can receive payment (" + invoice.getInvoiceNumber() + ")");
            }
            BigDecimal outstanding = outstandingAmountOf(invoice);
            if (allocationRequest.getAmount().compareTo(outstanding) > 0) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Allocation exceeds outstanding balance (" + outstanding + ") on invoice " + invoice.getInvoiceNumber());
            }
            total = total.add(allocationRequest.getAmount());
        }

        SupplierPayment payment = SupplierPayment.builder()
                .companyId(request.getCompanyId())
                .supplierId(request.getSupplierId())
                .paymentDate(request.getPaymentDate())
                .type(PaymentType.PAYMENT)
                .method(request.getMethod())
                .amount(total)
                .reference(request.getReference())
                .notes(request.getNotes())
                .createdBy(actingUsername)
                .build();
        supplierPaymentRepository.save(payment);
        payment.setPaymentNumber("SPAY-" + String.format("%06d", payment.getId()));
        supplierPaymentRepository.save(payment);

        for (SupplierPaymentAllocationRequest allocationRequest : request.getAllocations()) {
            supplierPaymentAllocationRepository.save(SupplierPaymentAllocation.builder()
                    .supplierPaymentId(payment.getId())
                    .purchaseInvoiceId(allocationRequest.getPurchaseInvoiceId())
                    .amount(allocationRequest.getAmount())
                    .build());
        }

        BalanceAdjustmentRequest adjustment = new BalanceAdjustmentRequest();
        adjustment.setType(BalanceAdjustmentType.PAYMENT);
        adjustment.setAmount(total);
        adjustment.setNote("Payment " + payment.getPaymentNumber());
        supplierService.adjustBalance(request.getSupplierId(), adjustment, actingUsername);

        return toResponse(payment);
    }

    @Override
    @Transactional
    public SupplierPaymentResponse refundPayment(Long id, RefundSupplierPaymentRequest request, String actingUsername) {
        SupplierPayment original = find(id);
        if (original.getType() != PaymentType.PAYMENT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only a payment (not a refund) can be refunded");
        }
        BigDecimal alreadyRefunded = supplierPaymentRepository.findByRelatedPaymentId(id).stream()
                .map(SupplierPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remainingRefundable = original.getAmount().subtract(alreadyRefunded);
        if (request.getAmount().compareTo(remainingRefundable) > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Refund amount exceeds what remains refundable on this payment (" + remainingRefundable + ")");
        }

        SupplierPayment refund = SupplierPayment.builder()
                .companyId(original.getCompanyId())
                .supplierId(original.getSupplierId())
                .paymentDate(request.getRefundDate())
                .type(PaymentType.REFUND)
                .method(original.getMethod())
                .amount(request.getAmount())
                .reference("Refund of " + original.getPaymentNumber())
                .notes(request.getReason())
                .relatedPaymentId(original.getId())
                .createdBy(actingUsername)
                .build();
        supplierPaymentRepository.save(refund);
        refund.setPaymentNumber("SREF-" + String.format("%06d", refund.getId()));
        supplierPaymentRepository.save(refund);

        // Reverse the original allocations proportionally — a partial refund
        // gives each originally-credited invoice back a proportional share of
        // its outstanding reduction.
        List<SupplierPaymentAllocation> originalAllocations = supplierPaymentAllocationRepository.findBySupplierPaymentId(original.getId());
        BigDecimal refundRatio = request.getAmount().divide(original.getAmount(), 8, RoundingMode.HALF_UP);
        for (SupplierPaymentAllocation allocation : originalAllocations) {
            BigDecimal reversedAmount = allocation.getAmount().multiply(refundRatio).setScale(4, RoundingMode.HALF_UP);
            if (reversedAmount.compareTo(BigDecimal.ZERO) <= 0) continue;
            supplierPaymentAllocationRepository.save(SupplierPaymentAllocation.builder()
                    .supplierPaymentId(refund.getId())
                    .purchaseInvoiceId(allocation.getPurchaseInvoiceId())
                    .amount(reversedAmount.negate())
                    .build());
        }

        BalanceAdjustmentRequest adjustment = new BalanceAdjustmentRequest();
        adjustment.setType(BalanceAdjustmentType.CHARGE);
        adjustment.setAmount(request.getAmount());
        adjustment.setNote("Refund " + refund.getPaymentNumber() + " of payment " + original.getPaymentNumber());
        supplierService.adjustBalance(original.getSupplierId(), adjustment, actingUsername);

        return toResponse(refund);
    }

    // Mirrors PurchaseCreditNoteServiceImpl.computeTotal — the invoice's own
    // total minus everything already credited/paid against it.
    private BigDecimal outstandingAmountOf(PurchaseInvoice invoice) {
        List<PurchaseInvoiceLine> lines = purchaseInvoiceLineRepository.findByPurchaseInvoiceId(invoice.getId());
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseInvoiceLine line : lines) {
            BigDecimal lineSubtotal = line.getQuantity().multiply(line.getUnitCost());
            BigDecimal discountAmount = lineSubtotal.multiply(line.getDiscountPercent()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
            BigDecimal afterDiscount = lineSubtotal.subtract(discountAmount);
            BigDecimal taxAmount = afterDiscount.multiply(line.getTaxRate()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
            total = total.add(afterDiscount).add(taxAmount);
        }
        BigDecimal credited = purchaseCreditNoteRepository.findByPurchaseInvoiceId(invoice.getId()).stream()
                .map(PurchaseCreditNote::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paid = supplierPaymentAllocationRepository.findByPurchaseInvoiceId(invoice.getId()).stream()
                .map(SupplierPaymentAllocation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.subtract(credited).subtract(paid);
    }

    private void requireCompany(Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private void requireSupplier(Long supplierId, Long companyId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Supplier not found with id: " + supplierId));
        if (!supplier.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Supplier does not belong to the selected company");
        }
    }

    private SupplierPayment find(Long id) {
        return supplierPaymentRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Supplier payment not found with id: " + id));
    }

    private SupplierPaymentResponse toResponse(SupplierPayment payment) {
        String companyName = companyRepository.findById(payment.getCompanyId()).map(Company::getName).orElse(null);
        String supplierName = supplierRepository.findById(payment.getSupplierId()).map(Supplier::getName).orElse(null);
        String relatedPaymentNumber = payment.getRelatedPaymentId() == null ? null
                : supplierPaymentRepository.findById(payment.getRelatedPaymentId()).map(SupplierPayment::getPaymentNumber).orElse(null);

        List<SupplierPaymentAllocation> allocations = supplierPaymentAllocationRepository.findBySupplierPaymentId(payment.getId());
        Map<Long, String> invoiceNumbers = purchaseInvoiceRepository.findAllById(
                allocations.stream().map(SupplierPaymentAllocation::getPurchaseInvoiceId).distinct().toList()
        ).stream().collect(Collectors.toMap(PurchaseInvoice::getId, PurchaseInvoice::getInvoiceNumber));

        List<SupplierPaymentAllocationResponse> allocationResponses = allocations.stream()
                .map(a -> SupplierPaymentAllocationResponse.builder()
                        .id(a.getId())
                        .purchaseInvoiceId(a.getPurchaseInvoiceId())
                        .invoiceNumber(invoiceNumbers.get(a.getPurchaseInvoiceId()))
                        .amount(a.getAmount())
                        .build())
                .toList();

        BigDecimal refundedAmount = payment.getType() == PaymentType.PAYMENT
                ? supplierPaymentRepository.findByRelatedPaymentId(payment.getId()).stream()
                        .map(SupplierPayment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
                : null;

        return SupplierPaymentResponse.builder()
                .id(payment.getId())
                .companyId(payment.getCompanyId())
                .companyName(companyName)
                .supplierId(payment.getSupplierId())
                .supplierName(supplierName)
                .paymentNumber(payment.getPaymentNumber())
                .paymentDate(payment.getPaymentDate())
                .type(payment.getType().name())
                .method(payment.getMethod().name())
                .amount(payment.getAmount())
                .reference(payment.getReference())
                .notes(payment.getNotes())
                .createdBy(payment.getCreatedBy())
                .relatedPaymentId(payment.getRelatedPaymentId())
                .relatedPaymentNumber(relatedPaymentNumber)
                .refundedAmount(refundedAmount)
                .allocations(allocationResponses)
                .build();
    }
}
