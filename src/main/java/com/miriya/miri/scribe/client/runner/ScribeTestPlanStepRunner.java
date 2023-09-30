package com.miriya.miri.scribe.client.runner;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miriya.miri.scribe.grpc.ScribeTestPlanReportStep;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicTest;

import java.time.Duration;
import java.util.UUID;

/**
 * Defined a step runner.
 */
@JsonSerialize(using = ScribeTestPlanReportStepSerializer.class)
public class ScribeTestPlanStepRunner {
    public enum Status {
        IDLE, STARTED, ABORTED, SUCCESSFUL, FAILED
    }

    private final UUID uuid;
    private final String step;
    private Duration duration;
    private Status status;
    private String reason;

    private boolean isTestStarted;
    private boolean isTestDone;

    /**
     * Creates a new instance.
     * @param report the step report
     */
    ScribeTestPlanStepRunner(ScribeTestPlanReportStep report) {
        this.uuid = UUID.fromString(report.getUuid());
        this.step = report.getStep();
        this.duration = toDuration(report.getDuration());

        setStatus(report.getStatus());
        this.reason = report.getReason();
    }

    private void setStatus(String status) {
        if (status == null || status.equals("")) {
            this.status = Status.IDLE;
        } else {
            try {
                this.status = Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                this.status = Status.IDLE;
            }
        }

        switch (this.status) {
            case ABORTED, SUCCESSFUL, FAILED -> isTestDone = true;
        }
    }

    private void doNotifyAll() {
        if (!isTestStarted) return;
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * Gets the uui of this step runner.
     * @return the uuid
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Converts this runner to a dynamic test.
     * @return the dynamic test
     */
    DynamicTest toDynamicTest() {
        return DynamicTest.dynamicTest(step, () -> {
            isTestStarted = true;
            while(!isTestDone) {
                synchronized (this) {
                    wait();
                }
            }

            switch (status) {
                case SUCCESSFUL -> {}
                case ABORTED -> {
                    if (reason != null) Assumptions.abort(reason); else Assumptions.abort();
                }
                default -> {
                    if (reason != null) Assertions.fail(reason); else Assertions.fail();
                }
            }
        });
    }

    private Duration toDuration(com.google.protobuf.Duration duration) {
        return Duration.ofSeconds(duration.getSeconds())
                .plus(Duration.ofNanos(duration.getNanos()));
    }

    /**
     * Refreshes this runner with the specified report.
     * @param report the step report
     */
    void refresh(ScribeTestPlanReportStep report) {
        if (uuid.equals(UUID.fromString(report.getUuid()))) {
            duration = toDuration(report.getDuration());
            setStatus(report.getStatus());
            reason = report.getReason();

            doNotifyAll();
        }
    }

    /**
     * Gets the test duration.
     * @return the duration
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * Called when error occurred on report streaming.
     * @param t the error
     */
    public void onError(Throwable t) {
        isTestDone = true;
        switch (status) {
            case STARTED, IDLE -> {
                status = Status.ABORTED;
                reason = t.getMessage();
            }
        }
        doNotifyAll();
    }

    /**
     * Called when report streaming completed.
     */
    public void onCompleted() {
        isTestDone = true;
        switch (status) {
            case STARTED, IDLE -> {
                status = Status.ABORTED;
                reason = "Scribe execution completed. Should not occurred.";
            }
        }
        doNotifyAll();
    }

    /**
     * Gets the step name.
     * @return the step name
     */
    public String getStep() {
        return step;
    }

    /**
     * Gets the test status.
     * @return the test status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets the failure/abort reason if any.
     * @return the reason or null
     */
    public String getReason() {
        return reason;
    }

}
