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
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UrlMappingServiceImpl implements UrlMappingService {

    private final UrlMappingRepository mappingRepository;
    private final ShortCodeGenerator generator;
    private final UrlValidationService urlValidationService;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public String createShortUrl(String longUrl, LocalDateTime expiresAt) {

        if (!urlValidationService.isValidUrl(longUrl)) {
            throw new InvalidUrlException("Invalid URL");
        }

        UrlMapping mapping = new UrlMapping();
        mapping.setLongUrl(longUrl.trim());
        mapping.setCreatedAt(LocalDateTime.now());
        mapping.setExpiredAt(expiresAt);
        mapping.setClickCount(0L);

        mapping = mappingRepository.save(mapping);

        String shortCode = generator.generate(mapping.getId());
        mapping.setShortCode(shortCode);
        mappingRepository.save(mapping);

        if (expiresAt != null) {
            long ttlSeconds = Duration.between(
                    LocalDateTime.now(),
                    expiresAt
            ).getSeconds();

            redisTemplate.opsForValue()
                    .set(shortCode, mapping.getLongUrl(), ttlSeconds, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue()
                    .set(shortCode, mapping.getLongUrl(), Duration.ofHours(24));
        }

        return shortCode;
    }

    @Override
    public UrlMapping getMapping(String code, HttpServletRequest request) {

        UrlMapping mapping = mappingRepository.findByShortCode(code)
                .orElseThrow(() -> new UrlNotFoundException("Short URL not found"));

        if (mapping.getExpiredAt() != null &&
                mapping.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException("URL expired");
        }

        mapping.setClickCount(mapping.getClickCount() + 1);
        mappingRepository.save(mapping);

        return mapping;
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
                expired
        );
    }
}
