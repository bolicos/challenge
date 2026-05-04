package com.bolicos.challenge.shared.constants;

public final class MetricsKeys {

    private MetricsKeys() {
    }

    public static final String EVENT_TYPE_TAG = "event.type";
    public static final String JOB_NAME_TAG = "job.name";

    public static final String KAFKA_PREFERENCE_EVENTS_PUBLISH_ATTEMPT =
        "challenge.kafka.preference.events.publish.attempt";
    public static final String KAFKA_PREFERENCE_EVENTS_PUBLISH_SUCCESS =
        "challenge.kafka.preference.events.publish.success";
    public static final String KAFKA_PREFERENCE_EVENTS_PUBLISH_FAILURE =
        "challenge.kafka.preference.events.publish.failure";

    public static final String BATCH_PREFERENCE_CSV_IMPORT_ATTEMPT =
        "challenge.batch.preference.csv.import.attempt";
    public static final String BATCH_PREFERENCE_CSV_IMPORT_SUCCESS =
        "challenge.batch.preference.csv.import.success";
    public static final String BATCH_PREFERENCE_CSV_IMPORT_FAILURE =
        "challenge.batch.preference.csv.import.failure";
    public static final String BATCH_PREFERENCE_CSV_IMPORT_DURATION =
        "challenge.batch.preference.csv.import.duration";
}
