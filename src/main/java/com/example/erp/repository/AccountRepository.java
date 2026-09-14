package com.example.erp.repository;

import com.example.erp.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {

    boolean existsByCompanyIdAndAccountCode(Long companyId, String accountCode);

    boolean existsByCompanyIdAndAccountCodeAndIdNot(Long companyId, String accountCode, Long id);

    List<Account> findByParentAccountId(Long parentAccountId);

    List<Account> findByCompanyId(Long companyId);

    long countByParentAccountId(Long parentAccountId);
}
