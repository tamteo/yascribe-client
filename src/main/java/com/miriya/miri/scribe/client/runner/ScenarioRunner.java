package com.miriya.miri.scribe.client.runner;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Defined a scenario test runner.
 */
public class ScenarioRunner {
    private final String scenario;

    // use a LinkedHashMap to keep the step order
    private final Map<String, StepRunner> stepMap = new LinkedHashMap<>();

    /**
     * Creates a new instance.
     * @param scenario the scenario name
     */
    public ScenarioRunner(String scenario) {
        this.scenario = scenario;
    }

    /**
     * Gets the scenario name.
     * @return the scenario name
     */
    public String getScenario() {
        return scenario;
    }

    /**
     * Adds the specified step runner to this scenario runner.
     * @param step the step runner
     */
    void addStep(StepRunner step) {
        stepMap.put(step.getStep(), step);
    }

    /**
     * Gets the step runner by its step name.
     * @param name the step name
     * @return the step runner or null
     */
    StepRunner getStep(String name) {
        return stepMap.get(name);
    }

    /**
     * Converts this runner to a dynamic node.
     * @return the dynamic node
     */
    DynamicNode toDynamicNode() {
        return DynamicContainer.dynamicContainer(getScenario(),
                stepMap.values()
                        .stream()
                        .map(step -> step.toDynamicTest()));
    }

    /**
     * Called when error occurred on report streaming.
     * @param t the error
     */
    public void onError(Throwable t) {
        stepMap.values().forEach(stepRunner -> stepRunner.onError(t));
    }

    /**
     * Called when report streaming completed.
     */
    public void onCompleted() {
        stepMap.values().forEach(stepRunner -> stepRunner.onCompleted());
    }
}
