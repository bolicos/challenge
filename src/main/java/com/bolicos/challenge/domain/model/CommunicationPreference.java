package com.bolicos.challenge.domain.model;

import com.bolicos.challenge.domain.exception.DuplicateEmailException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        // exemplo de regra: não duplicar por endereço
        boolean exists = emails.stream().anyMatch(e -> e.getEmail().equalsIgnoreCase(email.getEmail()));
        if (exists) throw new DuplicateEmailException("email already exists");
        this.emails.add(email);
    }

    public void replaceEmails(List<PreferenceEmail> newEmails) {
        if (newEmails == null) {
            this.emails.clear();
            return;
        }
        // validar duplicatas dentro de newEmails
        Set<String> lower = new HashSet<>();

        for (PreferenceEmail pe : newEmails) {
            String norm = pe.getEmail().toLowerCase();
            if (!lower.add(norm)) throw new DuplicateEmailException("duplicate email in list");
        }

        this.emails.clear();
        this.emails.addAll(newEmails);
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
        return getClass().hashCode();
    }
}
