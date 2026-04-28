package com.satyanand.shardedsagawallet.services.saga.steps;

import com.satyanand.shardedsagawallet.services.saga.SagaStepInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SagaStepFactory {

    private final Map<String, SagaStepInterface> sagaStepMap;

    public static enum SagaStepType{
        DEBIT_SOURCE_WALLET_STEP,
        CREDIT_DESTINATION_WALLET_STEP,
        UPDATE_TRANSACTION_STATUS_STEP
    }

    public SagaStepInterface getSagaStep(String stepName){
        return sagaStepMap.get(stepName);
    }
}
