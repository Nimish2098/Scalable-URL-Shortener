package com.project.urlshortener.service.impl;

import com.project.urlshortener.model.LinkAccessLog;
import com.project.urlshortener.model.UrlMapping;
import com.project.urlshortener.repository.LinkAccessRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LinkTrackingService {

    private final LinkAccessRepository repository;
    private final GeoLocationService geoLocationService;
    private final RedisTemplate<String, String> redisTemplate;
    private final SlackNotificationService slackService;

    public void track(UrlMapping mapping, HttpServletRequest request) {
        if (!mapping.isTrackingEnabled())
            return;

        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String location = geoLocationService.resolveLocation(ip);

        LinkAccessLog accessLog = new LinkAccessLog();
        accessLog.setTag(mapping.getTrackingTag());
        accessLog.setIpAddress(ip);
        accessLog.setUserAgent(userAgent);
        accessLog.setLocation(location);
        accessLog.setAccessedAt(LocalDateTime.now());

        repository.save(accessLog);

        notifyOncePerWindow(mapping, location, userAgent);
    }

    public String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For"); // ✅ FIX 2
        return forwarded != null ? forwarded.split(",")[0] : request.getRemoteAddr();
    }

    private void notifyOncePerWindow(
            UrlMapping mapping,
            String location,
            String userAgent) {

        String key = "notify:" + mapping.getShortCode();

        // Deduplicate notifications for 12h
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key)))
            return;

        redisTemplate.opsForValue()
                .set(key, "1", Duration.ofHours(12)); // ✅ FIX 3 (intentional)

        // 1. Send Slack Notification
        slackService.notify(
                "Link accessed\n" +
                        "Tag: " + mapping.getTrackingTag() + "\n" +
                        "Location: " + location + "\n" +
                        "User-Agent: " + userAgent);
    }
}
