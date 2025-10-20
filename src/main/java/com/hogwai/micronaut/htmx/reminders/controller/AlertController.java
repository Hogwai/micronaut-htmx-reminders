package com.hogwai.micronaut.htmx.reminders.controller;

import com.hogwai.micronaut.htmx.reminders.domain.Alert;
import com.hogwai.micronaut.htmx.reminders.domain.AlertType;
import com.hogwai.micronaut.htmx.reminders.domain.AlertForm;
import com.hogwai.micronaut.htmx.reminders.repository.AlertRepository;
import com.hogwai.micronaut.htmx.reminders.service.CronService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.views.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("/alerts")
public class AlertController {

    private static final Logger LOG = LoggerFactory.getLogger(AlertController.class);

    private final AlertRepository repository;
    private final CronService cronService;

    public AlertController(AlertRepository repository, CronService cronService) {
        this.repository = repository;
        this.cronService = cronService;
    }

    @Get
    @View("index")
    public Map<String, Object> index() {
        Map<String, Object> model = new HashMap<>();
        model.put("alerts", repository.findAllActive());
        return model;
    }

    @Get("/list")
    @Produces(MediaType.TEXT_HTML)
    public String list() {
        List<Alert> alerts = repository.findAllActive();

        if (alerts.isEmpty()) {
            return "<p style='text-align: center; color: #666; padding: 40px;'>No alert</p>";
        }

        StringBuilder html = new StringBuilder();
        for (Alert alert : alerts) {
            html.append("<div class='alert-item'>")
                .append("<h3>").append(escapeHtml(alert.getTitle())).append("</h3>");

            if (alert.getDescription() != null && !alert.getDescription().isEmpty()) {
                html.append("<p>").append(escapeHtml(alert.getDescription())).append("</p>");
            }

            html.append("<div class='alert-meta'>");

            if (alert.getType() == AlertType.ONE_TIME) {
                html.append("📅 One time - ")
                    .append(formatInstant(alert.getTriggerAt()));
            } else if (alert.getType() == AlertType.RECURRING) {
                html.append("🔄 Recurring - <code>")
                    .append(escapeHtml(alert.getCronExpression()))
                    .append("</code>");
            }

            html.append("<br/><small>Created at ")
                .append(formatInstant(alert.getCreatedAt()))
                .append("</small></div>");

            html.append("<button class='delete' ")
                .append("hx-delete='/alerts/").append(alert.getId()).append("' ")
                .append("hx-confirm='Delete this alert ?' ")
                .append("hx-target='closest .alert-item' ")
                .append("hx-swap='outerHTML swap:1s'>")
                .append("🗑️ Delete</button>")
                .append("</div>");
        }

        return html.toString();
    }

    @Post
    @Consumes("application/x-www-form-urlencoded")
    public HttpResponse<?> create(@Body AlertForm form) {
        LOG.info("Creating alert - Type: {}, Title: {}", form.type(), form.title());

        Alert alert = new Alert();
        alert.setTitle(form.title());
        alert.setDescription(form.description());
        alert.setType(form.type());

        if (form.type() == AlertType.ONE_TIME) {
            if (form.triggerAt() == null || form.triggerAt().isEmpty()) {
                return HttpResponse.badRequest()
                                   .body("triggerAt mandatory for ONE_TIME");
            }
            try {
                alert.setTriggerAt(Instant.parse(form.triggerAt()));
            } catch (Exception e) {
                return HttpResponse.badRequest()
                                   .body("Invalid date format");
            }
        } else if (form.type() == AlertType.RECURRING) {
            if (form.cronExpression() == null || form.cronExpression().isEmpty() ||
                    !cronService.isValidCron(form.cronExpression())) {
                return HttpResponse.badRequest()
                                   .body("Invalid cron");
            }
            alert.setCronExpression(form.cronExpression());
        }

        repository.save(alert);
        LOG.info("Created alert: {}", alert.getTitle());

        return HttpResponse.ok()
                           .header("HX-Trigger", "alertCreated");
    }

    @Delete(value = "/{id}", consumes = MediaType.ALL)
    public HttpResponse<?> delete(@PathVariable Long id) {
        return repository.findById(id)
                         .map(alert -> {
                             alert.setActive(false);
                             repository.delete(alert);
                             LOG.info("Deleted alert: {}", alert.getTitle());
                             return HttpResponse.ok()
                                                .header("HX-Trigger", "alertDeleted");
                         })
                         .orElse(HttpResponse.notFound());
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }

    private String formatInstant(Instant instant) {
        if (instant == null) return "";
        return instant.atZone(java.time.ZoneId.systemDefault())
                      .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
