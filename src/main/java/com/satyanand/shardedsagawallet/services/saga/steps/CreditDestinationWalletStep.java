package com.satyanand.shardedsagawallet.services.saga.steps;

import com.satyanand.shardedsagawallet.entities.Wallet;
import com.satyanand.shardedsagawallet.repositories.WalletRepository;
import com.satyanand.shardedsagawallet.services.saga.SagaContext;
import com.satyanand.shardedsagawallet.services.saga.SagaStepInterface;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditDestinationWalletStep implements SagaStepInterface {

    private final WalletRepository walletRepository;


    @Override
    @Transactional
    public boolean execute(SagaContext context) {
        // Step 1 - get wallet id from context and  also the amount we have to transfer
        Long toWalletId = context.getLong("toWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Crediting destination wallet {} with amount {}", toWalletId, amount);

        // Step 2 - Fetch the destination wallet from db with a lock - as we do not want to put ourselves in any kind of race condition
        Wallet wallet = walletRepository.findByIdWithLock(toWalletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        log.info("wallet fetched with balance {}", wallet.getBalance());

        context.put("originalToWalletBalance", wallet.getBalance());

        //Step 3 - credit the destination wallet

        wallet.credit(amount);
        walletRepository.save(wallet);

        log.info("wallet saved with balance {}", wallet.getBalance());

        // Step 4 - update the context with the changes
        context.put("toWalletBalanceAfterCredit", wallet.getBalance());

        log.info("Credit destination wallet step executed successfully");





        return true;
    }

    @Override
    @Transactional
    public boolean compensate(SagaContext context) {
        // Step 1 - get wallet id from context and  also the amount we have to transfer
        Long toWalletId = context.getLong("toWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Compensating credit of destination wallet {} with amount {}", toWalletId, amount);

        // Step 2 - Fetch the destination wallet from db with a lock - as we do not want to put ourselves in any kind of race condition
        Wallet wallet = walletRepository.findByIdWithLock(toWalletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        log.info("wallet fetched with balance {}", wallet.getBalance());

        //Step 3 - credit the destination wallet

        wallet.debit(amount);
        walletRepository.save(wallet);

        log.info("wallet saved with balance {}", wallet.getBalance());

        // Step 4 - update the context with the changes
        context.put("toWalletBalanceAfterCreditCompensation", wallet.getBalance());

        log.info("Credit compensation of destination wallet step executed successfully");





        return true;
    }

    @Override
    public String getStepName() {
        return SagaStepFactory.SagaStepType.CREDIT_DESTINATION_WALLET_STEP.toString();
    }
}
