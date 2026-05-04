package com.bolicos.challenge.shared.constants;

public final class KafkaKeys {

    private KafkaKeys() {
    }

    public static final String EVENT_ID_HEADER = "eventId";
    public static final String EVENT_TYPE_HEADER = "eventType";
    public static final String CUSTOMER_ID_HEADER = "customerId";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    public static final String PREFERENCE_EVENTS_TOPIC_PROPERTY = "${challenge.kafka.topics.preference-events}";
    public static final String PREFERENCE_EVENTS_TOPIC_PROPERTY_WITH_DEFAULT =
        "${challenge.kafka.topics.preference-events:communication-preference-events}";
    public static final String PREFERENCE_EVENT_LOGGER_GROUP_ID_PROPERTY =
        "${challenge.kafka.consumers.preference-event-logger.group-id:challenge-preference-event-logger}";
    public static final String PREFERENCE_EVENT_LOGGER_ENABLED_PROPERTY =
        "${challenge.kafka.consumers.preference-event-logger.enabled:false}";
}
