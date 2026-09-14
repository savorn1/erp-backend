package com.example.erp.repository;

import com.example.erp.entity.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long>, JpaSpecificationExecutor<BankTransaction> {

    List<BankTransaction> findByBankAccountId(Long bankAccountId);

    boolean existsByBankAccountId(Long bankAccountId);
}
