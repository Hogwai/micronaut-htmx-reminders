package com.hogwai.micronaut.htmx.reminders.service;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Singleton
public class CronService {

    private static final Logger LOG = LoggerFactory.getLogger(CronService.class);
    private final CronParser parser;

    public CronService() {
        this.parser = new CronParser(
                CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX)
        );
    }

    public boolean shouldTrigger(String cronExpression, Instant lastTriggered) {
        try {
            Cron cron = parser.parse(cronExpression);
            ExecutionTime executionTime = ExecutionTime.forCron(cron);

            ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());

            if (lastTriggered == null) {
                return executionTime.isMatch(now);
            }

            ZonedDateTime lastTime = ZonedDateTime.ofInstant(lastTriggered, ZoneId.systemDefault());
            Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(lastTime);

            return nextExecution.isPresent() && !nextExecution.get()
                                                              .isAfter(now);

        } catch (Exception e) {
            LOG.error("Error while parsing cron expression: {}", cronExpression, e);
            return false;
        }
    }


    /**
     * Check the cron format
     *
     * @param cronExpression cron expression
     * @return true if valid, false otherwise
     */
    public boolean isValidCron(String cronExpression) {
        try {
            parser.parse(cronExpression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
