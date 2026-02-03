package com.project.urlshortener.controller;

import com.project.urlshortener.dto.ShortenUrlRequest;
import com.project.urlshortener.service.UrlMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UrlMappingController {

    private final UrlMappingService urlMappingService;

    @PostMapping("/shorten")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String shortenURL(@RequestBody ShortenUrlRequest request) {
        return urlMappingService.createShortUrl(request.getLongUrl());
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String longUrl = urlMappingService.getOriginalUrl(code);

        return ResponseEntity
                .status(HttpStatus.FOUND)   // 302
                .location(URI.create(longUrl))
                .build();
    }


}
