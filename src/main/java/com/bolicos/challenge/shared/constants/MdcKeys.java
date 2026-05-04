package com.bolicos.challenge.shared.constants;

public final class MdcKeys {

    private MdcKeys() {
    }

    public static final String CORRELATION_ID = "correlationId";
    public static final String EVENT_ID = "eventId";
    public static final String EVENT_TYPE = "eventType";
    public static final String METHOD = "method";
    public static final String OFFSET = "offset";
    public static final String PARTITION = "partition";
    public static final String PATH = "path";
    public static final String SOURCE = "source";
    public static final String TOPIC = "topic";

    public static final String WEB_SOURCE = "web";
    public static final String KAFKA_SOURCE = "kafka";
}
