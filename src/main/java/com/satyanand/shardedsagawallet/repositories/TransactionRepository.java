package com.satyanand.shardedsagawallet.repositories;

import com.satyanand.shardedsagawallet.entities.Transaction;
import com.satyanand.shardedsagawallet.entities.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByFromWalletId(Long fromWalletId); // all the debit transactions
    List<Transaction> findByToWalletId(Long toWalletId); // all the credit transactions
    @Query("select t from Transaction t where t.fromWalletId = :walletId or t.toWalletId = :walletId")
    List<Transaction> findByWalletId(@Param("walletId") Long walletId);
    List<Transaction> findByStatus(TransactionStatus status);
    List<Transaction> findBySagaInstanceId(Long sagaInstanceId);
}
