package com.satyanand.shardedsagawallet.services.saga;

import com.satyanand.shardedsagawallet.entities.SagaInstance;
import com.satyanand.shardedsagawallet.entities.SagaStatus;
import com.satyanand.shardedsagawallet.entities.SagaStep;
import com.satyanand.shardedsagawallet.entities.StepStatus;
import com.satyanand.shardedsagawallet.repositories.SagaInstanceRepository;
import com.satyanand.shardedsagawallet.repositories.SagaStepRepository;
import com.satyanand.shardedsagawallet.services.saga.steps.SagaStepFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestratorImpl implements SagaOrchestrator{

    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaStepRepository sagaStepRepository;
    private final ObjectMapper objectMapper;
    private final SagaStepFactory sagaStepFactory;

    @Override
    public SagaInstance startSaga(SagaContext context) {
        try{
            String contextJSON = objectMapper.writeValueAsString(context);
            SagaInstance sagaInstance = SagaInstance.builder()
                    .context(contextJSON)
                    .status(SagaStatus.STARTED)
                    .build();

            sagaInstance = sagaInstanceRepository.save(sagaInstance);
            log.info("Started saga with id {}", sagaInstance.getId());
            return sagaInstance;

        }
        catch (Exception e){
            log.error("Error starting saga", e);
            throw new RuntimeException("Error starting saga", e);
        }
    }

    @Override
    public boolean executeStep(Long sagaInstanceId, String stepName) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga Instance not found"));

        SagaStepInterface step = sagaStepFactory.getSagaStep(stepName);
        if(step == null){
            log.info("Saga step {} not found ", stepName);
            throw new RuntimeException("Saga step not found: " + stepName);
        }

        SagaStep sagaStepDB =  sagaStepRepository.findBySagaInstanceIdAndStatus(sagaInstanceId, SagaStatus.RUNNING)
                .stream()
                .filter(s -> s.getStepName().equals(stepName))
                .findFirst()
                .orElse(SagaStep.builder().sagaInstanceId(sagaInstance.getId()).stepName(stepName).status(StepStatus.PENDING).build());



        return false;
    }

    @Override
    public boolean compensateStep(Long sagaInstanceId, String stepName) {
        return false;
    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {
        return null;
    }

    @Override
    public void compensateSaga(Long sagaInstanceId) {

    }

    @Override
    public void failSaga(Long sagaInstanceId) {

    }

    @Override
    public void completeSaga(Long sagaInstanceId) {

    }
}
