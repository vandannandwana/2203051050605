package com.examp.Placement.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoggingMiddleware {

    private static final String LOG_API_URL = "http://20.244.56.144/evaluation-service/logs";
    private final RestTemplate restTemplate = new RestTemplate();
    private String accessToken = null;
    private String tokenType = null;

    private static final Map<String, String[]> validValues = new HashMap<>() {{
        put("stack", new String[]{"frontend", "backend"});
        put("level", new String[]{"debug", "info", "warn", "error", "fatal"});
        put("package", new String[]{"api", "handler", "receiver", "cache", "controller", "cron_job", "db", "repository", "route", "service"});
    }};

    public void setAccessToken(String token) {
        this.accessToken = token;
    }

    public void setTokenType(String type) {
        this.tokenType = type;
    }

    public ResponseEntity<String> log(String stack, String level, String packageName, String message) {
        // Validate inputs
        if (!isValidValue("stack", stack.toLowerCase()) ||
                !isValidValue("level", level.toLowerCase()) ||
                !isValidValue("package", packageName.toLowerCase()) ||
                message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid stack, level, package, or message value");
        }

        // Truncate message to 48 characters to comply with API constraint
        String truncatedMessage = message.length() > 48 ? message.substring(0, 48) : message;

        // Prepare log entry
        Map<String, String> logEntry = new HashMap<>();
        logEntry.put("stack", stack.toLowerCase());
        logEntry.put("level", level.toLowerCase());
        logEntry.put("package", packageName.toLowerCase());
        logEntry.put("message", truncatedMessage);

        // Set headers with authentication
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (tokenType != null && accessToken != null && !accessToken.isEmpty()) {
            String authHeader = tokenType + " " + accessToken;
            headers.set("Authorization", authHeader);
            System.out.println("Sending Authorization header: " + authHeader); // Debug log
        } else {
            throw new IllegalStateException("Token type or access token is not set or empty");
        }

        // Make API call
        HttpEntity<Map<String, String>> request = new HttpEntity<>(logEntry, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(LOG_API_URL, request, String.class);
            System.out.println("Logging response: " + response.getStatusCode() + " - " + response.getBody()); // Debug log
            return response;
        } catch (Exception e) {
            System.out.println("Logging failed: " + e.getMessage()); // Debug log
            throw new RuntimeException("Failed to log: " + e.getMessage());
        }
    }

    private boolean isValidValue(String key, String value) {
        return validValues.containsKey(key) && java.util.Arrays.stream(validValues.get(key)).anyMatch(value::equals);
    }
}