package com.satyanand.shardedsagawallet.configs;

import com.satyanand.shardedsagawallet.services.saga.SagaStepInterface;
import com.satyanand.shardedsagawallet.services.saga.steps.CreditDestinationWalletStep;
import com.satyanand.shardedsagawallet.services.saga.steps.DebitSourceWalletStep;
import com.satyanand.shardedsagawallet.services.saga.steps.SagaStepFactory.SagaStepType;
import com.satyanand.shardedsagawallet.services.saga.steps.UpdateTransactionStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SagaConfigurations {

    @Bean
    public Map<String, SagaStepInterface> sagaStepMap(
            DebitSourceWalletStep debitSourceWalletStep,
            CreditDestinationWalletStep creditDestinationWalletStep,
            UpdateTransactionStatus updateTransactionStatus
    ){
        Map<String, SagaStepInterface> sagaStepMap = new HashMap<>();
        sagaStepMap.put(SagaStepType.DEBIT_SOURCE_WALLET_STEP.toString(), debitSourceWalletStep);
        sagaStepMap.put(SagaStepType.CREDIT_DESTINATION_WALLET_STEP.toString(), creditDestinationWalletStep);
        sagaStepMap.put(SagaStepType.UPDATE_TRANSACTION_STATUS_STEP.toString(), updateTransactionStatus);
        return sagaStepMap;
    }
}
