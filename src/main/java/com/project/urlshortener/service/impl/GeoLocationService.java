package com.project.urlshortener.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeoLocationService {

    private final RestTemplate restTemplate;

    public String resolveLocation(String ip) {
        try {
            String url = "https://ipapi.co/" + ip + "/json";
            Map response = restTemplate.getForObject(url, Map.class);

            if (response == null) return "Unknown";

            String city = (String) response.get("city");
            String region = (String) response.get("region");
            String country = (String) response.get("country_name");

            return city + ", " + region + ", " + country;
        } catch (Exception e) {
            return "Unknown";
        }
    }
}
