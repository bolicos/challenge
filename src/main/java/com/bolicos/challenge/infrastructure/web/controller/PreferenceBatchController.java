package com.bolicos.challenge.infrastructure.web.controller;

import com.bolicos.challenge.application.port.in.PreferenceBatchUseCase;
import com.bolicos.challenge.infrastructure.batch.PreferenceCsvImportJobLauncher;
import com.bolicos.challenge.infrastructure.web.dto.CommunicationPreferenceBatchRequest;
import com.bolicos.challenge.infrastructure.web.dto.CommunicationPreferenceBatchResponse;
import com.bolicos.challenge.infrastructure.web.dto.CommunicationPreferenceCsvImportResponse;
import com.bolicos.challenge.infrastructure.web.mapper.PreferenceWebMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/preferencias/importacao", produces = MediaType.APPLICATION_JSON_VALUE)
public class PreferenceBatchController {

    private final PreferenceWebMapper mapper;
    private final PreferenceBatchUseCase preferenceBatchUseCase;
    private final PreferenceCsvImportJobLauncher csvImportJobLauncher;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommunicationPreferenceBatchResponse> importBatch(
        @Valid @RequestBody CommunicationPreferenceBatchRequest request
    ) {
        var result = preferenceBatchUseCase.importBatch(mapper.toDomains(request.preferencias()));

        return ResponseEntity.ok(new CommunicationPreferenceBatchResponse(
            result.totalRecebido(),
            result.totalProcessado(),
            result.totalComErro(),
            mapper.toResponses(result.preferencias())
        ));
    }

    @PostMapping(path = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
}
