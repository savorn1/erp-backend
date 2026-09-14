package com.example.erp.service.impl;

import com.example.erp.dto.TaxByCustomerResponse;
import com.example.erp.dto.TaxByCustomerRowResponse;
import com.example.erp.dto.TaxByProductResponse;
import com.example.erp.dto.TaxByProductRowResponse;
import com.example.erp.dto.TaxBySupplierResponse;
import com.example.erp.dto.TaxBySupplierRowResponse;
import com.example.erp.dto.TaxDetailResponse;
import com.example.erp.dto.TaxDetailRowResponse;
import com.example.erp.dto.TaxReportFilterRequest;
import com.example.erp.dto.TaxReportRateRowResponse;
import com.example.erp.dto.TaxReportResponse;
import com.example.erp.entity.Customer;
import com.example.erp.entity.Invoice;
import com.example.erp.entity.InvoiceLine;
import com.example.erp.entity.InvoiceStatus;
import com.example.erp.entity.Product;
import com.example.erp.entity.PurchaseInvoice;
import com.example.erp.entity.PurchaseInvoiceLine;
import com.example.erp.entity.PurchaseInvoiceStatus;
import com.example.erp.entity.Supplier;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.InvoiceLineRepository;
import com.example.erp.repository.InvoiceRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.PurchaseInvoiceLineRepository;
import com.example.erp.repository.PurchaseInvoiceRepository;
import com.example.erp.repository.SupplierRepository;
import com.example.erp.service.TaxReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// Groups tax already recorded on approved Invoice/PurchaseInvoice lines by
// their raw taxRate percent — there's no TaxRate linkage on those lines (see
// TaxRate's own comment), so this can't label a row "VAT" vs "Withholding",
// only "X% tax". Still gives the standard output-tax/input-tax/net-payable
// shape of a VAT-style tax report.
// detail()/byCustomer()/bySupplier()/byProduct() break the same underlying
// lines down differently (line-level detail, by customer, by supplier, by
// product) but share this same "raw rate, no tax code" limitation.
@Service
@RequiredArgsConstructor
public class TaxReportServiceImpl implements TaxReportService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final PurchaseInvoiceLineRepository purchaseInvoiceLineRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public TaxReportResponse generate(TaxReportFilterRequest filter) {
        List<TaxReportRateRowResponse> outputTax = computeOutputTax(filter);
        List<TaxReportRateRowResponse> inputTax = computeInputTax(filter);

