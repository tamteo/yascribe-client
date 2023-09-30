package com.miriya.miri.scribe.client.runner;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Defined a json serializer of test scenario reports.
 */
public class ScribeTestPlanReportScenarioSerializer extends StdSerializer<ScribeTestPlanScenarioRunner> {
    public static final String SCENARIO_FIELD = "scenario";
    public static final String STEPS_FIELD = "steps";
    public static final String TIME_FIELD = "timeInMs";
    public static final String FAILURES_FIELD = "failures";
    public static final String ABORTED_FIELD = "aborted";
    public static final String SUCCESSFUL_FIELD = "successful";
    public static final String NB_STEPS_FIELD = "nbSteps";

    protected ScribeTestPlanReportScenarioSerializer() {
        this(null);
    }
    protected ScribeTestPlanReportScenarioSerializer(Class<ScribeTestPlanScenarioRunner> t) {
        super(t);
    }

    @Override
    public void serialize(ScribeTestPlanScenarioRunner data, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        gen.writeStringField(SCENARIO_FIELD, data.getScenario());
        gen.writeNumberField(TIME_FIELD, data.getDuration().toMillis());
        gen.writeNumberField(NB_STEPS_FIELD, data.getStepCount());
        gen.writeNumberField(SUCCESSFUL_FIELD, data.getFailureCount());
        gen.writeNumberField(FAILURES_FIELD, data.getFailureCount());
        gen.writeNumberField(ABORTED_FIELD, data.getAbortedCount());

        // write steps
        gen.writeArrayFieldStart(STEPS_FIELD);
        for (ScribeTestPlanStepRunner step : data.getSteps()) {
            gen.writeObject(step);
        }
        gen.writeEndArray();

        gen.writeEndObject();
    }
}
