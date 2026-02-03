package com.project.urlshortener.service.impl;

import com.project.urlshortener.service.UrlValidationService;

import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class UrlValidationServiceImpl implements UrlValidationService {

    // Checking if the URL is valid or not
    public boolean isValidUrl(String url) {
        if (url == null || url.isBlank())
            return false;

        try {
            URI uri = new URI(url.trim());
            return uri.getScheme() != null &&
                    ("http".equalsIgnoreCase(uri.getScheme())
                            || "https".equalsIgnoreCase(uri.getScheme()))
                    &&
                    uri.getHost() != null;
        } catch (Exception e) {
            return false;
        }
    }

}
