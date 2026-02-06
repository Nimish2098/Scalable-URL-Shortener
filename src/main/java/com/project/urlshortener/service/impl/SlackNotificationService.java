package com.project.urlshortener.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class SlackNotificationService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${notification.slack.webhook-url}")
    private String webhookUrl;

    @Value("${notification.slack.enabled:true}")
    private boolean enabled;

    @Value("${notification.slack.max-retries:3}")
    private int maxRetries;

    @org.springframework.scheduling.annotation.Async
    public void notify(String message) {

        if (!enabled || webhookUrl == null || webhookUrl.isEmpty()) {
            log.info("Slack notifications disabled or webhook URL missing");
            return;
        }

        Map<String, String> payload = Map.of("text", message);

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                restTemplate.postForEntity(webhookUrl, payload, String.class);
                log.info("Slack notification sent successfully");
                return;
            } catch (Exception ex) {
                log.warn(
                        "Slack notification attempt {}/{} failed",
                        attempt,
                        maxRetries,
                        ex);
                backoff(attempt);
            }
        }

        log.error("Slack notification failed after {} attempts", maxRetries);
    }

    private void backoff(int attempt) {
        try {
            Thread.sleep(1000L * attempt); // simple linear backoff
        } catch (InterruptedException ignored) {
        }
    }
}
