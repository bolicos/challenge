package com.bolicos.challenge.infrastructure.persistence.entity;

import com.bolicos.challenge.domain.model.CommunicationChannel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "communication_preferences")
public class CommunicationPreferenceEntity extends AuditableEntity {

    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ToString.Include
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @ToString.Include
    @Enumerated(EnumType.STRING)
    @Column(name = "communication_channel", nullable = false, length = 20)
    private CommunicationChannel communicationChannel;

    @Builder.Default
    @OneToMany(
        mappedBy = "preference",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<PreferenceEmailEntity> emails = new ArrayList<>();

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CommunicationPreferenceEntity that = (CommunicationPreferenceEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
