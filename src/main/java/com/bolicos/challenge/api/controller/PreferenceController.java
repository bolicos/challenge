package com.bolicos.challenge.api.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Set;

@RestController
@RequestMapping(path = "/api/preferences", produces = MediaType.APPLICATION_JSON_VALUE)
public class PreferenceController {

    @GetMapping
    public Set<String> all() {
        return Collections.emptySet();
    }
}
