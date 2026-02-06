package com.project.urlshortener.service.impl;

import com.project.urlshortener.dto.UrlStatsResponse;
import com.project.urlshortener.exception.InvalidUrlException;
import com.project.urlshortener.exception.UrlExpiredException;
import com.project.urlshortener.exception.UrlNotFoundException;
import com.project.urlshortener.model.UrlMapping;
import com.project.urlshortener.repository.UrlMappingRepository;
import com.project.urlshortener.service.ShortCodeGenerator;
import com.project.urlshortener.service.UrlMappingService;
import com.project.urlshortener.service.UrlValidationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlMappingServiceImpl implements UrlMappingService {

    private final UrlMappingRepository mappingRepository;
    private final ShortCodeGenerator generator;
    private final UrlValidationService urlValidationService;
    private final RedisTemplate<String, UrlMapping> redisTemplate;

    @Override
    public String createShortUrl(String longUrl, String trackingTag, LocalDateTime expiresAt) {

        if (!urlValidationService.isValidUrl(longUrl)) {
            throw new InvalidUrlException("Invalid URL");
        }

        UrlMapping mapping = new UrlMapping();
        mapping.setLongUrl(longUrl.trim());
        mapping.setCreatedAt(LocalDateTime.now());
        mapping.setExpiredAt(expiresAt);

        mapping.setTrackingTag(trackingTag != null ? trackingTag : "Generic");
        mapping.setClickCount(0L);

        mapping = mappingRepository.save(mapping);

        String shortCode = generator.generate(mapping.getId());
        mapping.setShortCode(shortCode);
        mappingRepository.save(mapping);
        Duration ttl = computeTtl(mapping.getExpiredAt());
        if (ttl != null) {
            redisTemplate.opsForValue().set(
                    "shortUrl:" + shortCode,
                    mapping,
                    ttl);
        }

        return shortCode;
    }

    @Override
    public UrlMapping getMapping(String code, HttpServletRequest request) {

        String cacheKey = "shortUrl:" + code;

        // 1. Try Redis
        UrlMapping mapping = redisTemplate.opsForValue().get(cacheKey);

        if (mapping == null) {
            // 2. DB fallback
            mapping = mappingRepository.findByShortCode(code)
                    .orElseThrow(() -> new UrlNotFoundException("Short URL not found"));

            // 3. Expiry check
            if (mapping.getExpiredAt() != null &&
                    mapping.getExpiredAt().isBefore(LocalDateTime.now())) {
                throw new UrlExpiredException("URL expired");
            }

            // 4. Cache with TTL
            Duration ttl = computeTtl(mapping.getExpiredAt());
            if (ttl != null) {
                redisTemplate.opsForValue().set(cacheKey, mapping, ttl);
            }
        }

        // 5. Increment clicks (DB is source of truth)
        mapping.setClickCount(mapping.getClickCount() + 1);
        mappingRepository.save(mapping);

        return mapping;
    }

    private Duration computeTtl(LocalDateTime expiredAt) {
        if (expiredAt == null) {
            return Duration.ofDays(30); // default TTL for non-expiring URLs
        }

        Duration ttl = Duration.between(LocalDateTime.now(), expiredAt);

        // If already expired, don't cache
        return ttl.isNegative() || ttl.isZero() ? null : ttl;
    }

    @Override
    public UrlStatsResponse getStats(String code) {

        UrlMapping mapping = mappingRepository.findByShortCode(code)
                .orElseThrow(() -> new UrlNotFoundException("URL not found"));

        boolean expired = mapping.getExpiredAt() != null &&
                mapping.getExpiredAt().isBefore(LocalDateTime.now());

        return new UrlStatsResponse(
                mapping.getShortCode(),
                mapping.getLongUrl(),
                mapping.getClickCount(),
                mapping.getCreatedAt(),
                mapping.getExpiredAt(),
                expired);
    }

    @Override
    public java.util.List<UrlStatsResponse> getStatsByTag(String tag) {
        return mappingRepository.findByTrackingTag(tag).stream()
                .map(mapping -> {
                    boolean expired = mapping.getExpiredAt() != null &&
                            mapping.getExpiredAt().isBefore(LocalDateTime.now());
                    return new UrlStatsResponse(
                            mapping.getShortCode(),
                            mapping.getLongUrl(),
                            mapping.getClickCount(),
                            mapping.getCreatedAt(),
                            mapping.getExpiredAt(),
                            expired);
                })
                .collect(java.util.stream.Collectors.toList());

    }

    @Override
    public java.util.List<UrlStatsResponse> getAllLinks(int page, int size) {
        return mappingRepository
                .findAll(org.springframework.data.domain.PageRequest.of(page, size,
                        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
                                "createdAt")))
                .stream()
                .map(mapping -> {
                    boolean expired = mapping.getExpiredAt() != null &&
                            mapping.getExpiredAt().isBefore(LocalDateTime.now());
                    return new UrlStatsResponse(
                            mapping.getShortCode(),
                            mapping.getLongUrl(),
                            mapping.getClickCount(),
                            mapping.getCreatedAt(),
                            mapping.getExpiredAt(),
                            expired);
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
