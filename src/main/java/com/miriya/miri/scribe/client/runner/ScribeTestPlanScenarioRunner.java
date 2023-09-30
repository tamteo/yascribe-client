package com.miriya.miri.scribe.client.runner;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miriya.miri.scribe.grpc.ScribeTestPlanReportScenario;
import com.miriya.miri.scribe.grpc.ScribeTestPlanReportStep;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;

import java.time.Duration;
import java.util.*;

/**
 * Defined a scenario test runner.
 */
@JsonSerialize(using = ScribeTestPlanReportScenarioSerializer.class)
public class ScribeTestPlanScenarioRunner {
    private final UUID uuid;
    private final String scenario;
    private Duration duration = Duration.ZERO;
    private int failureCount;
    private int abortedCount;
    private int successfulCount;

    // use a LinkedHashMap to keep the step order
    private final Map<UUID, ScribeTestPlanStepRunner> stepMap = new LinkedHashMap<>();

    ScribeTestPlanScenarioRunner(ScribeTestPlanReportScenario report) {
        this.uuid = UUID.fromString(report.getUuid());
        this.scenario = report.getScenario();

        for (ScribeTestPlanReportStep step : report.getStepsList()) {
            ScribeTestPlanStepRunner runner = new ScribeTestPlanStepRunner(step);
            stepMap.put(runner.getUuid(), runner);
        }
    }

    /**
     * Gets the uui of this runner.
     * @return the uuid
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Refreshes this runner with the specified report.
     * @param report the scenario report
     */
    void refresh(ScribeTestPlanReportScenario report) {
        if (uuid.equals(UUID.fromString(report.getUuid()))) {
            for (ScribeTestPlanReportStep step : report.getStepsList()) {
                ScribeTestPlanStepRunner runner = stepMap.get(UUID.fromString(step.getUuid()));
                if (runner == null) {
                    runner = new ScribeTestPlanStepRunner(step);
                    stepMap.put(runner.getUuid(), runner);
                } else {
                    runner.refresh(step);
                }
            }
        }
    }

    /**
     * Gets the scenario name.
     * @return the scenario name
     */
    public String getScenario() {
        return scenario;
    }

    /**
     * Gets the list of step runners.
     * @return the list of step runners
     */
    List<ScribeTestPlanStepRunner> getSteps() {
        return new ArrayList<>(stepMap.values());
    }

    /**
     * Computes status and duration.
     */
    void computeStatusAndDuration() {
        duration = Duration.ZERO;
        abortedCount = 0;
        successfulCount = 0;
        failureCount = 0;

        for (ScribeTestPlanStepRunner step : stepMap.values()) {
            duration = duration.plus(step.getDuration());

            switch (step.getStatus()) {
                case FAILED -> failureCount++;
                case ABORTED -> abortedCount++;
                case SUCCESSFUL -> successfulCount++;
            }
        }
    }

    /**
     * Gets this scenario duration.
     * @return the duration
     */
    Duration getDuration() {
        return duration;
    }

    /**
     * Gets this scenario step count.
     * @return step count
     */
    int getStepCount() {
        return stepMap.size();
    }

    /**
     * Gets this scenario successful step count.
     * @return successful step count
     */
    int getSuccessfulCount() {
        return successfulCount;
    }

    /**
     * Gets this scenario failed step count.
     * @return failed step count
     */
    int getFailureCount() {
        return failureCount;
    }

    /**
     * Gets this scenario aborted step count.
     * @return aborted step count
     */
    int getAbortedCount() {
        return abortedCount;
    }

    /**
     * Converts this runner to a dynamic node.
     * @return the dynamic node
     */
    DynamicNode toDynamicNode() {
        return DynamicContainer.dynamicContainer(scenario,
                stepMap.values()
                        .stream()
                        .map(ScribeTestPlanStepRunner::toDynamicTest));
    }

    /**
     * Called when error occurred on report streaming.
     * @param t the error
     */
    void onError(Throwable t) {
        stepMap.values().forEach(stepRunner -> stepRunner.onError(t));
    }

    /**
     * Called when report streaming completed.
     */
    void onCompleted() {
        stepMap.values().forEach(ScribeTestPlanStepRunner::onCompleted);
    }
}
