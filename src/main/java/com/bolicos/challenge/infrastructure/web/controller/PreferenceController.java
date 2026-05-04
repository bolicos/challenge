package com.bolicos.challenge.infrastructure.web.controller;

import com.bolicos.challenge.infrastructure.web.dto.CommunicationPreferenceRequest;
import com.bolicos.challenge.infrastructure.web.dto.CommunicationPreferenceBatchRequest;
import com.bolicos.challenge.infrastructure.web.dto.CommunicationPreferenceBatchResponse;
import com.bolicos.challenge.infrastructure.web.dto.CommunicationPreferenceCsvImportResponse;
import com.bolicos.challenge.infrastructure.web.dto.CommunicationPreferenceResponse;
import com.bolicos.challenge.infrastructure.web.dto.CommunicationPreferenceSummaryResponse;
import com.bolicos.challenge.infrastructure.web.mapper.PreferenceWebMapper;
import com.bolicos.challenge.application.port.in.PreferenceUseCase;
import com.bolicos.challenge.infrastructure.batch.PreferenceCsvImportJobLauncher;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/preferencias", produces = MediaType.APPLICATION_JSON_VALUE)
public class PreferenceController {

    private final PreferenceUseCase preferenceUseCase;
    private final PreferenceWebMapper mapper;
    private final PreferenceCsvImportJobLauncher csvImportJobLauncher;

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

    @PostMapping(path = "/importacao", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommunicationPreferenceBatchResponse> importBatch(
        @Valid @RequestBody CommunicationPreferenceBatchRequest request
    ) {
        var result = preferenceUseCase.importBatch(mapper.toDomains(request.preferencias()));

        return ResponseEntity.ok(new CommunicationPreferenceBatchResponse(
            result.totalRecebido(),
            result.totalProcessado(),
            result.totalComErro(),
            mapper.toResponses(result.preferencias())
        ));
    }

    @PostMapping(path = "/importacao/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommunicationPreferenceCsvImportResponse> importCsv(
        @RequestParam("file") MultipartFile file
    ) {
        var execution = csvImportJobLauncher.launch(file);

        return ResponseEntity.ok(new CommunicationPreferenceCsvImportResponse(
            execution.getId(),
            csvImportJobLauncher.jobName(),
            execution.getStatus().name(),
            execution.getExitStatus().getExitCode()
        ));
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
