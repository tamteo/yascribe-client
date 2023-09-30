package com.miriya.miri.scribe.client.runner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicTest;

/**
 * Defined a step runner.
 */
public class StepRunner {
    private static final String STARTED = "STARTED";
    private static final String ABORTED = "ABORTED";
    private static final String SUCCESSFUL = "SUCCESSFUL";
    private static final String FAILED = "FAILED";
    private static final String IDLE = "IDLE";

    private final ScenarioRunner scenario;
    private final String step;
    private long timeInMs = 0;
    private String status = IDLE;
    private String reason;

    private boolean isTestStarted;
    private boolean isTestDone;

    /**
     * Creates a new instance.
     * @param scenario the parent scenario runner
     * @param step the step name
     */
    public StepRunner(ScenarioRunner scenario, String step) {
        this.step = step;
        this.scenario = scenario;
    }

    private void doNotifyAll() {
        if (!isTestStarted) return;
        synchronized (this) {
            notifyAll();
        }
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

            switch (status.toUpperCase()) {
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

    /**
     * Refreshes this step test execution result.
     * @param timeInMs test time in ms
     * @param status the test status
     * @param reason the failure/abort reason if any
     */
    public void refresh(long timeInMs, String status, String reason) {
        if (isTestDone) return;

        this.timeInMs = timeInMs;
        this.status = status != null ? status : IDLE;
        this.reason = reason;

        if (this.status.equalsIgnoreCase(SUCCESSFUL)
                || this.status.equalsIgnoreCase(ABORTED)
                || this.status.equalsIgnoreCase(FAILED)) {
            isTestDone = true;
            doNotifyAll();
        }
    }

    /**
     * Called when error occurred on report streaming.
     * @param t the error
     */
    public void onError(Throwable t) {
        isTestDone = true;
        if (STARTED.equalsIgnoreCase(status) || IDLE.equalsIgnoreCase(status)) {
            status = ABORTED;
            reason = t.getMessage();
        }
        doNotifyAll();
    }

    /**
     * Called when report streaming completed.
     */
    public void onCompleted() {
        isTestDone = true;
        if (STARTED.equalsIgnoreCase(status) || IDLE.equalsIgnoreCase(status)) {
            status = ABORTED;
            reason = "Scribe execution completed. Should not occurred.";
        }
        doNotifyAll();
    }

    /**
     * Gets the parent scenario runner.
     * @return the scenario runner
     */
    public ScenarioRunner getScenario() {
        return scenario;
    }

    /**
     * Gets the step name.
     * @return the step name
     */
    public String getStep() {
        return step;
    }

    /**
     * Gets the test time in ms.
     * @return the test time
     */
    public long getTimeInMs() {
        return timeInMs;
    }

    /**
     * Gets the test status.
     * @return the test status
     */
    public String getStatus() {
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
