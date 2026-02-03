package com.project.urlshortener.service.impl;

import com.project.urlshortener.exception.UrlExpiredException;
import com.project.urlshortener.exception.UrlNotFoundException;
import com.project.urlshortener.model.UrlMapping;
import com.project.urlshortener.repository.UrlMappingRepository;
import com.project.urlshortener.service.ShortCodeGenerator;
import com.project.urlshortener.service.UrlMappingService;
import com.project.urlshortener.service.UrlValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.InvalidUrlException;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlMappingServiceImpl implements UrlMappingService {

    private final UrlMappingRepository mappingRepository;
    private final ShortCodeGenerator generator;
    private final UrlValidationService urlValidationService;
    private final RedisTemplate<String,String> redisTemplate;
    @Override
    public String createShortUrl(String longUrl) {
        if (!urlValidationService.isValidUrl(longUrl)) {
            throw new InvalidUrlException("Invalid URL format");
        }
        longUrl = longUrl.trim();

        UrlMapping mapping = new UrlMapping();
        mapping.setLongUrl(longUrl);
        mapping.setCreatedAt(LocalDateTime.now());
        mapping.setClickCount(0L);

        mapping = mappingRepository.save(mapping);
        String shortCode = generator.generate(mapping.getId());

        mapping.setShortCode(shortCode);
        mappingRepository.save(mapping);
        return shortCode;
    }

    @Override
    public String getOriginalUrl(String code) {

        String cachedUrl = redisTemplate.opsForValue().get(code);
        if(cachedUrl!=null){
            return cachedUrl;
        }
        UrlMapping urlMapping = mappingRepository.findByShortCode(code)
                .orElseThrow(() -> new UrlNotFoundException("Short URL not found"));

        if (urlMapping.getExpiredAt() != null &&
                urlMapping.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException("URL has expired");
        }
        redisTemplate.opsForValue().set(code,urlMapping.getLongUrl());
        redisTemplate.opsForValue()
                .set(code, urlMapping.getLongUrl(), Duration.ofHours(24));

        urlMapping.setClickCount(urlMapping.getClickCount()+1);
        mappingRepository.save(urlMapping);
        return urlMapping.getLongUrl();

    }
}
