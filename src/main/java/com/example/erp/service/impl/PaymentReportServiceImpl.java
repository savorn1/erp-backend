package com.example.erp.service.impl;

import com.example.erp.dto.CollectionByCustomerResponse;
import com.example.erp.dto.CollectionByCustomerRowResponse;
import com.example.erp.dto.CollectionBySalespersonResponse;
import com.example.erp.dto.CollectionBySalespersonRowResponse;
import com.example.erp.dto.PaymentByBranchResponse;
import com.example.erp.dto.PaymentByBranchRowResponse;
import com.example.erp.dto.PaymentByMethodResponse;
import com.example.erp.dto.PaymentByMethodRowResponse;
import com.example.erp.dto.PaymentDetailResponse;
import com.example.erp.dto.PaymentDetailRowResponse;
import com.example.erp.dto.PaymentReconciliationResponse;
import com.example.erp.dto.PaymentReconciliationRowResponse;
import com.example.erp.dto.PaymentReportFilterRequest;
import com.example.erp.dto.PaymentSummaryResponse;
import com.example.erp.dto.RefundReportResponse;
import com.example.erp.dto.RefundRowResponse;
import com.example.erp.dto.TransferReportResponse;
import com.example.erp.dto.TransferRowResponse;
import com.example.erp.entity.BankAccount;
import com.example.erp.entity.BankTransaction;
import com.example.erp.entity.BankTransactionType;
import com.example.erp.entity.Branch;
import com.example.erp.entity.Customer;
import com.example.erp.entity.Invoice;
import com.example.erp.entity.Payment;
import com.example.erp.entity.PaymentAllocation;
import com.example.erp.entity.PaymentMethod;
import com.example.erp.entity.PaymentType;
import com.example.erp.entity.SalesOrder;
import com.example.erp.entity.Supplier;
import com.example.erp.entity.SupplierPayment;
import com.example.erp.entity.User;
import com.example.erp.repository.BankAccountRepository;
import com.example.erp.repository.BankTransactionRepository;
import com.example.erp.repository.BranchRepository;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.InvoiceRepository;
import com.example.erp.repository.PaymentAllocationRepository;
import com.example.erp.repository.PaymentRepository;
import com.example.erp.repository.SalesOrderRepository;
import com.example.erp.repository.SupplierPaymentRepository;
import com.example.erp.repository.SupplierRepository;
import com.example.erp.repository.UserRepository;
import com.example.erp.service.PaymentReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentReportServiceImpl implements PaymentReportService {

    private static final String CUSTOMER = "CUSTOMER";
    private static final String SUPPLIER = "SUPPLIER";
    private static final String UNALLOCATED = "Unallocated";
    private static final String UNASSIGNED = "Unassigned";

    private final PaymentRepository paymentRepository;
    private final SupplierPaymentRepository supplierPaymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final InvoiceRepository invoiceRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BankTransactionRepository bankTransactionRepository;

    @Override
    @Transactional(readOnly = true)
    public PaymentSummaryResponse summary(PaymentReportFilterRequest filter) {
        List<Payment> customer = customerPayments(filter);
        List<SupplierPayment> supplier = supplierPayments(filter);

        BigDecimal customerReceived = sumWhere(customer, p -> p.getType() == PaymentType.PAYMENT, Payment::getAmount);
        BigDecimal customerRefunded = sumWhere(customer, p -> p.getType() == PaymentType.REFUND, Payment::getAmount);
        BigDecimal supplierPaid = sumWhere(supplier, p -> p.getType() == PaymentType.PAYMENT, SupplierPayment::getAmount);
        BigDecimal supplierRefunded = sumWhere(supplier, p -> p.getType() == PaymentType.REFUND, SupplierPayment::getAmount);
        BigDecimal customerNet = customerReceived.subtract(customerRefunded);
        BigDecimal supplierNet = supplierPaid.subtract(supplierRefunded);

        return PaymentSummaryResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .customerPaymentCount(customer.size())
                .customerReceived(customerReceived)
                .customerRefunded(customerRefunded)
                .customerNet(customerNet)
                .supplierPaymentCount(supplier.size())
                .supplierPaid(supplierPaid)
                .supplierRefunded(supplierRefunded)
                .supplierNet(supplierNet)
                .netCashFlow(customerNet.subtract(supplierNet))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDetailResponse detail(PaymentReportFilterRequest filter) {
        List<Payment> customer = includeCustomer(filter) ? customerPayments(filter) : List.of();
        List<SupplierPayment> supplier = includeSupplier(filter) ? supplierPayments(filter) : List.of();
        Map<Long, Customer> customers = customersById(customer.stream().map(Payment::getCustomerId).toList());
        Map<Long, Supplier> suppliers = suppliersById(supplier.stream().map(SupplierPayment::getSupplierId).toList());

        List<PaymentDetailRowResponse> rows = new ArrayList<>();
        BigDecimal totalReceived = BigDecimal.ZERO;
        for (Payment p : customer) {
            Customer c = customers.get(p.getCustomerId());
            rows.add(PaymentDetailRowResponse.builder()
                    .party(CUSTOMER)
                    .paymentId(p.getId())
                    .paymentNumber(p.getPaymentNumber())
                    .paymentDate(p.getPaymentDate())
                    .partyId(p.getCustomerId())
                    .partyName(c == null ? null : c.getName())
                    .type(p.getType().name())
                    .method(p.getMethod().name())
                    .amount(p.getAmount())
                    .reference(p.getReference())
                    .createdBy(p.getCreatedBy())
                    .build());
            totalReceived = totalReceived.add(signed(p.getType(), p.getAmount()));
        }
        BigDecimal totalPaid = BigDecimal.ZERO;
        for (SupplierPayment p : supplier) {
            Supplier s = suppliers.get(p.getSupplierId());
            rows.add(PaymentDetailRowResponse.builder()
                    .party(SUPPLIER)
                    .paymentId(p.getId())
                    .paymentNumber(p.getPaymentNumber())
                    .paymentDate(p.getPaymentDate())
                    .partyId(p.getSupplierId())
                    .partyName(s == null ? null : s.getName())
                    .type(p.getType().name())
                    .method(p.getMethod().name())
                    .amount(p.getAmount())
                    .reference(p.getReference())
                    .createdBy(p.getCreatedBy())
                    .build());
            totalPaid = totalPaid.add(signed(p.getType(), p.getAmount()));
        }
        rows.sort(Comparator.comparing(PaymentDetailRowResponse::getPaymentDate).reversed());

        return PaymentDetailResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalReceived(totalReceived)
                .totalPaid(totalPaid)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentByMethodResponse byMethod(PaymentReportFilterRequest filter) {
        List<Payment> customer = customerPayments(filter);
        List<SupplierPayment> supplier = supplierPayments(filter);

        Map<PaymentMethod, long[]> counts = new HashMap<>();      // [customer, supplier]
        Map<PaymentMethod, BigDecimal[]> nets = new HashMap<>();  // [customer, supplier]
        for (Payment p : customer) {
            counts.computeIfAbsent(p.getMethod(), m -> new long[2])[0]++;
            BigDecimal[] n = nets.computeIfAbsent(p.getMethod(), m -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            n[0] = n[0].add(signed(p.getType(), p.getAmount()));
        }
        for (SupplierPayment p : supplier) {
            counts.computeIfAbsent(p.getMethod(), m -> new long[2])[1]++;
            BigDecimal[] n = nets.computeIfAbsent(p.getMethod(), m -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            n[1] = n[1].add(signed(p.getType(), p.getAmount()));
        }

        List<PaymentByMethodRowResponse> rows = new ArrayList<>();
        BigDecimal totalCustomer = BigDecimal.ZERO;
        BigDecimal totalSupplier = BigDecimal.ZERO;
        for (PaymentMethod method : PaymentMethod.values()) {
            if (!counts.containsKey(method)) continue;
            BigDecimal[] n = nets.get(method);
            rows.add(PaymentByMethodRowResponse.builder()
                    .method(method.name())
                    .customerCount(counts.get(method)[0])
                    .customerNet(n[0])
                    .supplierCount(counts.get(method)[1])
                    .supplierNet(n[1])
                    .build());
            totalCustomer = totalCustomer.add(n[0]);
            totalSupplier = totalSupplier.add(n[1]);
        }

        return PaymentByMethodResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalCustomerNet(totalCustomer)
                .totalSupplierNet(totalSupplier)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentByBranchResponse byBranch(PaymentReportFilterRequest filter) {
        List<Payment> customer = customerPayments(filter);
        List<SupplierPayment> supplier = supplierPayments(filter);

        Set<String> usernames = new HashSet<>();
        customer.forEach(p -> { if (p.getCreatedBy() != null) usernames.add(p.getCreatedBy()); });
        supplier.forEach(p -> { if (p.getCreatedBy() != null) usernames.add(p.getCreatedBy()); });
        Map<String, Long> branchByUsername = new HashMap<>();
        for (String username : usernames) {
            userRepository.findByUsername(username).map(User::getBranchId).ifPresent(b -> branchByUsername.put(username, b));
        }

        // Keyed by branchId; a null key collects payments whose recording
        // user has no branch (or is unknown).
        Map<Long, long[]> counts = new HashMap<>();
        Map<Long, BigDecimal[]> nets = new HashMap<>();
        for (Payment p : customer) {
            Long branchId = p.getCreatedBy() == null ? null : branchByUsername.get(p.getCreatedBy());
            counts.computeIfAbsent(branchId, b -> new long[2])[0]++;
            BigDecimal[] n = nets.computeIfAbsent(branchId, b -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            n[0] = n[0].add(signed(p.getType(), p.getAmount()));
        }
        for (SupplierPayment p : supplier) {
            Long branchId = p.getCreatedBy() == null ? null : branchByUsername.get(p.getCreatedBy());
            counts.computeIfAbsent(branchId, b -> new long[2])[1]++;
            BigDecimal[] n = nets.computeIfAbsent(branchId, b -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            n[1] = n[1].add(signed(p.getType(), p.getAmount()));
        }

        Map<Long, Branch> branches = branchRepository.findAllById(
                counts.keySet().stream().filter(Objects::nonNull).toList()
        ).stream().collect(Collectors.toMap(Branch::getId, b -> b));

        List<PaymentByBranchRowResponse> rows = new ArrayList<>();
        for (Map.Entry<Long, long[]> entry : counts.entrySet()) {
            Branch branch = entry.getKey() == null ? null : branches.get(entry.getKey());
            BigDecimal[] n = nets.get(entry.getKey());
            rows.add(PaymentByBranchRowResponse.builder()
                    .branchId(entry.getKey())
                    .branchName(branch == null ? UNASSIGNED : branch.getName())
                    .customerCount(entry.getValue()[0])
                    .customerNet(n[0])
                    .supplierCount(entry.getValue()[1])
                    .supplierNet(n[1])
                    .build());
        }
        rows.sort(Comparator.comparing(PaymentByBranchRowResponse::getCustomerNet).reversed());

        return PaymentByBranchResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RefundReportResponse refunds(PaymentReportFilterRequest filter) {
        List<Payment> customer = includeCustomer(filter)
                ? customerPayments(filter).stream().filter(p -> p.getType() == PaymentType.REFUND).toList()
                : List.of();
        List<SupplierPayment> supplier = includeSupplier(filter)
                ? supplierPayments(filter).stream().filter(p -> p.getType() == PaymentType.REFUND).toList()
                : List.of();
        Map<Long, Customer> customers = customersById(customer.stream().map(Payment::getCustomerId).toList());
        Map<Long, Supplier> suppliers = suppliersById(supplier.stream().map(SupplierPayment::getSupplierId).toList());
        Map<Long, Payment> originalCustomer = paymentRepository.findAllById(
                customer.stream().map(Payment::getRelatedPaymentId).filter(Objects::nonNull).toList()
        ).stream().collect(Collectors.toMap(Payment::getId, p -> p));
        Map<Long, SupplierPayment> originalSupplier = supplierPaymentRepository.findAllById(
                supplier.stream().map(SupplierPayment::getRelatedPaymentId).filter(Objects::nonNull).toList()
        ).stream().collect(Collectors.toMap(SupplierPayment::getId, p -> p));

        List<RefundRowResponse> rows = new ArrayList<>();
        BigDecimal totalCustomer = BigDecimal.ZERO;
        for (Payment p : customer) {
            Customer c = customers.get(p.getCustomerId());
            Payment original = p.getRelatedPaymentId() == null ? null : originalCustomer.get(p.getRelatedPaymentId());
            rows.add(RefundRowResponse.builder()
                    .party(CUSTOMER)
                    .refundId(p.getId())
                    .refundNumber(p.getPaymentNumber())
                    .refundDate(p.getPaymentDate())
                    .partyId(p.getCustomerId())
                    .partyName(c == null ? null : c.getName())
                    .method(p.getMethod().name())
                    .amount(p.getAmount())
                    .originalPaymentNumber(original == null ? null : original.getPaymentNumber())
                    .notes(p.getNotes())
                    .build());
            totalCustomer = totalCustomer.add(p.getAmount());
        }
        BigDecimal totalSupplier = BigDecimal.ZERO;
        for (SupplierPayment p : supplier) {
            Supplier s = suppliers.get(p.getSupplierId());
            SupplierPayment original = p.getRelatedPaymentId() == null ? null : originalSupplier.get(p.getRelatedPaymentId());
            rows.add(RefundRowResponse.builder()
                    .party(SUPPLIER)
                    .refundId(p.getId())
                    .refundNumber(p.getPaymentNumber())
                    .refundDate(p.getPaymentDate())
                    .partyId(p.getSupplierId())
                    .partyName(s == null ? null : s.getName())
                    .method(p.getMethod().name())
                    .amount(p.getAmount())
                    .originalPaymentNumber(original == null ? null : original.getPaymentNumber())
                    .notes(p.getNotes())
                    .build());
            totalSupplier = totalSupplier.add(p.getAmount());
        }
        rows.sort(Comparator.comparing(RefundRowResponse::getRefundDate).reversed());

        return RefundReportResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalCustomerRefunds(totalCustomer)
                .totalSupplierRefunds(totalSupplier)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TransferReportResponse transfers(PaymentReportFilterRequest filter) {
        List<BankTransaction> outs = bankTransactions(filter).stream()
                .filter(t -> t.getType() == BankTransactionType.TRANSFER_OUT)
                .sorted(Comparator.comparing(BankTransaction::getTransactionDate).reversed())
                .toList();
        Map<Long, BankTransaction> ins = bankTransactionRepository.findAllById(
                outs.stream().map(BankTransaction::getRelatedTransactionId).filter(Objects::nonNull).toList()
        ).stream().collect(Collectors.toMap(BankTransaction::getId, t -> t));

        Set<Long> accountIds = new HashSet<>();
        outs.forEach(t -> accountIds.add(t.getBankAccountId()));
        ins.values().forEach(t -> accountIds.add(t.getBankAccountId()));
        Map<Long, BankAccount> accounts = bankAccountRepository.findAllById(accountIds).stream()
                .collect(Collectors.toMap(BankAccount::getId, a -> a));

        List<TransferRowResponse> rows = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (BankTransaction out : outs) {
            BankTransaction in = out.getRelatedTransactionId() == null ? null : ins.get(out.getRelatedTransactionId());
            BankAccount from = accounts.get(out.getBankAccountId());
            BankAccount to = in == null ? null : accounts.get(in.getBankAccountId());
            rows.add(TransferRowResponse.builder()
                    .transactionId(out.getId())
                    .transactionNumber(out.getTransactionNumber())
                    .transactionDate(out.getTransactionDate())
                    .fromAccountId(out.getBankAccountId())
                    .fromAccountName(from == null ? null : from.getName())
                    .toAccountId(in == null ? null : in.getBankAccountId())
                    .toAccountName(to == null ? null : to.getName())
                    .amount(out.getAmount())
                    .reference(out.getReference())
                    .description(out.getDescription())
                    .build());
            total = total.add(out.getAmount());
        }

        return TransferReportResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalTransferred(total)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionByCustomerResponse collectionByCustomer(PaymentReportFilterRequest filter) {
        List<Payment> payments = customerPayments(filter);
        Map<Long, List<Payment>> byCustomer = payments.stream().collect(Collectors.groupingBy(Payment::getCustomerId));
        Map<Long, Customer> customers = customersById(byCustomer.keySet().stream().toList());

        List<CollectionByCustomerRowResponse> rows = new ArrayList<>();
        BigDecimal totalNet = BigDecimal.ZERO;
        for (Map.Entry<Long, List<Payment>> entry : byCustomer.entrySet()) {
            Customer c = customers.get(entry.getKey());
            BigDecimal received = sumWhere(entry.getValue(), p -> p.getType() == PaymentType.PAYMENT, Payment::getAmount);
            BigDecimal refunded = sumWhere(entry.getValue(), p -> p.getType() == PaymentType.REFUND, Payment::getAmount);
            BigDecimal net = received.subtract(refunded);
            rows.add(CollectionByCustomerRowResponse.builder()
                    .customerId(entry.getKey())
                    .customerName(c == null ? null : c.getName())
                    .paymentCount(entry.getValue().size())
                    .received(received)
                    .refunded(refunded)
                    .net(net)
                    .build());
            totalNet = totalNet.add(net);
        }
        rows.sort(Comparator.comparing(CollectionByCustomerRowResponse::getNet).reversed());

        return CollectionByCustomerResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalNet(totalNet)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionBySalespersonResponse collectionBySalesperson(PaymentReportFilterRequest filter) {
        List<Payment> payments = customerPayments(filter);

        // Allocation amounts are signed (negative for refunds), so summing
        // them per invoice gives the net collected — see PaymentAllocation.
        List<PaymentAllocation> allocations = new ArrayList<>();
        Map<Long, BigDecimal> allocatedByPayment = new HashMap<>();
        for (Payment p : payments) {
            List<PaymentAllocation> forPayment = allocationRepository.findByPaymentId(p.getId());
            allocations.addAll(forPayment);
            allocatedByPayment.put(p.getId(), forPayment.stream().map(PaymentAllocation::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        }

        Map<Long, Invoice> invoices = invoiceRepository.findAllById(
                allocations.stream().map(PaymentAllocation::getInvoiceId).distinct().toList()
        ).stream().collect(Collectors.toMap(Invoice::getId, i -> i));
        Map<Long, SalesOrder> orders = salesOrderRepository.findAllById(
                invoices.values().stream().map(Invoice::getSalesOrderId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(SalesOrder::getId, o -> o));

        Map<String, BigDecimal> amountBySalesperson = new HashMap<>();
        Map<String, Set<Long>> invoicesBySalesperson = new HashMap<>();
        for (PaymentAllocation a : allocations) {
            Invoice invoice = invoices.get(a.getInvoiceId());
            String salesperson = UNALLOCATED;
            if (invoice != null) {
                SalesOrder order = invoice.getSalesOrderId() == null ? null : orders.get(invoice.getSalesOrderId());
                String creator = order != null ? order.getCreatedBy() : invoice.getCreatedBy();
                salesperson = creator == null ? UNASSIGNED : creator;
            }
            amountBySalesperson.merge(salesperson, a.getAmount(), BigDecimal::add);
            invoicesBySalesperson.computeIfAbsent(salesperson, s -> new HashSet<>()).add(a.getInvoiceId());
        }
        // Whatever part of a payment isn't applied to any invoice yet.
        BigDecimal unallocated = BigDecimal.ZERO;
        for (Payment p : payments) {
            unallocated = unallocated.add(signed(p.getType(), p.getAmount()).subtract(allocatedByPayment.getOrDefault(p.getId(), BigDecimal.ZERO)));
        }
        if (unallocated.signum() != 0) {
            amountBySalesperson.merge(UNALLOCATED, unallocated, BigDecimal::add);
            invoicesBySalesperson.computeIfAbsent(UNALLOCATED, s -> new HashSet<>());
        }

        List<CollectionBySalespersonRowResponse> rows = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : amountBySalesperson.entrySet()) {
            rows.add(CollectionBySalespersonRowResponse.builder()
                    .salesperson(entry.getKey())
                    .invoiceCount(invoicesBySalesperson.getOrDefault(entry.getKey(), Set.of()).size())
                    .allocatedAmount(entry.getValue())
                    .build());
            total = total.add(entry.getValue());
        }
        rows.sort(Comparator.comparing(CollectionBySalespersonRowResponse::getAllocatedAmount).reversed());

        return CollectionBySalespersonResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalAllocated(total)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentReconciliationResponse reconciliation(PaymentReportFilterRequest filter) {
        List<BankTransaction> transactions = bankTransactions(filter);
        Map<Long, List<BankTransaction>> byAccount = transactions.stream().collect(Collectors.groupingBy(BankTransaction::getBankAccountId));
        Map<Long, BankAccount> accounts = bankAccountRepository.findAllById(byAccount.keySet()).stream()
                .collect(Collectors.toMap(BankAccount::getId, a -> a));

        List<PaymentReconciliationRowResponse> rows = new ArrayList<>();
        for (Map.Entry<Long, List<BankTransaction>> entry : byAccount.entrySet()) {
            BankAccount account = accounts.get(entry.getKey());
            List<BankTransaction> txs = entry.getValue();
            rows.add(PaymentReconciliationRowResponse.builder()
                    .bankAccountId(entry.getKey())
                    .bankAccountName(account == null ? null : account.getName())
                    .accountType(account == null ? null : account.getType().name())
                    .deposits(sumWhere(txs, t -> t.getType() == BankTransactionType.DEPOSIT || t.getType() == BankTransactionType.TRANSFER_IN, BankTransaction::getAmount))
                    .withdrawals(sumWhere(txs, t -> t.getType() == BankTransactionType.WITHDRAWAL || t.getType() == BankTransactionType.TRANSFER_OUT, BankTransaction::getAmount))
                    .reconciledCount(txs.stream().filter(BankTransaction::isReconciled).count())
                    .reconciledAmount(sumWhere(txs, BankTransaction::isReconciled, BankTransaction::getAmount))
                    .unreconciledCount(txs.stream().filter(t -> !t.isReconciled()).count())
                    .unreconciledAmount(sumWhere(txs, t -> !t.isReconciled(), BankTransaction::getAmount))
                    .build());
        }
        rows.sort(Comparator.comparing(PaymentReconciliationRowResponse::getUnreconciledAmount).reversed());

        BigDecimal customerBankReceipts = customerPayments(filter).stream()
                .filter(p -> isBankMethod(p.getMethod()))
                .map(p -> signed(p.getType(), p.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal supplierBankPayments = supplierPayments(filter).stream()
                .filter(p -> isBankMethod(p.getMethod()))
                .map(p -> signed(p.getType(), p.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDeposits = sumWhere(transactions, t -> t.getType() == BankTransactionType.DEPOSIT, BankTransaction::getAmount);
        BigDecimal totalWithdrawals = sumWhere(transactions, t -> t.getType() == BankTransactionType.WITHDRAWAL, BankTransaction::getAmount);

        return PaymentReconciliationResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .customerBankReceipts(customerBankReceipts)
                .totalDeposits(totalDeposits)
                .receiptsVariance(totalDeposits.subtract(customerBankReceipts))
                .supplierBankPayments(supplierBankPayments)
                .totalWithdrawals(totalWithdrawals)
                .paymentsVariance(totalWithdrawals.subtract(supplierBankPayments))
                .build();
    }

    // ── Ledger fetches ─────────────────────────────────────────────────────

    private List<Payment> customerPayments(PaymentReportFilterRequest filter) {
        List<Specification<Payment>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getCustomerId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("customerId"), filter.getCustomerId()));
        if (filter.getDateFrom() != null) conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("paymentDate"), filter.getDateFrom()));
        if (filter.getDateTo() != null) conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("paymentDate"), filter.getDateTo()));
        if (filter.getMethod() != null) conditions.add((root, query, cb) -> cb.equal(root.get("method"), filter.getMethod()));
        if (filter.getType() != null) conditions.add((root, query, cb) -> cb.equal(root.get("type"), filter.getType()));
        return paymentRepository.findAll(Specification.allOf(conditions));
    }

    private List<SupplierPayment> supplierPayments(PaymentReportFilterRequest filter) {
        List<Specification<SupplierPayment>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getSupplierId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("supplierId"), filter.getSupplierId()));
        if (filter.getDateFrom() != null) conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("paymentDate"), filter.getDateFrom()));
        if (filter.getDateTo() != null) conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("paymentDate"), filter.getDateTo()));
        if (filter.getMethod() != null) conditions.add((root, query, cb) -> cb.equal(root.get("method"), filter.getMethod()));
        if (filter.getType() != null) conditions.add((root, query, cb) -> cb.equal(root.get("type"), filter.getType()));
        return supplierPaymentRepository.findAll(Specification.allOf(conditions));
    }

    private List<BankTransaction> bankTransactions(PaymentReportFilterRequest filter) {
        List<Specification<BankTransaction>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getDateFrom() != null) conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("transactionDate"), filter.getDateFrom()));
        if (filter.getDateTo() != null) conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("transactionDate"), filter.getDateTo()));
        return bankTransactionRepository.findAll(Specification.allOf(conditions));
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private boolean includeCustomer(PaymentReportFilterRequest filter) {
        return filter.getParty() == null || CUSTOMER.equalsIgnoreCase(filter.getParty());
    }

    private boolean includeSupplier(PaymentReportFilterRequest filter) {
        return filter.getParty() == null || SUPPLIER.equalsIgnoreCase(filter.getParty());
    }

    private boolean isBankMethod(PaymentMethod method) {
        return method == PaymentMethod.BANK_TRANSFER || method == PaymentMethod.PAYMENT_GATEWAY;
    }

    private BigDecimal signed(PaymentType type, BigDecimal amount) {
        return type == PaymentType.REFUND ? amount.negate() : amount;
    }

    private <T> BigDecimal sumWhere(List<T> items, java.util.function.Predicate<T> where, java.util.function.Function<T, BigDecimal> amount) {
        return items.stream().filter(where).map(amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<Long, Customer> customersById(List<Long> ids) {
        return customerRepository.findAllById(ids.stream().distinct().toList()).stream().collect(Collectors.toMap(Customer::getId, c -> c));
    }

    private Map<Long, Supplier> suppliersById(List<Long> ids) {
        return supplierRepository.findAllById(ids.stream().distinct().toList()).stream().collect(Collectors.toMap(Supplier::getId, s -> s));
    }
}
