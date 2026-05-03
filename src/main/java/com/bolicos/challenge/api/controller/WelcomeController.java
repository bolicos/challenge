package com.bolicos.challenge.api.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class WelcomeController {

    @GetMapping
    public WelcomeResponse welcome() {
        return new WelcomeResponse("ok", "API Challenge is running");
    }

    public record WelcomeResponse(String status, String message) {
    }
}
