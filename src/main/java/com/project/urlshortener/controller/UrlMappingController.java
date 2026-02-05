package com.project.urlshortener.controller;

import com.project.urlshortener.dto.ShortenUrlRequest;
import com.project.urlshortener.dto.UrlStatsResponse;
import com.project.urlshortener.model.UrlMapping;
import com.project.urlshortener.service.UrlMappingService;
import com.project.urlshortener.service.impl.LinkTrackingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UrlMappingController {

    private final UrlMappingService urlMappingService;
    private final LinkTrackingService linkTrackingService;

    @PostMapping("/api/shorten")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String shortenURL(@RequestBody ShortenUrlRequest request) {
        return urlMappingService.createShortUrl(
                request.getLongUrl(),
                request.getExpiresAt()
        );
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(
            @PathVariable String code,
            HttpServletRequest request) {

        UrlMapping mapping = urlMappingService.getMapping(code,request);

        linkTrackingService.track(mapping, request);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(mapping.getLongUrl()))
                .build();
    }

    @GetMapping("/api/stats/{code}")
    public UrlStatsResponse getStats(@PathVariable String code) {
        return urlMappingService.getStats(code);
    }
}
