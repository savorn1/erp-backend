package com.example.erp.service.impl;

import com.example.erp.dto.BankAccountFilterRequest;
import com.example.erp.dto.BankAccountRequest;
import com.example.erp.dto.BankAccountResponse;
import com.example.erp.dto.BankReconciliationResponse;
import com.example.erp.dto.BankTransactionFilterRequest;
import com.example.erp.dto.BankTransactionRequest;
import com.example.erp.dto.BankTransactionResponse;
import com.example.erp.dto.BankTransferRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ReconcileTransactionsRequest;
import com.example.erp.entity.Account;
import com.example.erp.entity.BankAccount;
import com.example.erp.entity.BankTransaction;
import com.example.erp.entity.BankTransactionType;
import com.example.erp.entity.Company;
import com.example.erp.exception.AppException;
import com.example.erp.repository.AccountRepository;
import com.example.erp.repository.BankAccountRepository;
import com.example.erp.repository.BankTransactionRepository;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.service.BankAccountService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final CompanyRepository companyRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BankAccountResponse> list(BankAccountFilterRequest filter) {
        List<Specification<BankAccount>> conditions = new ArrayList<>();
        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            String like = "%" + filter.getSearch().toLowerCase() + "%";
            conditions.add((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(cb.coalesce(root.get("bankName"), "")), like),
                    cb.like(cb.lower(cb.coalesce(root.get("accountNumber"), "")), like)));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getType() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("type"), filter.getType()));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        Specification<BankAccount> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<BankAccount> page = bankAccountRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    public BankAccountResponse get(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public BankAccountResponse create(BankAccountRequest request) {
        requireCompany(request.getCompanyId());
        requireAccount(request.getAccountId(), request.getCompanyId());

        BankAccount bankAccount = BankAccount.builder()
                .companyId(request.getCompanyId())
                .accountId(request.getAccountId())
                .name(request.getName())
                .type(request.getType())
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .currency(request.getCurrency())
                .openingBalance(request.getOpeningBalance())
                .currentBalance(request.getOpeningBalance())
                .active(request.isActive())
                .build();
        bankAccountRepository.save(bankAccount);
        return toResponse(bankAccount);
    }

    @Override
    @Transactional
    public BankAccountResponse update(Long id, BankAccountRequest request) {
        BankAccount bankAccount = find(id);
        requireCompany(request.getCompanyId());
        requireAccount(request.getAccountId(), request.getCompanyId());

        bankAccount.setCompanyId(request.getCompanyId());
        bankAccount.setAccountId(request.getAccountId());
        bankAccount.setName(request.getName());
        bankAccount.setType(request.getType());
        bankAccount.setBankName(request.getBankName());
        bankAccount.setAccountNumber(request.getAccountNumber());
        bankAccount.setCurrency(request.getCurrency());
        bankAccount.setOpeningBalance(request.getOpeningBalance());
        bankAccount.setActive(request.isActive());
        bankAccountRepository.save(bankAccount);
        return toResponse(bankAccount);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        find(id);
        if (bankTransactionRepository.existsByBankAccountId(id)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delete a bank account that already has transactions");
        }
        bankAccountRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BankTransactionResponse> listTransactions(BankTransactionFilterRequest filter) {
        List<Specification<BankTransaction>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getBankAccountId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("bankAccountId"), filter.getBankAccountId()));
        }
        if (filter.getType() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("type"), filter.getType()));
        }
        if (filter.getReconciled() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("reconciled"), filter.getReconciled()));
        }
        Specification<BankTransaction> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<BankTransaction> page = bankTransactionRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toTransactionResponse));
    }

    @Override
    @Transactional
    public BankTransactionResponse deposit(Long bankAccountId, BankTransactionRequest request, String actingUsername) {
        BankAccount bankAccount = find(bankAccountId);
        BankTransaction transaction = BankTransaction.builder()
                .companyId(bankAccount.getCompanyId())
                .bankAccountId(bankAccount.getId())
                .transactionDate(request.getTransactionDate())
                .type(BankTransactionType.DEPOSIT)
                .amount(request.getAmount())
                .reference(request.getReference())
                .description(request.getDescription())
                .createdBy(actingUsername)
                .build();
        bankTransactionRepository.save(transaction);
        transaction.setTransactionNumber("BT-" + String.format("%06d", transaction.getId()));
        bankTransactionRepository.save(transaction);

        bankAccount.setCurrentBalance(bankAccount.getCurrentBalance().add(request.getAmount()));
        bankAccountRepository.save(bankAccount);

        return toTransactionResponse(transaction);
    }

    @Override
    @Transactional
    public BankTransactionResponse withdraw(Long bankAccountId, BankTransactionRequest request, String actingUsername) {
        BankAccount bankAccount = find(bankAccountId);
        if (request.getAmount().compareTo(bankAccount.getCurrentBalance()) > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Withdrawal exceeds the account's current balance (" + bankAccount.getCurrentBalance() + ")");
        }
        BankTransaction transaction = BankTransaction.builder()
                .companyId(bankAccount.getCompanyId())
                .bankAccountId(bankAccount.getId())
                .transactionDate(request.getTransactionDate())
                .type(BankTransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .reference(request.getReference())
                .description(request.getDescription())
                .createdBy(actingUsername)
                .build();
        bankTransactionRepository.save(transaction);
        transaction.setTransactionNumber("BT-" + String.format("%06d", transaction.getId()));
        bankTransactionRepository.save(transaction);

        bankAccount.setCurrentBalance(bankAccount.getCurrentBalance().subtract(request.getAmount()));
        bankAccountRepository.save(bankAccount);

        return toTransactionResponse(transaction);
    }

    @Override
    @Transactional
    public BankTransactionResponse transfer(Long fromBankAccountId, BankTransferRequest request, String actingUsername) {
        if (fromBankAccountId.equals(request.getToBankAccountId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot transfer a bank account to itself");
        }
        BankAccount from = find(fromBankAccountId);
        BankAccount to = find(request.getToBankAccountId());
        if (!from.getCompanyId().equals(to.getCompanyId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Both accounts must belong to the same company");
        }
        if (request.getAmount().compareTo(from.getCurrentBalance()) > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Transfer exceeds the source account's current balance (" + from.getCurrentBalance() + ")");
        }

        BankTransaction outTxn = BankTransaction.builder()
                .companyId(from.getCompanyId())
                .bankAccountId(from.getId())
                .transactionDate(request.getTransactionDate())
                .type(BankTransactionType.TRANSFER_OUT)
                .amount(request.getAmount())
                .reference(request.getReference())
                .description(request.getDescription() != null ? request.getDescription() : "Transfer to " + to.getName())
                .createdBy(actingUsername)
                .build();
        bankTransactionRepository.save(outTxn);
        outTxn.setTransactionNumber("BT-" + String.format("%06d", outTxn.getId()));

        BankTransaction inTxn = BankTransaction.builder()
                .companyId(to.getCompanyId())
                .bankAccountId(to.getId())
                .transactionDate(request.getTransactionDate())
                .type(BankTransactionType.TRANSFER_IN)
                .amount(request.getAmount())
                .reference(request.getReference())
                .description(request.getDescription() != null ? request.getDescription() : "Transfer from " + from.getName())
                .relatedTransactionId(outTxn.getId())
                .createdBy(actingUsername)
                .build();
        bankTransactionRepository.save(inTxn);
        inTxn.setTransactionNumber("BT-" + String.format("%06d", inTxn.getId()));
        bankTransactionRepository.save(inTxn);

        outTxn.setRelatedTransactionId(inTxn.getId());
        bankTransactionRepository.save(outTxn);

        from.setCurrentBalance(from.getCurrentBalance().subtract(request.getAmount()));
        to.setCurrentBalance(to.getCurrentBalance().add(request.getAmount()));
        bankAccountRepository.save(from);
        bankAccountRepository.save(to);

        return toTransactionResponse(outTxn);
    }

    @Override
    @Transactional
    public BankReconciliationResponse reconcile(Long bankAccountId, ReconcileTransactionsRequest request) {
        BankAccount bankAccount = find(bankAccountId);
        List<BankTransaction> toReconcile = new ArrayList<>();
        for (Long transactionId : request.getTransactionIds()) {
            BankTransaction transaction = bankTransactionRepository.findById(transactionId)
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Transaction not found with id: " + transactionId));
            if (!transaction.getBankAccountId().equals(bankAccountId)) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Transaction " + transactionId + " does not belong to this bank account");
            }
            transaction.setReconciled(true);
            transaction.setReconciledDate(request.getStatementDate());
            bankTransactionRepository.save(transaction);
            toReconcile.add(transaction);
        }

        BigDecimal reconciledBalance = bankAccount.getOpeningBalance().add(bankTransactionRepository.findByBankAccountId(bankAccountId).stream()
                .filter(BankTransaction::isReconciled)
                .map(this::signedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        return BankReconciliationResponse.builder()
                .statementDate(request.getStatementDate())
                .statementBalance(request.getStatementBalance())
                .reconciledBalance(reconciledBalance)
                .difference(request.getStatementBalance().subtract(reconciledBalance))
                .reconciledTransactions(toReconcile.stream().map(this::toTransactionResponse).toList())
                .build();
    }

    private BigDecimal signedAmount(BankTransaction transaction) {
        boolean isInflow = transaction.getType() == BankTransactionType.DEPOSIT || transaction.getType() == BankTransactionType.TRANSFER_IN;
        return isInflow ? transaction.getAmount() : transaction.getAmount().negate();
    }

    private void requireCompany(Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private void requireAccount(Long accountId, Long companyId) {
        if (accountId == null) return;
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Account not found with id: " + accountId));
        if (!account.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Account does not belong to the selected company");
        }
    }

    private BankAccount find(Long id) {
        return bankAccountRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Bank account not found with id: " + id));
    }

    private BankAccountResponse toResponse(BankAccount bankAccount) {
        String companyName = companyRepository.findById(bankAccount.getCompanyId()).map(Company::getName).orElse(null);
        Account account = bankAccount.getAccountId() == null ? null : accountRepository.findById(bankAccount.getAccountId()).orElse(null);
        return BankAccountResponse.builder()
                .id(bankAccount.getId())
                .companyId(bankAccount.getCompanyId())
                .companyName(companyName)
                .accountId(bankAccount.getAccountId())
                .accountCode(account == null ? null : account.getAccountCode())
                .accountName(account == null ? null : account.getName())
                .name(bankAccount.getName())
                .type(bankAccount.getType().name())
                .bankName(bankAccount.getBankName())
                .accountNumber(bankAccount.getAccountNumber())
                .currency(bankAccount.getCurrency())
                .openingBalance(bankAccount.getOpeningBalance())
                .currentBalance(bankAccount.getCurrentBalance())
                .active(bankAccount.isActive())
                .build();
    }

    private BankTransactionResponse toTransactionResponse(BankTransaction transaction) {
        String bankAccountName = bankAccountRepository.findById(transaction.getBankAccountId()).map(BankAccount::getName).orElse(null);
        String relatedTransactionNumber = null;
        String relatedBankAccountName = null;
        if (transaction.getRelatedTransactionId() != null) {
            BankTransaction related = bankTransactionRepository.findById(transaction.getRelatedTransactionId()).orElse(null);
            if (related != null) {
                relatedTransactionNumber = related.getTransactionNumber();
                relatedBankAccountName = bankAccountRepository.findById(related.getBankAccountId()).map(BankAccount::getName).orElse(null);
            }
        }
        return BankTransactionResponse.builder()
                .id(transaction.getId())
                .companyId(transaction.getCompanyId())
                .bankAccountId(transaction.getBankAccountId())
                .bankAccountName(bankAccountName)
                .transactionNumber(transaction.getTransactionNumber())
                .transactionDate(transaction.getTransactionDate())
                .type(transaction.getType().name())
                .amount(transaction.getAmount())
                .reference(transaction.getReference())
                .description(transaction.getDescription())
                .relatedTransactionId(transaction.getRelatedTransactionId())
                .relatedTransactionNumber(relatedTransactionNumber)
                .relatedBankAccountName(relatedBankAccountName)
                .reconciled(transaction.isReconciled())
                .reconciledDate(transaction.getReconciledDate())
                .createdBy(transaction.getCreatedBy())
                .build();
    }
}
