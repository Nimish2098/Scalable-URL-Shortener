package com.project.urlshortener.service;


public interface UrlMappingService {

    public String createShortUrl(String longUrl);
    public String getOriginalUrl(String code);
}
