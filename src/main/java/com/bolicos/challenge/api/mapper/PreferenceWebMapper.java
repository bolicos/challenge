package com.bolicos.challenge.api.mapper;

import com.bolicos.challenge.api.dto.CommunicationPreferenceRequest;
import com.bolicos.challenge.api.dto.CommunicationPreferenceResponse;
import com.bolicos.challenge.api.dto.PreferenceEmailRequest;
import com.bolicos.challenge.api.dto.PreferenceEmailResponse;
import com.bolicos.challenge.application.model.CommunicationPreferenceView;
import com.bolicos.challenge.application.model.PreferenceEmailView;
import com.bolicos.challenge.domain.model.CommunicationPreference;
import com.bolicos.challenge.domain.model.PreferenceEmail;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PreferenceWebMapper {

    public CommunicationPreference toDomain(CommunicationPreferenceRequest request) {
        var preference = new CommunicationPreference();
        preference.setCommunicationChannel(request.preferenciaCanalComunicacao());
        preference.replaceEmails(toDomainEmails(request.emails()));

        return preference;
    }

    public CommunicationPreferenceResponse toResponse(CommunicationPreferenceView view) {
        return new CommunicationPreferenceResponse(
            view.id(),
            view.communicationChannel(),
            view.audit().dataAtualizacao(),
            view.audit().dataCriacao(),
            view.audit().criadoPor(),
            view.audit().alteradoPor(),
            toEmailResponses(view.emails())
        );
    }

    public List<CommunicationPreferenceResponse> toResponses(List<CommunicationPreferenceView> views) {
        return views.stream().map(this::toResponse).toList();
    }

    private List<PreferenceEmail> toDomainEmails(List<PreferenceEmailRequest> emails) {
        if (emails == null || emails.isEmpty()) {
            return List.of();
        }

        return emails.stream().map(this::toDomainEmail).toList();
    }

    private PreferenceEmail toDomainEmail(PreferenceEmailRequest request) {
        var email = new PreferenceEmail();
        email.setId(request.id());
        email.setEmail(request.email());
        email.setType(request.tipo());
        email.setVerified(request.verificado());

        return email;
    }

    private List<PreferenceEmailResponse> toEmailResponses(List<PreferenceEmailView> emails) {
        if (emails == null || emails.isEmpty()) {
            return List.of();
        }

        return emails.stream().map(this::toEmailResponse).toList();
    }

    private PreferenceEmailResponse toEmailResponse(PreferenceEmailView view) {
        return new PreferenceEmailResponse(
            view.id(),
            view.email(),
            view.type(),
            view.verified(),
            view.audit().dataAtualizacao(),
            view.audit().dataCriacao(),
            view.audit().criadoPor(),
            view.audit().alteradoPor()
        );
    }
}
