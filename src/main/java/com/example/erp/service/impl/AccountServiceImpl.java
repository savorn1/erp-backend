package com.example.erp.service.impl;

import com.example.erp.dto.AccountFilterRequest;
import com.example.erp.dto.AccountRequest;
import com.example.erp.dto.AccountResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.entity.Account;
import com.example.erp.entity.AccountType;
import com.example.erp.entity.Company;
import com.example.erp.exception.AppException;
import com.example.erp.repository.AccountRepository;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.service.AccountService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AccountResponse> list(AccountFilterRequest filter) {
        List<Specification<Account>> conditions = new ArrayList<>();
        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            String like = "%" + filter.getSearch().toLowerCase() + "%";
            conditions.add((root, query, cb) ->
                    cb.or(cb.like(cb.lower(root.get("accountCode")), like), cb.like(cb.lower(root.get("name")), like)));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getAccountType() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("accountType"), filter.getAccountType()));
        }
        if (filter.getParentAccountId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("parentAccountId"), filter.getParentAccountId()));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        Specification<Account> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<Account> page = accountRepository.findAll(spec, pageable);

        // Batch-load every account in the company so parent lookups and
        // hasChildren don't turn into an N+1 query per row — a chart of
        // accounts is small enough for this to stay a single extra query.
        List<Account> companyAccounts = filter.getCompanyId() != null
                ? accountRepository.findByCompanyId(filter.getCompanyId())
                : accountRepository.findAll();
        Map<Long, Account> accountsById = companyAccounts.stream().collect(Collectors.toMap(Account::getId, a -> a));
        Map<Long, Long> childCountByParentId = companyAccounts.stream()
                .filter(a -> a.getParentAccountId() != null)
                .collect(Collectors.groupingBy(Account::getParentAccountId, Collectors.counting()));
        Map<Long, String> companyNames = companyRepository.findAllById(
                page.getContent().stream().map(Account::getCompanyId).distinct().toList()
        ).stream().collect(Collectors.toMap(Company::getId, Company::getName));

        return PageResponse.of(page.map(a -> toResponse(a, companyNames.get(a.getCompanyId()), accountsById, childCountByParentId)));
    }

    @Override
    public AccountResponse get(Long id) {
        Account account = find(id);
        List<Account> companyAccounts = accountRepository.findByCompanyId(account.getCompanyId());
        Map<Long, Account> accountsById = companyAccounts.stream().collect(Collectors.toMap(Account::getId, a -> a));
        Map<Long, Long> childCountByParentId = companyAccounts.stream()
                .filter(a -> a.getParentAccountId() != null)
                .collect(Collectors.groupingBy(Account::getParentAccountId, Collectors.counting()));
        return toResponse(account, companyNameOf(account.getCompanyId()), accountsById, childCountByParentId);
    }

    @Override
    @Transactional
    public AccountResponse create(AccountRequest request) {
        requireCompany(request.getCompanyId());
        if (accountRepository.existsByCompanyIdAndAccountCode(request.getCompanyId(), request.getAccountCode())) {
            throw new AppException(HttpStatus.CONFLICT, "Account code already taken in this company: " + request.getAccountCode());
        }
        Account parent = requireParent(request.getParentAccountId(), request.getCompanyId(), request.getAccountType());

        Account account = Account.builder()
                .companyId(request.getCompanyId())
                .accountCode(request.getAccountCode())
                .name(request.getName())
                .accountType(request.getAccountType())
                .parentAccountId(parent == null ? null : parent.getId())
                .description(request.getDescription())
                .active(request.isActive())
                .build();
        accountRepository.save(account);
        return get(account.getId());
    }

    @Override
    @Transactional
    public AccountResponse update(Long id, AccountRequest request) {
        Account account = find(id);
        requireCompany(request.getCompanyId());
        if (accountRepository.existsByCompanyIdAndAccountCodeAndIdNot(request.getCompanyId(), request.getAccountCode(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "Account code already taken in this company: " + request.getAccountCode());
        }
        if (request.getAccountType() != account.getAccountType() && accountRepository.countByParentAccountId(id) > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot change the type of an account that has child accounts");
        }
        if (request.getParentAccountId() != null && request.getParentAccountId().equals(id)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "An account cannot be its own parent");
        }
        Account parent = requireParent(request.getParentAccountId(), request.getCompanyId(), request.getAccountType());
        if (parent != null) {
            requireNotDescendant(id, parent);
        }

        account.setCompanyId(request.getCompanyId());
        account.setAccountCode(request.getAccountCode());
        account.setName(request.getName());
        account.setAccountType(request.getAccountType());
        account.setParentAccountId(parent == null ? null : parent.getId());
        account.setDescription(request.getDescription());
        account.setActive(request.isActive());
        accountRepository.save(account);
        return get(account.getId());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        find(id);
        if (accountRepository.countByParentAccountId(id) > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delete an account that has child accounts — reassign or delete them first");
        }
        accountRepository.deleteById(id);
    }

    @Override
    @Transactional
    public List<AccountResponse> seedSampleChartOfAccounts(Long companyId) {
        requireCompany(companyId);

        record SeedChild(String code, String name) {}
        record SeedGroup(String code, String name, AccountType type, List<SeedChild> children) {}
        List<SeedGroup> groups = List.of(
                new SeedGroup("1000", "Assets", AccountType.ASSET, List.of(
                        new SeedChild("1100", "Cash"),
                        new SeedChild("1200", "Bank"),
                        new SeedChild("1300", "Inventory"))),
                new SeedGroup("2000", "Liabilities", AccountType.LIABILITY, List.of(
                        new SeedChild("2100", "Accounts Payable"))),
                new SeedGroup("4000", "Revenue", AccountType.REVENUE, List.of(
                        new SeedChild("4100", "Sales Revenue"))),
                new SeedGroup("5000", "Expenses", AccountType.EXPENSE, List.of(
                        new SeedChild("5100", "Salary"),
                        new SeedChild("5200", "Rent")))
        );

        for (SeedGroup group : groups) {
            Account groupAccount = accountRepository.findByCompanyId(companyId).stream()
                    .filter(a -> a.getAccountCode().equals(group.code()))
                    .findFirst()
                    .orElseGet(() -> accountRepository.save(Account.builder()
                            .companyId(companyId)
                            .accountCode(group.code())
                            .name(group.name())
                            .accountType(group.type())
                            .build()));
            for (SeedChild child : group.children()) {
                if (accountRepository.existsByCompanyIdAndAccountCode(companyId, child.code())) continue;
                accountRepository.save(Account.builder()
                        .companyId(companyId)
                        .accountCode(child.code())
                        .name(child.name())
                        .accountType(group.type())
                        .parentAccountId(groupAccount.getId())
                        .build());
            }
        }

        List<Account> companyAccounts = accountRepository.findByCompanyId(companyId);
        Map<Long, Account> accountsById = companyAccounts.stream().collect(Collectors.toMap(Account::getId, a -> a));
        Map<Long, Long> childCountByParentId = companyAccounts.stream()
                .filter(a -> a.getParentAccountId() != null)
                .collect(Collectors.groupingBy(Account::getParentAccountId, Collectors.counting()));
        String companyName = companyNameOf(companyId);
        return companyAccounts.stream()
                .sorted((a, b) -> a.getAccountCode().compareTo(b.getAccountCode()))
                .map(a -> toResponse(a, companyName, accountsById, childCountByParentId))
                .toList();
    }

    // Walks up from `parent` through its own ancestry — if `accountId` shows
    // up along the way, setting `parent` as accountId's parent would create a
    // cycle.
    private void requireNotDescendant(Long accountId, Account parent) {
        Account current = parent;
        Map<Long, Account> visited = new HashMap<>();
        while (current != null && current.getParentAccountId() != null) {
            if (current.getParentAccountId().equals(accountId)) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Cannot set parent to a descendant of this account");
            }
            if (visited.put(current.getId(), current) != null) break; // defensive: already-corrupt cycle, stop looping
            current = accountRepository.findById(current.getParentAccountId()).orElse(null);
        }
    }

    private Account requireParent(Long parentAccountId, Long companyId, AccountType accountType) {
        if (parentAccountId == null) return null;
        Account parent = accountRepository.findById(parentAccountId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Parent account not found with id: " + parentAccountId));
        if (!parent.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Parent account does not belong to the selected company");
        }
        if (parent.getAccountType() != accountType) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Parent account's type (" + parent.getAccountType() + ") must match this account's type (" + accountType + ")");
        }
        return parent;
    }

    private void requireCompany(Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private String companyNameOf(Long companyId) {
        return companyRepository.findById(companyId).map(Company::getName).orElse(null);
    }

    private Account find(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Account not found with id: " + id));
    }

    private AccountResponse toResponse(Account account, String companyName, Map<Long, Account> accountsById, Map<Long, Long> childCountByParentId) {
        Account parent = account.getParentAccountId() == null ? null : accountsById.get(account.getParentAccountId());
        return AccountResponse.builder()
                .id(account.getId())
                .companyId(account.getCompanyId())
                .companyName(companyName)
                .accountCode(account.getAccountCode())
                .name(account.getName())
                .accountType(account.getAccountType().name())
                .parentAccountId(account.getParentAccountId())
                .parentAccountCode(parent == null ? null : parent.getAccountCode())
                .parentAccountName(parent == null ? null : parent.getName())
                .description(account.getDescription())
                .active(account.isActive())
                .hasChildren(childCountByParentId.getOrDefault(account.getId(), 0L) > 0)
                .build();
    }
}
