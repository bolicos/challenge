package com.bolicos.challenge.shared.util;

import com.bolicos.challenge.application.event.PreferenceChangedEvent;
import com.bolicos.challenge.application.port.out.PreferenceEventPublisher;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public final class TransactionEventPublisherUtils {

    private TransactionEventPublisherUtils() {
    }

    public static void publishAfterCommit(
        PreferenceEventPublisher eventPublisher,
        PreferenceChangedEvent event
    ) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            eventPublisher.publish(event);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publish(event);
            }
        });
    }
}
