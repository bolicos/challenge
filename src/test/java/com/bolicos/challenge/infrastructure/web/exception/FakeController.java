package com.bolicos.challenge.infrastructure.web.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpMethod;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestController
@Validated
class FakeController {

    @PostMapping("/fake/validation")
    void validation(@Valid @RequestBody FakeRequest request) {
    }

    @PostMapping("/fake/read")
    void read(@RequestBody FakeRequest request) {
    }

    @GetMapping("/fake/constraint")
    void constraint(@RequestParam @NotBlank String name) {
    }

    @GetMapping("/fake/not-found")
    void notFound() throws NoResourceFoundException {
        throw new NoResourceFoundException(HttpMethod.GET, "/fake/not-found");
    }

    @GetMapping("/fake/error")
    void error() {
        throw new IllegalStateException("boom");
    }
}

record FakeRequest(@NotBlank String name) {
}
