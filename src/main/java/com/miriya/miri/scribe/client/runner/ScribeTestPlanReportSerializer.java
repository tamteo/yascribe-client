package com.miriya.miri.scribe.client.runner;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Defined a json serializer of test plan reports.
 */
public class ScribeTestPlanReportSerializer extends StdSerializer<ScribeTestPlanRunner> {
    public static final String SCRIBE_FIELD = "scribe";
    public static final String BROWSER_FIELD = "browser";
    public static final String SCENARIOS_FIELD = "scenarios";
    public static final String DATE_FIELD = "date";
    public static final String TIME_FIELD = "timeInMs";
    public static final String NB_SCENARIOS_FIELD = "nbScenarios";
    public static final String NB_STEPS_FIELD = "nbSteps";
    public static final String FAILURES_FIELD = "failures";
    public static final String ABORTED_FIELD = "aborted";
    public static final String SUCCESSFUL_FIELD = "successful";

    public ScribeTestPlanReportSerializer() {
        this(null);
    }
    public ScribeTestPlanReportSerializer(Class<ScribeTestPlanRunner> t) {
        super(t);
    }

    @Override
    public void serialize(ScribeTestPlanRunner data, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        gen.writeStringField(SCRIBE_FIELD, data.getScribe());
        gen.writeStringField(BROWSER_FIELD, data.getBrowser());
        gen.writeObjectField(DATE_FIELD, data.getDate());
        gen.writeNumberField(TIME_FIELD, data.getDuration().toMillis());
        gen.writeNumberField(NB_SCENARIOS_FIELD, data.getScenarioCount());
        gen.writeNumberField(NB_STEPS_FIELD, data.getStepCount());
        gen.writeNumberField(SUCCESSFUL_FIELD, data.getSuccessfulCount());
        gen.writeNumberField(FAILURES_FIELD, data.getFailureCount());
        gen.writeNumberField(ABORTED_FIELD, data.getAbortedCount());

        // write scenarios
        gen.writeArrayFieldStart(SCENARIOS_FIELD);
        for (ScribeTestPlanScenarioRunner scenario : data.getScenarios()) {
            gen.writeObject(scenario);
        }
        gen.writeEndArray();

        gen.writeEndObject();
    }
}
