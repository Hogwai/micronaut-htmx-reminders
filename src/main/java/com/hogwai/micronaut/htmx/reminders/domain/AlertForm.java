package com.hogwai.micronaut.htmx.reminders.domain;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Introspected
@Serdeable
public record AlertForm(
        String title,
        String description,
        AlertType type,
        String triggerAt,
        String cronExpression
) {}

