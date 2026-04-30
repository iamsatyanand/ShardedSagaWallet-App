package com.satyanand.shardedsagawallet.repositories;

import com.satyanand.shardedsagawallet.entities.SagaStatus;
import com.satyanand.shardedsagawallet.entities.SagaStep;
import com.satyanand.shardedsagawallet.entities.StepStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SagaStepRepository extends JpaRepository<SagaStep, Long> {
    List<SagaStep> findBySagaInstanceId(Long sagaInstanceId);

    List<SagaStep> findBySagaInstanceIdAndStatus(Long sagaInstanceId, StepStatus status);

    Optional<SagaStep> findBySagaInstanceIdAndStepNameAndStatus(Long sagaInstanceId, String stepName, StepStatus status);

    @Query("select s from SagaStep s where s.sagaInstanceId = :sagaInstanceId and s.status = 'COMPLETED'")
    List<SagaStep> findCompletedStepsBySagaInstanceId(@Param("sagaInstanceId") Long sagaInstanceId);

    @Query("select s from SagaStep s where s.sagaInstanceId = :sagaInstanceId and s.status in ('COMPLETED', 'COMPENSATED')")
    List<SagaStep> findCompletedOrCompensatedStepsBySagaInstanceId(@Param("sagaInstanceId") Long sagaInstanceId);
}
