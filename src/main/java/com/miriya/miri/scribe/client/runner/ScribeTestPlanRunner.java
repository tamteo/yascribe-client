package com.miriya.miri.scribe.client.runner;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.miriya.miri.scribe.grpc.ScribeTestPlanReport;
import com.miriya.miri.scribe.grpc.ScribeTestPlanReportScenario;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;

import java.time.Duration;
import java.util.*;

/**
 * Defined a test plan runner.
 */
@JsonSerialize(using = ScribeTestPlanReportSerializer.class)
public class ScribeTestPlanRunner {
    public static StdDateFormat getDateFormat() {
        return new StdDateFormat().withColonInTimeZone(true);
    }

    private final UUID uui;
    private final String scribe;
    private final Date date;
    private final String browser;
    private Duration duration = Duration.ZERO;
    private int failureCount;
    private int abortedCount;
    private int successfulCount;
    private int stepCount;

    // use a LinkedHashMap to keep the scenario order
    private final Map<UUID, ScribeTestPlanScenarioRunner> scenarioMap = new LinkedHashMap<>();

    public ScribeTestPlanRunner(ScribeTestPlanReport testPlanReport) {
        this.uui = UUID.fromString(testPlanReport.getTestPlanUuid());
        this.scribe = testPlanReport.getScribe();
        this.date = new Date(testPlanReport.getDate().getSeconds() * 1000);
        this.browser = testPlanReport.getBrowser();

        for (ScribeTestPlanReportScenario scenario : testPlanReport.getScenariosList()) {
            ScribeTestPlanScenarioRunner scenarioRunner = new ScribeTestPlanScenarioRunner(scenario);
            scenarioMap.put(scenarioRunner.getUuid(), scenarioRunner);
        }
    }

    /**
     * Converts this runner to dynamic node.
     * @return the dynamic node
     */
    public DynamicNode toDynamicNode() {
        return DynamicContainer.dynamicContainer(scribe,
                scenarioMap.values()
                        .stream()
                        .map(ScribeTestPlanScenarioRunner::toDynamicNode));
    }

    /**
     * Refreshes this runner with the specified report.
     * @param testPlanReport the test plan report
     */
    public void refresh(ScribeTestPlanReport testPlanReport) {
        if (uui.equals(UUID.fromString(testPlanReport.getTestPlanUuid()))) {
            for (ScribeTestPlanReportScenario scenario : testPlanReport.getScenariosList()) {
                ScribeTestPlanScenarioRunner scenarioRunner = scenarioMap.get(UUID.fromString(scenario.getUuid()));
                if (scenarioRunner == null) {
                    scenarioRunner = new ScribeTestPlanScenarioRunner(scenario);
                    scenarioMap.put(scenarioRunner.getUuid(), scenarioRunner);
                } else {
                    scenarioRunner.refresh(scenario);
                }
            }
        }
    }

    /**
     * Called when error occurred on report streaming.
     * @param t the error
     */
    public void onError(Throwable t) {
        scenarioMap.values().forEach(scenarioRunner -> scenarioRunner.onError(t));
    }

    /**
     * Called when report streaming completed.
     */
    public void onCompleted() {
        scenarioMap.values().forEach(ScribeTestPlanScenarioRunner::onCompleted);
    }

    /**
     * Computes status and duration.
     */
    public void computeStatusAndDuration() {
        duration = Duration.ZERO;
        abortedCount = 0;
        successfulCount = 0;
        failureCount = 0;
        stepCount = 0;

        for (ScribeTestPlanScenarioRunner scenario : scenarioMap.values()) {
            scenario.computeStatusAndDuration();
            duration = duration.plus(scenario.getDuration());
            abortedCount += scenario.getAbortedCount();
            successfulCount += scenario.getSuccessfulCount();
            failureCount += scenario.getFailureCount();
            stepCount += scenario.getStepCount();
        }
    }

    /**
     * Gets the step count.
     * @return step count
     */
    int getStepCount() {
        return stepCount;
    }

    /**
     * Gets the successful step count.
     * @return successful step count
     */
    int getSuccessfulCount() {
        return successfulCount;
    }

    /**
     * Gets the failed step count.
     * @return failed step count
     */
    int getFailureCount() {
        return failureCount;
    }

    /**
     * Gets the aborted step count.
     * @return aborted step count
     */
    int getAbortedCount() {
        return abortedCount;
    }

    /**
     * Gets the test duration.
     * @return the duration
     */
    Duration getDuration() {
        return duration;
    }

    /**
     * Gets the scribe title.
     * @return the scribe title
     */
    public String getScribe() {
        return scribe;
    }

    /**
     * Gets this runner uuid.
     * @return the uuid
     */
    public UUID getUui() {
        return uui;
    }

    /**
     * Gets the test date.
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Gets the scenario count.
     * @return the scenario count
     */
    int getScenarioCount() {
        return scenarioMap.size();
    }

    /**
     * Gets the list of scenarios.
     * @return the list of scenarios
     */
    List<ScribeTestPlanScenarioRunner> getScenarios() {
        return new ArrayList<>(scenarioMap.values());
    }

    /**
     * Gets the browser name.
     * @return browser name
     */
    public String getBrowser() {
        return browser;
    }
}
