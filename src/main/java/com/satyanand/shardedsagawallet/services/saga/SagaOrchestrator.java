package com.satyanand.shardedsagawallet.services.saga;

import com.satyanand.shardedsagawallet.entities.SagaInstance;
import com.satyanand.shardedsagawallet.entities.SagaStep;

import java.util.List;

public interface SagaOrchestrator {

    SagaInstance startSaga(SagaContext context);

    boolean executeStep(Long sagaInstanceId, String stepName);

    boolean compensateStep(Long sagaInstanceId, String stepName);

    SagaInstance getSagaInstance(Long sagaInstanceId);

    void compensateSaga(Long sagaInstanceId); // compensate the complete saga i.e. all the steps of saga should be compensated

    void failSaga(Long sagaInstanceId); // saga steps has failed

    void completeSaga(Long sagaInstanceId); // all the saga steps are completed

}
