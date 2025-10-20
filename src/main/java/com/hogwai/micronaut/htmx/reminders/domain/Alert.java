package com.hogwai.micronaut.htmx.reminders.domain;

import io.micronaut.serde.annotation.Serdeable;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "alerts")
@Serdeable
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType type;

    // ONE_TIME
    private Instant triggerAt;

    // RECURRING
    private String cronExpression;

    @Column(nullable = false)
    private boolean active = true;

    private Instant lastTriggered;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Alert() {}

    public Alert(String title, String description, AlertType type) {
        this.title = title;
        this.description = description;
        this.type = type;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public AlertType getType() { return type; }
    public void setType(AlertType type) { this.type = type; }

    public Instant getTriggerAt() { return triggerAt; }
    public void setTriggerAt(Instant triggerAt) { this.triggerAt = triggerAt; }

    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getLastTriggered() { return lastTriggered; }
    public void setLastTriggered(Instant lastTriggered) { this.lastTriggered = lastTriggered; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}