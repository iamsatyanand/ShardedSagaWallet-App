package com.satyanand.shardedsagawallet.entities;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import jakarta.persistence.*;
import lombok.*;
import org.apache.calcite.model.JsonType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="saga_step")
@Getter
@Setter
@Builder
public class SagaStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "saga_instance_id", nullable = false)
    private Long sagaInstanceId;

    @Column(name = "step_name", nullable = false)
    private String stepName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StepStatus status;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Type(JsonType.class)
    @Column(name = "step_data", columnDefinition = "json")
    private String stepData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void markAsCompensated() { this.status = StepStatus.COMPENSATED; }
    public void markAsFailed()      { this.status = StepStatus.FAILED; }
    public void markAsPending()     { this.status = StepStatus.PENDING; }
    public void markAsRunning()     { this.status = StepStatus.RUNNING; }
    public void markAsCompleted()   { this.status = StepStatus.COMPLETED; }
    public void markAsCompensating(){ this.status = StepStatus.COMPENSATING; }
}
