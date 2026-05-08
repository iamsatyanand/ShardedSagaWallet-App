package com.satyanand.shardedsagawallet.services;

import com.satyanand.shardedsagawallet.entities.SagaInstance;
import com.satyanand.shardedsagawallet.entities.Transaction;
import com.satyanand.shardedsagawallet.services.saga.SagaContext;
import com.satyanand.shardedsagawallet.services.saga.SagaOrchestrator;
import com.satyanand.shardedsagawallet.services.saga.steps.SagaStepFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferSagaService {
    private final TransactionService transactionService;
    private final SagaOrchestrator sagaOrchestrator;

    @Transactional
    public Long initiateTransfer(
            Long fromWalletId,
            Long toWalletId,
            BigDecimal amount,
            String description
    ){
        Transaction transaction = transactionService.createTransaction(fromWalletId, toWalletId, amount, description);

        SagaContext sagaContext = SagaContext.builder()
                .data(Map.ofEntries(
                        Map.entry("transactionId", transaction.getId()),
                        Map.entry("fromWalletId", fromWalletId),
                        Map.entry("toWalletId", toWalletId),
                        Map.entry("amount", amount),
                        Map.entry("description", description == null ? "" : description)
                ))
                .build();

        Long sagaInstanceId = sagaOrchestrator.startSaga(sagaContext);
        transactionService.updateTransactionWithSagaInstanceId(transaction.getId(), sagaInstanceId);

        executeTransferSaga(sagaInstanceId);

        return sagaInstanceId;
    }

    public void executeTransferSaga(Long sagaInstanceId) {
        log.info("Executing transfer saga {}", sagaInstanceId);
        try {
            for (SagaStepFactory.SagaStepType step : SagaStepFactory.TransferMoneySagaSteps) {
                boolean success = sagaOrchestrator.executeStep(sagaInstanceId, step.toString());
                if (!success) {
                    log.error("Step {} failed for saga {} — initiating compensation", step, sagaInstanceId);
                    sagaOrchestrator.failSaga(sagaInstanceId);
                    return;
                }
            }
            sagaOrchestrator.completeSaga(sagaInstanceId);
            log.info("Transfer saga {} completed successfully", sagaInstanceId);
        } catch (Exception e) {
            log.error("Exception in transfer saga {}: {}", sagaInstanceId, e.getMessage(), e);
            sagaOrchestrator.failSaga(sagaInstanceId);
        }
    }


}
