package com.satyanand.shardedsagawallet.entities;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import jakarta.persistence.*;
import lombok.*;
import org.apache.calcite.model.JsonType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="saga_instance")
@Getter
@Setter
@Builder
public class SagaInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SagaStatus status = SagaStatus.STARTED;

    @Type(JsonType.class)
    @Column(name = "context", columnDefinition = "json")
    private String context;

    @Column(name = "current_step")
    private String currentStep;

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

    public void markAsStarted()      { this.status = SagaStatus.STARTED; }
    public void markAsRunning()      { this.status = SagaStatus.RUNNING; }
    public void markAsCompleted()    { this.status = SagaStatus.COMPLETED; }
    public void markAsFailed()       { this.status = SagaStatus.FAILED; }
    public void markAsCompensating() { this.status = SagaStatus.COMPENSATING; }
    public void markAsCompensated()  { this.status = SagaStatus.COMPENSATED; }
}
