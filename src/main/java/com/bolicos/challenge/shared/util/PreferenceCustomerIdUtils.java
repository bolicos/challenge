package com.bolicos.challenge.shared.util;

import com.bolicos.challenge.domain.model.CommunicationPreference;

import java.util.UUID;

public final class PreferenceCustomerIdUtils {

    private PreferenceCustomerIdUtils() {
    }

    public static void ensureCustomerId(CommunicationPreference preference) {
        if (preference.getCustomerId() == null) {
            preference.setCustomerId(UUID.randomUUID());
        }
    }
}
