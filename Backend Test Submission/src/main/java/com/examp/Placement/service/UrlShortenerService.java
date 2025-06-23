package com.examp.Placement.service;

import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UrlShortenerService {

    private final Map<String, UrlMapping> urlMappings = new HashMap<>();
    private final LoggingMiddleware loggingMiddleware;

    public UrlShortenerService(LoggingMiddleware loggingMiddleware) {
        this.loggingMiddleware = loggingMiddleware;
    }

    public static class UrlMapping {
        String originalUrl;
        Instant expiry;
        Map<String, ClickData> clickData = new HashMap<>();

        UrlMapping(String originalUrl, Instant expiry) {
            this.originalUrl = originalUrl;
            this.expiry = expiry;
        }
    }

    public static class ClickData {
        Instant timestamp;
        String referrer;
        String location;

        ClickData(Instant timestamp, String referrer, String location) {
            this.timestamp = timestamp;
            this.referrer = referrer;
            this.location = location;
        }
    }

    public Map<String, Object> shortenUrl(String url, Integer validity, String shortcode, String authorizationHeader) {
        loggingMiddleware.log("backend", "info", "service", "Shortening URL");

        if (!isValidUrl(url)) {
            loggingMiddleware.log("backend", "error", "service", "Invalid URL");
            throw new IllegalArgumentException("Invalid URL format");
        }

        if (shortcode != null && !isValidShortcode(shortcode)) {
            loggingMiddleware.log("backend", "error", "service", "Invalid shortcode");
            throw new IllegalArgumentException("Invalid shortcode format");
        }

        Instant expiry = Instant.now().plus(validity != null ? validity : 30, ChronoUnit.MINUTES);
        String finalShortcode = shortcode != null && !urlMappings.containsKey(shortcode) ? shortcode : generateUniqueShortcode();

        if (urlMappings.containsKey(finalShortcode)) {
            loggingMiddleware.log("backend", "error", "service", "Shortcode collision");
            throw new IllegalStateException("Shortcode collision");
        }

        urlMappings.put(finalShortcode, new UrlMapping(url, expiry));
        loggingMiddleware.log("backend", "info", "service", "URL shortened");

        return Map.of(
                "shortLink", "http://localhost:8080/" + finalShortcode,
                "expiry", expiry.toString()
        );
    }

    public Map<String, Object> getUrlStats(String shortcode, String authorizationHeader) {
        loggingMiddleware.log("backend", "info", "service", "Retrieving stats");

        UrlMapping mapping = urlMappings.get(shortcode);
        if (mapping == null) {
            loggingMiddleware.log("backend", "error", "service", "Non-existent shortcode");
            throw new IllegalArgumentException("Shortcode does not exist");
        }

        if (Instant.now().isAfter(mapping.expiry)) {
            loggingMiddleware.log("backend", "error", "service", "Expired shortcode");
            throw new IllegalStateException("Link has expired");
        }

        return Map.of(
                "shortLink", "http://localhost:8080/" + shortcode,
                "expiry", mapping.expiry.toString(),
                "totalClicks", mapping.clickData.size(),
                "originalUrl", mapping.originalUrl,
                "creationDate", mapping.expiry.minus(30, ChronoUnit.MINUTES).toString(),
                "clickData", mapping.clickData.values()
        );
    }

    public String redirect(String shortcode, String authorizationHeader) {
        loggingMiddleware.log("backend", "info", "service", "Redirecting");

        UrlMapping mapping = urlMappings.get(shortcode);
        if (mapping == null) {
            loggingMiddleware.log("backend", "error", "service", "Non-existent shortcode");
            throw new IllegalArgumentException("Shortcode does not exist");
        }

        if (Instant.now().isAfter(mapping.expiry)) {
            loggingMiddleware.log("backend", "error", "service", "Expired shortcode");
            throw new IllegalStateException("Link has expired");
        }

        mapping.clickData.put(UUID.randomUUID().toString(), new ClickData(Instant.now(), "referrer", "location"));
        return mapping.originalUrl;
    }

    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidShortcode(String shortcode) {
        return shortcode != null && shortcode.matches("^[a-zA-Z0-9]{1,10}$");
    }

    private String generateUniqueShortcode() {
        String shortcode;
        do {
            shortcode = UUID.randomUUID().toString().substring(0, 6).replaceAll("-", "");
        } while (urlMappings.containsKey(shortcode));
        return shortcode;
    }
}