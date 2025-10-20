package com.hogwai.micronaut.htmx.reminders.repository;

import com.hogwai.micronaut.htmx.reminders.domain.Alert;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AlertRepository extends CrudRepository<Alert, Long> {

    @Query(value = "SELECT * FROM alerts WHERE active = true ORDER BY created_at DESC", nativeQuery = true)
    List<Alert> findAllActive();

    @Query(value = "SELECT * FROM alerts WHERE active = true " +
            "AND ((type = 'ONE_TIME' AND trigger_at <= :now AND last_triggered IS NULL) " +
            "OR (type = 'RECURRING' AND " +
            "(last_triggered IS NULL OR last_triggered < :threshold)))", nativeQuery = true)
    List<Alert> findAlertsToTrigger(Instant now, Instant threshold);
}
