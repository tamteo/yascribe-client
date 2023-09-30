package com.miriya.miri.scribe.client.runner;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Defined a json serializer of test step reports.
 */
public class ScribeTestPlanReportStepSerializer extends StdSerializer<ScribeTestPlanStepRunner> {
    public static final String STEP_FIELD = "step";
    public static final String TIME_FIELD = "timeInMs";
    public static final String STATUS_FIELD = "status";
    public static final String REASON_FIELD = "reason";

    protected ScribeTestPlanReportStepSerializer() {
        this(null);
    }
    protected ScribeTestPlanReportStepSerializer(Class<ScribeTestPlanStepRunner> t) {
        super(t);
    }

    @Override
    public void serialize(ScribeTestPlanStepRunner data, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        gen.writeStringField(STEP_FIELD, data.getStep());
        gen.writeNumberField(TIME_FIELD, data.getDuration().toMillis());
        gen.writeStringField(STATUS_FIELD, data.getStatus().toString());

        String reason = data.getReason();
        if (reason != null && !reason.equals("")) gen.writeStringField(REASON_FIELD, reason);

        gen.writeEndObject();
    }
}
