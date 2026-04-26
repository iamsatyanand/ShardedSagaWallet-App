package com.satyanand.shardedsagawallet.repositories;

import com.satyanand.shardedsagawallet.entities.SagaInstance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaInstanceRepository extends JpaRepository<SagaInstance, Long> {
}
