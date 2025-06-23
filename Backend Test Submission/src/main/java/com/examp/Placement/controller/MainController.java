package com.examp.Placement.controller;

import com.examp.Placement.model.UrlRequest;
import com.examp.Placement.service.LoggingMiddleware;
import com.examp.Placement.service.UrlShortenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class MainController {

    @Autowired
    private UrlShortenerService urlShortenerService;
    @Autowired
    private LoggingMiddleware loggingMiddleware;

    @PostMapping("/shorturls")
    public ResponseEntity<?> createShortUrl(
            @RequestBody UrlRequest request,
            @RequestHeader(value = "Authorization", required = true) String authorizationHeader) {
        // Extract and set token details for logging
        loggingMiddleware.setTokenType("Bearer");
        String accessToken = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7).trim() : authorizationHeader.trim();
        if (accessToken.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or empty access token"));
        }
        loggingMiddleware.setAccessToken(accessToken);
        System.out.println("Received Authorization header: " + authorizationHeader); // Debug log

        try {
            loggingMiddleware.log("backend", "info", "service", "Shortening URL");
            Map<String, Object> response = urlShortenerService.shortenUrl(request.getUrl(), request.getValidity(), request.getShortcode(), authorizationHeader);
            loggingMiddleware.log("backend", "info", "service", "URL shortened");
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            loggingMiddleware.log("backend", "error", "service", "Invalid input");
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            loggingMiddleware.log("backend", "error", "service", "Shortcode collision");
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/shorturls/{shortcode}")
    public ResponseEntity<?> getUrlStats(
            @PathVariable String shortcode,
            @RequestHeader(value = "Authorization", required = true) String authorizationHeader) {
        // Extract and set token details for logging
        loggingMiddleware.setTokenType("Bearer");
        String accessToken = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7).trim() : authorizationHeader.trim();
        if (accessToken.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or empty access token"));
        }
        loggingMiddleware.setAccessToken(accessToken);

        try {
            loggingMiddleware.log("backend", "info", "service", "Retrieving stats");
            return ok(urlShortenerService.getUrlStats(shortcode, authorizationHeader));
        } catch (IllegalArgumentException e) {
            loggingMiddleware.log("backend", "error", "service", "Non-existent shortcode");
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            loggingMiddleware.log("backend", "error", "service", "Expired shortcode");
            return ResponseEntity.status(410).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{shortcode}")
    public ResponseEntity<?> redirect(
            @PathVariable String shortcode,
            @RequestHeader(value = "Authorization", required = true) String authorizationHeader) {
        // Extract and set token details for logging
        loggingMiddleware.setTokenType("Bearer");
        String accessToken = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7).trim() : authorizationHeader.trim();
        if (accessToken.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or empty access token"));
        }
        loggingMiddleware.setAccessToken(accessToken);

        try {
            loggingMiddleware.log("backend", "info", "service", "Redirecting");
            String originalUrl = urlShortenerService.redirect(shortcode, authorizationHeader);
            return ResponseEntity.status(302).location(java.net.URI.create(originalUrl)).build();
        } catch (IllegalArgumentException e) {
            loggingMiddleware.log("backend", "error", "service", "Non-existent shortcode");
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            loggingMiddleware.log("backend", "error", "service", "Expired shortcode");
            return ResponseEntity.status(410).body(Map.of("error", e.getMessage()));
        }
    }
}