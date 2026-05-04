package com.bolicos.challenge.application.port.out;

import com.bolicos.challenge.application.event.PreferenceChangedEvent;

public interface PreferenceEventPublisher {

    void publish(PreferenceChangedEvent event);
}
