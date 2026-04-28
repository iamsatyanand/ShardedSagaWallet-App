package com.satyanand.shardedsagawallet.services.saga.steps;

import com.satyanand.shardedsagawallet.entities.Transaction;
import com.satyanand.shardedsagawallet.entities.TransactionStatus;
import com.satyanand.shardedsagawallet.repositories.TransactionRepository;
import com.satyanand.shardedsagawallet.services.saga.SagaContext;
import com.satyanand.shardedsagawallet.services.saga.SagaStepInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateTransactionStatus implements SagaStepInterface {

    private final TransactionRepository transactionRepository;

    @Override
    public boolean execute(SagaContext context) {

        Long transactionId = context.getLong("transactionId");
        log.info("Updating transaction status for transaction {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        context.put("originalTransactionStatus", transaction.getStatus());

        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);

        log.info("Transaction status updated for transaction {}", transactionId);

        context.put("transactionStatusAfterUpdate", transaction.getStatus());

        log.info("Update transaction status step executed successfully");

        return true;
    }

    @Override
    public boolean compensate(SagaContext context) {
        Long transactionId = context.getLong("transactionId");
        log.info("Compensating transaction status for transaction {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        TransactionStatus originalTransactionStatus = TransactionStatus.valueOf(context.getString("originalTransactionStatus"));

        transaction.setStatus(originalTransactionStatus);
        transactionRepository.save(transaction);

        log.info("Transaction status compensated for transaction {}", transactionId);

        log.info("Transaction status compensated for transaction {}", transactionId);

        return true;
    }

    @Override
    public String getStepName() {
        return SagaStepFactory.SagaStepType.UPDATE_TRANSACTION_STATUS_STEP.toString();
    }
}
