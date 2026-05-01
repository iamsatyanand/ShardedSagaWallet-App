package com.satyanand.shardedsagawallet.services;

import com.satyanand.shardedsagawallet.entities.Wallet;
import com.satyanand.shardedsagawallet.exceptions.InsufficientFundsException;
import com.satyanand.shardedsagawallet.exceptions.WalletNotFoundException;
import com.satyanand.shardedsagawallet.repositories.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletService {


    private final WalletRepository walletRepository;

    public Wallet createWallet(Long userId) {
        log.info("Creating wallet for user {}", userId);
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .isActive(true)
                .balance(BigDecimal.ZERO)
                .build();
        wallet = walletRepository.save(wallet);
        log.info("Wallet created with id {}", wallet.getId());
        return wallet;
    }

    public Wallet getWalletById(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException(id));
    }

    public List<Wallet> getWalletsByUserId(Long userId) {
        return walletRepository.findByUserId(userId);
    }

}
