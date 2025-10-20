package com.hogwai.micronaut.htmx.reminders.scheduler;

import com.hogwai.micronaut.htmx.reminders.domain.Alert;
import com.hogwai.micronaut.htmx.reminders.domain.AlertType;
import com.hogwai.micronaut.htmx.reminders.repository.AlertRepository;
import com.hogwai.micronaut.htmx.reminders.service.CronService;
import com.hogwai.micronaut.htmx.reminders.websocket.AlertWebSocket;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Singleton
public class AlertScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(AlertScheduler.class);

    private final AlertRepository repository;
    private final AlertWebSocket webSocket;
    private final CronService cronService;

    public AlertScheduler(AlertRepository repository,
                          AlertWebSocket webSocket,
                          CronService cronService) {
        this.repository = repository;
        this.webSocket = webSocket;
        this.cronService = cronService;
    }

    @Scheduled(fixedDelay = "5s", initialDelay = "10s")
    public void checkAlerts() {
        Instant now = Instant.now();
        Instant threshold = now.minus(25, ChronoUnit.SECONDS);

        List<Alert> alerts = repository.findAlertsToTrigger(now, threshold);

        LOG.debug("Alert checking: {} found", alerts.size());

        for (Alert alert : alerts) {
            if (shouldTriggerAlert(alert, now)) {
                triggerAlert(alert, now);
            }
        }
    }

    private boolean shouldTriggerAlert(Alert alert, Instant now) {
        if (alert.getType() == AlertType.ONE_TIME) {
            return alert.getTriggerAt() != null &&
                    !alert.getTriggerAt().isAfter(now) &&
                    alert.getLastTriggered() == null;
        } else if (alert.getType() == AlertType.RECURRING) {
            return cronService.shouldTrigger(
                    alert.getCronExpression(),
                    alert.getLastTriggered()
            );
        }
        return false;
    }

    private void triggerAlert(Alert alert, Instant now) {
        LOG.info("Triggering alert: {}", alert.getTitle());

        webSocket.sendAlert(alert);

        alert.setLastTriggered(now);

        if (alert.getType() == AlertType.ONE_TIME) {
            alert.setActive(false);
        }

        repository.update(alert);
    }
}
