package com.exp.LoggingMiddleware.controller;

import com.exp.LoggingMiddleware.service.LoggingMiddleware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class LoggingController {

    @RestController
    public class ExampleController {

        @Autowired
        private LoggingMiddleware loggingMiddleware;

        @GetMapping("/test")
        public ResponseEntity<String> testLog(@RequestHeader(value = "Authorization", required = true) String authorizationHeader) {
            // Set the access token obtained from the token API
            System.out.println(authorizationHeader);
            loggingMiddleware.setAccessToken(authorizationHeader); // Replace with the full token
            return loggingMiddleware.log("backend", "debug", "handler", "Test debug message with token");
        }
    }


}
