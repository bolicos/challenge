package com.bolicos.challenge.domain.model;

import com.bolicos.challenge.domain.exception.DuplicateEmailException;
import com.bolicos.challenge.domain.exception.DomainException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class CommunicationPreference {

    @ToString.Include
    private UUID id;

    @ToString.Include
    private UUID customerId;

    @ToString.Include
    private CommunicationChannel communicationChannel;

    @ToString.Exclude
    private final List<PreferenceEmail> emails = new ArrayList<>();

    public void addEmail(PreferenceEmail email) {
        String normalizedEmail = normalizeEmail(email);
        boolean exists = emails.stream()
            .map(this::normalizeEmail)
            .anyMatch(normalizedEmail::equals);

        if (exists) throw new DuplicateEmailException("email already exists");
        this.emails.add(email);
    }

    public void replaceEmails(List<PreferenceEmail> newEmails) {
        if (newEmails == null) {
            this.emails.clear();
            return;
        }
        Set<String> lower = new HashSet<>();

        for (PreferenceEmail pe : newEmails) {
            String norm = normalizeEmail(pe);
            if (!lower.add(norm)) throw new DuplicateEmailException("duplicate email in list");
        }

        this.emails.clear();
        this.emails.addAll(newEmails);
    }

    private String normalizeEmail(PreferenceEmail email) {
        if (email == null || email.getEmail() == null || email.getEmail().isBlank()) {
            throw new DomainException("email is required");
        }

        return email.getEmail().trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommunicationPreference that = (CommunicationPreference) o;

        if (id == null || that.id == null) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
