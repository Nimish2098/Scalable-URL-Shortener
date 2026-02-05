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
    private final EmailNotificationService notificationService;
    private final RedisTemplate<String,String> redisTemplate;


    public void track(UrlMapping mapping, HttpServletRequest request) {
        if (!mapping.isTrackingEnabled()) return;

        String ip = getClientIp(request);
        String useragent = request.getHeader("User-Agent");
        String location = geoLocationService.resolveLocation(ip);


        LinkAccessLog accessLog = new LinkAccessLog();
        accessLog.setTag(mapping.getTrackingTag());
        accessLog.setIpAddress(ip);
        accessLog.setUserAgent(useragent);
        accessLog.setLocation(location);
        accessLog.setAccessedAt(LocalDateTime.now());

        repository.save(accessLog);

        notifyOncePerWindow(mapping, location, useragent);


    }

    public String getClientIp(HttpServletRequest request) {
        String forwaded = request.getHeader("X-Forwaded-For");
        return forwaded!=null ? forwaded.split(",")[0]: request.getRemoteAddr();
    }

    private void notifyOncePerWindow(
            UrlMapping mapping,
            String location,
            String userAgent) {

        String key = "notify:" + mapping.getShortCode();

        if (redisTemplate.hasKey(key)) return;

        redisTemplate.opsForValue()
                .set(key, "1", Duration.ofHours(12));

        notificationService.notify(
                mapping.getTrackingTag(),
                location,
                userAgent
        );
    }


}
