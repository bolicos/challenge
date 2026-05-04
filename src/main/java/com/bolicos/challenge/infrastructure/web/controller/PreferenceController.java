package com.bolicos.challenge.infrastructure.web.controller;

import com.bolicos.challenge.infrastructure.web.dto.CommunicationPreferenceRequest;
import com.bolicos.challenge.infrastructure.web.dto.CommunicationPreferenceResponse;
import com.bolicos.challenge.infrastructure.web.dto.CommunicationPreferenceSummaryResponse;
import com.bolicos.challenge.infrastructure.web.mapper.PreferenceWebMapper;
import com.bolicos.challenge.application.port.in.PreferenceUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/preferencias", produces = MediaType.APPLICATION_JSON_VALUE)
public class PreferenceController {

    private final PreferenceWebMapper mapper;
    private final PreferenceUseCase preferenceUseCase;

    @GetMapping
    public ResponseEntity<List<CommunicationPreferenceResponse>> list() {
        return ResponseEntity.ok(mapper.toResponses(preferenceUseCase.findAll()));
    }

    @GetMapping("/resumo")
    public ResponseEntity<List<CommunicationPreferenceSummaryResponse>> summary() {
        return ResponseEntity.ok(mapper.toSummaryResponses(preferenceUseCase.findSummary()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommunicationPreferenceResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponse(preferenceUseCase.findById(id)));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommunicationPreferenceResponse> create(
        @Valid @RequestBody CommunicationPreferenceRequest request
    ) {
        var created = preferenceUseCase.create(mapper.toDomain(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();

        return ResponseEntity.created(location).body(mapper.toResponse(created));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommunicationPreferenceResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody CommunicationPreferenceRequest request
    ) {
        return ResponseEntity.ok(mapper.toResponse(preferenceUseCase.update(id, mapper.toDomain(request))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        preferenceUseCase.delete(id);
        return ResponseEntity.ok().build();
    }
}
