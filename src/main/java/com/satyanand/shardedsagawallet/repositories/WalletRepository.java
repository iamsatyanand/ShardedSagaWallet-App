package com.satyanand.shardedsagawallet.repositories;

import com.satyanand.shardedsagawallet.entities.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    List<Wallet> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.userId = :id")
    Optional<Wallet> findByIdWithLock(@Param("id") Long id);

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("update Wallet w set w.balance = :balance where w.userId = :userId")
    void updateBalanceByUserId(@Param("userId") Long userId, @Param("balance") BigDecimal balance);

//    @Modifying
//    @Query("update Wallet w set w.balance = w.balance - :amount where w.userId = :userId and w.balance >= :amount")
//    int debitWalletBalance(@Param("userId") Long userId, @Param("amount")BigDecimal balance);
//
//    @Modifying
//    @Query("update Wallet w set w.balance = w.balance + :amount where w.userId = :userId")
//    int creditWalletBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}