        BigDecimal outputTotal = outputTax.stream().map(TaxReportRateRowResponse::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal inputTotal = inputTax.stream().map(TaxReportRateRowResponse::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        return TaxReportResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .outputTax(outputTax)
                .outputTaxTotal(outputTotal)
                .inputTax(inputTax)
                .inputTaxTotal(inputTotal)
                .netTaxPayable(outputTotal.subtract(inputTotal))
                .build();
    }

    private List<TaxReportRateRowResponse> computeOutputTax(TaxReportFilterRequest filter) {
        TreeMap<BigDecimal, BigDecimal[]> byRate = new TreeMap<>(Comparator.naturalOrder());
        for (Invoice invoice : approvedInvoices(filter)) {
            for (InvoiceLine line : invoiceLineRepository.findByInvoiceId(invoice.getId())) {
                BigDecimal afterDiscount = afterDiscount(line.getQuantity(), line.getUnitPrice(), line.getDiscountPercent());
                BigDecimal taxAmount = taxAmount(afterDiscount, line.getTaxRate());
                accumulate(byRate, line.getTaxRate(), afterDiscount, taxAmount);
            }
        }
        return toRows(byRate);
    }

    private List<TaxReportRateRowResponse> computeInputTax(TaxReportFilterRequest filter) {
        TreeMap<BigDecimal, BigDecimal[]> byRate = new TreeMap<>(Comparator.naturalOrder());
        for (PurchaseInvoice invoice : approvedPurchaseInvoices(filter)) {
            for (PurchaseInvoiceLine line : purchaseInvoiceLineRepository.findByPurchaseInvoiceId(invoice.getId())) {
                BigDecimal afterDiscount = afterDiscount(line.getQuantity(), line.getUnitCost(), line.getDiscountPercent());
                BigDecimal taxAmount = taxAmount(afterDiscount, line.getTaxRate());
                accumulate(byRate, line.getTaxRate(), afterDiscount, taxAmount);
            }
        }
        return toRows(byRate);
    }

    @Override
    @Transactional(readOnly = true)
    public TaxDetailResponse detail(TaxReportFilterRequest filter) {
        List<TaxDetailRowResponse> rows = new ArrayList<>();
        BigDecimal totalOutputTax = BigDecimal.ZERO;
        BigDecimal totalInputTax = BigDecimal.ZERO;

        for (Invoice invoice : approvedInvoices(filter)) {
            Customer customer = customerRepository.findById(invoice.getCustomerId()).orElse(null);
            for (InvoiceLine line : invoiceLineRepository.findByInvoiceId(invoice.getId())) {
                BigDecimal afterDiscount = afterDiscount(line.getQuantity(), line.getUnitPrice(), line.getDiscountPercent());
                BigDecimal taxAmount = taxAmount(afterDiscount, line.getTaxRate());
                if (taxAmount.compareTo(BigDecimal.ZERO) == 0) continue;
                rows.add(TaxDetailRowResponse.builder()
                        .type("Output")
                        .date(invoice.getInvoiceDate())
                        .reference(invoice.getInvoiceNumber())
                        .partyId(invoice.getCustomerId())
                        .partyName(customer == null ? null : customer.getName())
                        .taxableAmount(afterDiscount)
                        .taxRatePercent(line.getTaxRate())
                        .taxAmount(taxAmount)
                        .build());
                totalOutputTax = totalOutputTax.add(taxAmount);
            }
        }

        for (PurchaseInvoice invoice : approvedPurchaseInvoices(filter)) {
            Supplier supplier = supplierRepository.findById(invoice.getSupplierId()).orElse(null);
            for (PurchaseInvoiceLine line : purchaseInvoiceLineRepository.findByPurchaseInvoiceId(invoice.getId())) {
                BigDecimal afterDiscount = afterDiscount(line.getQuantity(), line.getUnitCost(), line.getDiscountPercent());
                BigDecimal taxAmount = taxAmount(afterDiscount, line.getTaxRate());
                if (taxAmount.compareTo(BigDecimal.ZERO) == 0) continue;
                rows.add(TaxDetailRowResponse.builder()
                        .type("Input")
                        .date(invoice.getInvoiceDate())
                        .reference(invoice.getInvoiceNumber())
                        .partyId(invoice.getSupplierId())
                        .partyName(supplier == null ? null : supplier.getName())
                        .taxableAmount(afterDiscount)
                        .taxRatePercent(line.getTaxRate())
                        .taxAmount(taxAmount)
                        .build());
                totalInputTax = totalInputTax.add(taxAmount);
            }
        }
        rows.sort(Comparator.comparing(TaxDetailRowResponse::getDate).reversed());

        return TaxDetailResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalOutputTax(totalOutputTax)
                .totalInputTax(totalInputTax)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TaxByCustomerResponse byCustomer(TaxReportFilterRequest filter) {
        Map<Long, BigDecimal[]> byCustomer = new HashMap<>(); // [taxableAmount, taxAmount]
        for (Invoice invoice : approvedInvoices(filter)) {
            for (InvoiceLine line : invoiceLineRepository.findByInvoiceId(invoice.getId())) {
                BigDecimal afterDiscount = afterDiscount(line.getQuantity(), line.getUnitPrice(), line.getDiscountPercent());
                BigDecimal taxAmount = taxAmount(afterDiscount, line.getTaxRate());
                if (taxAmount.compareTo(BigDecimal.ZERO) == 0) continue;
                BigDecimal[] bucket = byCustomer.computeIfAbsent(invoice.getCustomerId(), id -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                bucket[0] = bucket[0].add(afterDiscount);
                bucket[1] = bucket[1].add(taxAmount);
            }
        }

        Map<Long, Customer> customers = customerRepository.findAllById(byCustomer.keySet()).stream()
                .collect(java.util.stream.Collectors.toMap(Customer::getId, c -> c));

        List<TaxByCustomerRowResponse> rows = new ArrayList<>();
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal[]> entry : byCustomer.entrySet()) {
            Customer customer = customers.get(entry.getKey());
            rows.add(TaxByCustomerRowResponse.builder()
                    .customerId(entry.getKey())
                    .customerName(customer == null ? null : customer.getName())
                    .taxableAmount(entry.getValue()[0])
                    .taxAmount(entry.getValue()[1])
                    .build());
            totalTaxAmount = totalTaxAmount.add(entry.getValue()[1]);
        }
        rows.sort(Comparator.comparing(TaxByCustomerRowResponse::getTaxAmount).reversed());

        return TaxByCustomerResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalTaxAmount(totalTaxAmount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TaxBySupplierResponse bySupplier(TaxReportFilterRequest filter) {
        Map<Long, BigDecimal[]> bySupplier = new HashMap<>(); // [taxableAmount, taxAmount]
        for (PurchaseInvoice invoice : approvedPurchaseInvoices(filter)) {
            for (PurchaseInvoiceLine line : purchaseInvoiceLineRepository.findByPurchaseInvoiceId(invoice.getId())) {
                BigDecimal afterDiscount = afterDiscount(line.getQuantity(), line.getUnitCost(), line.getDiscountPercent());
                BigDecimal taxAmount = taxAmount(afterDiscount, line.getTaxRate());
                if (taxAmount.compareTo(BigDecimal.ZERO) == 0) continue;
                BigDecimal[] bucket = bySupplier.computeIfAbsent(invoice.getSupplierId(), id -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                bucket[0] = bucket[0].add(afterDiscount);
                bucket[1] = bucket[1].add(taxAmount);
            }
        }

        Map<Long, Supplier> suppliers = supplierRepository.findAllById(bySupplier.keySet()).stream()
                .collect(java.util.stream.Collectors.toMap(Supplier::getId, s -> s));

        List<TaxBySupplierRowResponse> rows = new ArrayList<>();
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal[]> entry : bySupplier.entrySet()) {
            Supplier supplier = suppliers.get(entry.getKey());
            rows.add(TaxBySupplierRowResponse.builder()
                    .supplierId(entry.getKey())
                    .supplierName(supplier == null ? null : supplier.getName())
                    .taxableAmount(entry.getValue()[0])
                    .taxAmount(entry.getValue()[1])
                    .build());
            totalTaxAmount = totalTaxAmount.add(entry.getValue()[1]);
        }
        rows.sort(Comparator.comparing(TaxBySupplierRowResponse::getTaxAmount).reversed());

        return TaxBySupplierResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalTaxAmount(totalTaxAmount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TaxByProductResponse byProduct(TaxReportFilterRequest filter) {
        Map<Long, BigDecimal[]> byProduct = new HashMap<>(); // [outputTax, inputTax]
        for (Invoice invoice : approvedInvoices(filter)) {
            for (InvoiceLine line : invoiceLineRepository.findByInvoiceId(invoice.getId())) {
                BigDecimal afterDiscount = afterDiscount(line.getQuantity(), line.getUnitPrice(), line.getDiscountPercent());
                BigDecimal taxAmount = taxAmount(afterDiscount, line.getTaxRate());
                if (taxAmount.compareTo(BigDecimal.ZERO) == 0) continue;
                BigDecimal[] bucket = byProduct.computeIfAbsent(line.getProductId(), id -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                bucket[0] = bucket[0].add(taxAmount);
            }
        }
        for (PurchaseInvoice invoice : approvedPurchaseInvoices(filter)) {
            for (PurchaseInvoiceLine line : purchaseInvoiceLineRepository.findByPurchaseInvoiceId(invoice.getId())) {
                BigDecimal afterDiscount = afterDiscount(line.getQuantity(), line.getUnitCost(), line.getDiscountPercent());
                BigDecimal taxAmount = taxAmount(afterDiscount, line.getTaxRate());
                if (taxAmount.compareTo(BigDecimal.ZERO) == 0) continue;
                BigDecimal[] bucket = byProduct.computeIfAbsent(line.getProductId(), id -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                bucket[1] = bucket[1].add(taxAmount);
            }
        }

        Map<Long, Product> products = productRepository.findAllById(byProduct.keySet()).stream()
                .collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));

        List<TaxByProductRowResponse> rows = new ArrayList<>();
        BigDecimal totalOutputTax = BigDecimal.ZERO;
        BigDecimal totalInputTax = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal[]> entry : byProduct.entrySet()) {
            Product product = products.get(entry.getKey());
            rows.add(TaxByProductRowResponse.builder()
                    .productId(entry.getKey())
                    .productName(product == null ? null : product.getName())
                    .productSku(product == null ? null : product.getSku())
                    .outputTaxAmount(entry.getValue()[0])
                    .inputTaxAmount(entry.getValue()[1])
                    .build());
            totalOutputTax = totalOutputTax.add(entry.getValue()[0]);
            totalInputTax = totalInputTax.add(entry.getValue()[1]);
        }
        rows.sort(Comparator.comparing((TaxByProductRowResponse r) -> r.getOutputTaxAmount().add(r.getInputTaxAmount())).reversed());

        return TaxByProductResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalOutputTax(totalOutputTax)
                .totalInputTax(totalInputTax)
                .build();
    }

    private List<Invoice> approvedInvoices(TaxReportFilterRequest filter) {
        List<Specification<Invoice>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> cb.equal(root.get("status"), InvoiceStatus.APPROVED));
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getDateFrom() != null) conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("invoiceDate"), filter.getDateFrom()));
        if (filter.getDateTo() != null) conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("invoiceDate"), filter.getDateTo()));
        return invoiceRepository.findAll(Specification.allOf(conditions));
    }

    private List<PurchaseInvoice> approvedPurchaseInvoices(TaxReportFilterRequest filter) {
        List<Specification<PurchaseInvoice>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> cb.equal(root.get("status"), PurchaseInvoiceStatus.APPROVED));
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getDateFrom() != null) conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("invoiceDate"), filter.getDateFrom()));
        if (filter.getDateTo() != null) conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("invoiceDate"), filter.getDateTo()));
        return purchaseInvoiceRepository.findAll(Specification.allOf(conditions));
    }

    private BigDecimal afterDiscount(BigDecimal quantity, BigDecimal unitAmount, BigDecimal discountPercent) {
        BigDecimal lineSubtotal = quantity.multiply(unitAmount);
        BigDecimal discount = lineSubtotal.multiply(discountPercent).divide(HUNDRED, 4, RoundingMode.HALF_UP);
        return lineSubtotal.subtract(discount);
    }

    private BigDecimal taxAmount(BigDecimal afterDiscount, BigDecimal taxRate) {
        return afterDiscount.multiply(taxRate).divide(HUNDRED, 4, RoundingMode.HALF_UP);
    }

    private void accumulate(TreeMap<BigDecimal, BigDecimal[]> byRate, BigDecimal rate, BigDecimal taxableAmount, BigDecimal taxAmount) {
        if (taxAmount.compareTo(BigDecimal.ZERO) == 0) return;
        BigDecimal[] bucket = byRate.computeIfAbsent(rate, r -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        bucket[0] = bucket[0].add(taxableAmount);
        bucket[1] = bucket[1].add(taxAmount);
    }

    private List<TaxReportRateRowResponse> toRows(TreeMap<BigDecimal, BigDecimal[]> byRate) {
        List<TaxReportRateRowResponse> rows = new ArrayList<>();
        for (Map.Entry<BigDecimal, BigDecimal[]> entry : byRate.entrySet()) {
            rows.add(TaxReportRateRowResponse.builder()
                    .taxRatePercent(entry.getKey())
                    .taxableAmount(entry.getValue()[0])
                    .taxAmount(entry.getValue()[1])
                    .build());
        }
        return rows;
    }
}
