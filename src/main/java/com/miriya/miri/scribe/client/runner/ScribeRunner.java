package com.miriya.miri.scribe.client.runner;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Defined a Scribe test runner.
 */
public class ScribeRunner {
    private final String scribe;

    // use a LinkedHashMap to keep the scenario order
    private final Map<String, ScenarioRunner> scenarioMap = new LinkedHashMap<>();

    /**
     * Creates a new instance.
     * @param scribe the scribe name
     */
    public ScribeRunner(String scribe) {
        this.scribe = scribe;
    }

    /**
     * Adds the specified scenario runner to this scribe runner.
     * @param scenario the scenario runner
     */
    void addScenario(ScenarioRunner scenario) {
        scenarioMap.put(scenario.getScenario(), scenario);
    }

    /**
     * Gets a scenario runner by its scenario name.
     * @param name the scenario name
     * @return the scenario runner or null
     */
    ScenarioRunner getScenario(String name) {
        return scenarioMap.get(name);
    }

    /**
     * Gets the scribe name.
     * @return the scribe name
     */
    public String getScribe() {
        return scribe;
    }

    /**
     * Converts this runner to dynamic node.
     * @return the dynamic node
     */
    public DynamicNode toDynamicNode() {
        return DynamicContainer.dynamicContainer(getScribe(),
                scenarioMap.values()
                        .stream()
                        .map(scenario -> scenario.toDynamicNode()));
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
        scenarioMap.values().forEach(scenarioRunner -> scenarioRunner.onCompleted());
    }
}
