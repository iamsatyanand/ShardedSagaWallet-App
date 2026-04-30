package com.satyanand.shardedsagawallet.services.saga;

import com.satyanand.shardedsagawallet.entities.SagaInstance;
import com.satyanand.shardedsagawallet.entities.SagaStatus;
import com.satyanand.shardedsagawallet.entities.SagaStep;
import com.satyanand.shardedsagawallet.entities.StepStatus;
import com.satyanand.shardedsagawallet.repositories.SagaInstanceRepository;
import com.satyanand.shardedsagawallet.repositories.SagaStepRepository;
import com.satyanand.shardedsagawallet.services.saga.steps.SagaStepFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestratorImpl implements SagaOrchestrator{

    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaStepRepository sagaStepRepository;
    private final ObjectMapper objectMapper;
    private final SagaStepFactory sagaStepFactory;


    /*
     * Starts a new Saga instance.
     * Responsibility:
     * - Serializes the provided SagaContext.
     * - Persists a new SagaInstance with status STARTED.
     *
     * Notes:
     * - This only initializes the saga; no steps are executed here.
     * - Execution is driven separately via executeStep().
     *
     * Failure Handling:
     * - Any serialization or persistence failure results in a runtime exception.
     */
    @Override
    @Transactional
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

    /*
     * Executes a specific step in the Saga.
     *
     * Responsibility:
     * - Fetch the saga instance and step definition.
     * - Ensure a SagaStep entry exists (or create one if missing).
     * - Execute the step logic using the SagaStep implementation.
     * - Update step and saga state accordingly.
     *
     * Flow:
     * 1. Mark step as RUNNING
     * 2. Execute step
     * 3. On success → mark COMPLETED, update saga progress
     * 4. On failure → mark FAILED
     *
     * Notes:
     * - Each step execution is isolated and tracked independently.
     * - Context is deserialized per execution to maintain statelessness.
     *
     * Failure Handling:
     * - Any exception marks the step as FAILED.
     * - Caller is responsible for triggering compensation if needed.
     */

    @Override
    @Transactional
    public boolean executeStep(Long sagaInstanceId, String stepName) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga Instance not found"));

        SagaStepInterface step = sagaStepFactory.getSagaStep(stepName);
        if(step == null){
            log.info("Saga step {} not found ", stepName);
            throw new RuntimeException("Saga step not found: " + stepName);
        }

//        SagaStep sagaStepDB =  sagaStepRepository.findBySagaInstanceIdAndStatus(sagaInstanceId, StepStatus.PENDING)
//                .stream()
//                .filter(s -> s.getStepName().equals(stepName))
//                .findFirst()
//                .orElse(SagaStep.builder().sagaInstanceId(sagaInstance.getId()).stepName(stepName).status(StepStatus.PENDING).build());


        SagaStep sagaStepDB = sagaStepRepository
                .findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, StepStatus.PENDING)
                .orElse(
                        SagaStep.builder()
                                .sagaInstanceId(sagaInstanceId)
                                .stepName(stepName)
                                .status(StepStatus.PENDING)
                                .build()
                );

        if(sagaStepDB.getId() == null){
            sagaStepRepository.save(sagaStepDB);
        }

        try{

            SagaContext context = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
            sagaStepDB.setStatus(StepStatus.RUNNING);
            sagaStepRepository.save(sagaStepDB);

            boolean success = step.execute(context);

            if(success){
                sagaStepDB.setStatus(StepStatus.COMPLETED);
                sagaStepRepository.save(sagaStepDB);

                sagaInstance.setCurrentStep(stepName); // step we just completed
                sagaInstance.setStatus(SagaStatus.RUNNING); // saga is not completed only one of the step completed saga is still running
                sagaInstanceRepository.save(sagaInstance);

                log.info("Step {} executed successfully", stepName);
                return true;

            }
            else{
                sagaStepDB.setStatus(StepStatus.FAILED);
                sagaStepRepository.save(sagaStepDB);
                log.error("Step {} failed", stepName);
                return false;
            }

        }catch (Exception e){
            sagaStepDB.setStatus(StepStatus.FAILED);
            sagaStepRepository.save(sagaStepDB);
            log.error("Failed to execute step {}", stepName);
            return false;
        }


    }

    @Override
    public boolean compensateStep(Long sagaInstanceId, String stepName) {
        // 1. Retrieve the saga instance from the database using the instance ID
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga Instance not found"));

        // 2. Retrieve the specific saga step from the database using the instance ID and step name
        SagaStepInterface step = sagaStepFactory.getSagaStep(stepName);
        if(step == null){
            log.info("Saga step {} not found ", stepName);
            throw new RuntimeException("Saga step not found: " + stepName);
        }

        SagaStep sagaStepDB = sagaStepRepository
                .findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, StepStatus.COMPLETED)
                .orElse(null);

        if(sagaStepDB == null){
            log.info("Step {} not found in db for saga instance {}, so it is either already compensated or not executed", stepName, sagaInstanceId);
            return true;
        }
        // 3. Extract the context from the saga instance and invoke the compensation logic
        try{

            SagaContext context = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
            sagaStepDB.setStatus(StepStatus.COMPENSATING);
            sagaStepRepository.save(sagaStepDB);

            boolean success = step.compensate(context);

            if(success){
                sagaStepDB.setStatus(StepStatus.COMPENSATED);
                sagaStepRepository.save(sagaStepDB);

                log.info("Step {} compensated successfully", stepName);
                return true;

            }
            else{
                sagaStepDB.setStatus(StepStatus.FAILED);
                sagaStepRepository.save(sagaStepDB);
                log.error("Step {} failed", stepName);
                return false;
            }

        }catch (Exception e){
            sagaStepDB.setStatus(StepStatus.FAILED);
            sagaStepRepository.save(sagaStepDB);
            log.error("Failed to execute step {}", stepName);
            return false;
        }
        // 4. Update the status of the saga step accordingly


        return false;
    }

    /*
     * Retrieves a Saga instance by ID.
     *
     * Responsibility:
     * - Fetch the SagaInstance from persistence.
     *
     * Notes:
     * - Used for querying current saga state and debugging.
     *
     * Failure Handling:
     * - Throws exception if saga is not found.
     */

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {
        return sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found: " + sagaInstanceId));
    }

    /*
     * Compensation Flow (Orchestrator-driven Saga):
     *
     * Consider a sequence of distributed steps across services:
     *     A → B → C
     *
     * - Each step represents a successful operation in a different service.
     * - If step C fails, the saga must ensure data consistency by rolling back
     *   previously completed steps.
     *
     * Compensation Strategy:
     * - Compensation is executed in reverse order of successful steps.
     * - Only steps that were successfully completed are compensated.
     *
     * Example:
     *     Step A → COMPLETED
     *     Step B → COMPLETED
     *     Step C → FAILED
     *
     * Compensation flow:
     *     1. Compensate Step B
     *     2. Compensate Step A
     *
     * Important Notes:
     * - Step C is NOT compensated because it never completed successfully.
     * - Each compensation step must be idempotent to safely support retries.
     * - If a compensation step fails (e.g., compensating B fails),
     *   the orchestrator should retry or mark the saga as FAILED.
     *
     * Orchestration Rule:
     * - Compensation is centrally controlled by the orchestrator.
     * - A service (e.g., C) MUST NOT trigger compensation of another service (e.g., B).
     * - No cascading or peer-to-peer compensation calls between services.
     *
     * What NOT to do:
     * - Do NOT compensate in forward order (A → B → C); this breaks consistency.
     * - Do NOT attempt to compensate a step that never completed.
     * - Do NOT let one service trigger compensation of another service.
     * - Do NOT stop compensation midway without marking the saga as FAILED.
     * - Do NOT assume compensation will always succeed; always handle retries/failures.
     * - Do NOT tightly couple services; each service must own its compensation logic.
     *
     * Orchestrator Responsibility:
     * - Identify all COMPLETED steps for the saga instance.
     * - Execute their compensation logic in reverse order.
     * - Update step statuses (e.g., COMPENSATED or FAILED).
     * - Ensure the saga reaches a consistent terminal state (FAILED).
     */
    @Override
    public void compensateSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found: " + sagaInstanceId));

        List<SagaStep> completedSteps = sagaStepRepository.findCompletedStepsBySagaInstanceId(sagaInstanceId);
        sagaInstance.setStatus(SagaStatus.COMPENSATING);
        sagaInstanceRepository.save(sagaInstance);
        boolean allCompensated = true;
        for(SagaStep completedStep : completedSteps){
            boolean success = this.compensateStep(sagaInstanceId, completedStep.getStepName());
            if(!success){
                allCompensated = false;
            }
        }
        if(allCompensated){
            sagaInstance.setStatus(SagaStatus.COMPENSATED);
            sagaInstanceRepository.save(sagaInstance);
            log.info("Saga {} compensated successfully", sagaInstanceId);
        }
        else{
            log.error("Saga {} compensation failed", sagaInstanceId);
        }
    }

    /*
     * Marks a Saga as FAILED.
     *
     * Responsibility:
     * - Update saga status to FAILED.
     *
     * Notes:
     * - Typically invoked after compensation completes or unrecoverable failure.
     * - Indicates the saga did not complete successfully.
     */

    @Override
    public void failSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found: " + sagaInstanceId));
        sagaInstance.setStatus(SagaStatus.FAILED);
        sagaInstanceRepository.save(sagaInstance);

        log.warn("Saga {} failed", sagaInstanceId);
    }

    /*
     * Marks a Saga as COMPLETED.
     *
     * Responsibility:
     * - Update saga status to COMPLETED.
     *
     * Notes:
     * - Should only be called after all steps have executed successfully.
     * - Represents a successful end of the saga lifecycle.
     */

    @Override
    public void completeSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found: " + sagaInstanceId));
        sagaInstance.setStatus(SagaStatus.COMPLETED);
        sagaInstanceRepository.save(sagaInstance);
        log.info("Saga {} completed", sagaInstanceId);
    }
}
