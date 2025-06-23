package com.exp.LoggingMiddleware.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoggingMiddleware {

    private static final String LOG_API_URL = "http://20.244.56.144/evaluation-service/logs";
    private final RestTemplate restTemplate = new RestTemplate();
    private String accessToken = "eyJhbc..."; // Replace with actual token

    public void setAccessToken(String token) {
        this.accessToken = token;
    }

    public ResponseEntity<String> log(String stack, String level, String packageName, String message) {
        Map<String, String> logEntry = new HashMap<>();
        logEntry.put("stack", "backend");
        logEntry.put("level", level.toLowerCase());
        logEntry.put("package", packageName.toLowerCase());
        logEntry.put("message", message);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(logEntry, headers);
        try {
            return restTemplate.postForEntity(LOG_API_URL, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Logging failed: " + e.getMessage());
        }
    }
}