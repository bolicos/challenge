package com.bolicos.challenge.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class PreferenceEmail {

    @ToString.Include
    private Long id;

    @ToString.Include
    private String email;

    @ToString.Include
    private EmailType type;

    @ToString.Include
    private Boolean verified;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PreferenceEmail that = (PreferenceEmail) o;

        if (id == null || that.id == null) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
