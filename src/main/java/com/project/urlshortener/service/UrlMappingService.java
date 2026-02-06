package com.project.urlshortener.service;

import com.project.urlshortener.dto.UrlStatsResponse;
import com.project.urlshortener.model.UrlMapping;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;

public interface UrlMappingService {

    public String createShortUrl(String longUrl, String trackingTag, LocalDateTime expiresAt);

    public UrlMapping getMapping(String code, HttpServletRequest request);

    public UrlStatsResponse getStats(String shortCode);

    public java.util.List<UrlStatsResponse> getStatsByTag(String tag);

    public java.util.List<UrlStatsResponse> getAllLinks(int page, int size);

}
