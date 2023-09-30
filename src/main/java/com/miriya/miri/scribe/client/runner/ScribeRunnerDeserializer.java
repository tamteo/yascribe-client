package com.miriya.miri.scribe.client.runner;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

/**
 * Defined a deserializer from scribe report to {@link ScribeRunner}.
 */
public class ScribeRunnerDeserializer extends StdDeserializer<ScribeRunner> {
    private static final String SCRIBE = "scribe";
    private static final String SCENARIOS = "scenarios";
    private static final String SCENARIO = "scenario";
    private static final String STEPS = "steps";
    private static final String STEP = "step";
    private static final String STATUS = "status";
    private static final String REASON = "reason";
    private static final String TIME_IN_MS = "timeInMs";

    private final ThreadLocal<ScribeRunner> threadLocalScribeRunner = new InheritableThreadLocal<>();

    public ScribeRunnerDeserializer() {
        this(null);
    }

    public ScribeRunnerDeserializer(Class<?> vc) {
        super(vc);
    }

    /**
     * Sets the specified scribe runner to the current thread.
     * @param scribeRunner the scribe runner
     */
    public void setScribeRunner(ScribeRunner scribeRunner) {
        threadLocalScribeRunner.set(scribeRunner);
    }

    /**
     * Clears the scribe runner store for the current thread.
     */
    public void clearScribeRunner() {
        threadLocalScribeRunner.set(null);
    }

    @Override
    public ScribeRunner deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        ObjectCodec codec = parser.getCodec();
        JsonNode root = codec.readTree(parser);

        ScribeRunner scribeRunner = threadLocalScribeRunner.get();
        return scribeRunner == null ? readScribe(root) : mergeScribe(scribeRunner, root);
    }

    /**
     * Reads the scenario from the specified json node.
     * @param node the json node
     * @return an instance of the scenario runner
     */
    private ScenarioRunner readScenario(JsonNode node) {
        ScenarioRunner scenario = new ScenarioRunner(node.get(SCENARIO).asText());

        JsonNode stepsNode = node.get(STEPS);
        if (stepsNode != null && stepsNode.isArray()) {
            for (JsonNode stepNode: stepsNode) {
                scenario.addStep(readStep(stepNode, scenario));
            }
        }

        return scenario;
    }

    /**
     * Refreshes the specified step with the data contained in the specified json node.
     * @param step the step runner to refresh
     * @param node the json node
     */
    private void refreshStep(StepRunner step, JsonNode node) {
        JsonNode reason = node.get(REASON);
        step.refresh(
                node.get(TIME_IN_MS).asLong(),
                node.get(STATUS).asText(),
                reason != null ? reason.asText() : null);
    }

    /**
     * Reads the step from the specified json node.
     * @param node the json node
     * @param scenario the parent scenario of the step
     * @return an instance of the step runner
     */
    private StepRunner readStep(JsonNode node, ScenarioRunner scenario) {
        StepRunner step = new StepRunner(scenario, node.get(STEP).asText());
        scenario.addStep(step);
        refreshStep(step, node);

        return step;
    }

    /**
     * Reads the scribe from the specified json node.
     * @param root the json node
     * @return an instance of the scribe runner
     */
    private ScribeRunner readScribe(JsonNode root) {
        ScribeRunner scribe;

        scribe = new ScribeRunner(root.get(SCRIBE).asText());

        JsonNode scenariosNode = root.get(SCENARIOS);
        if (scenariosNode != null && scenariosNode.isArray()) {
            for (JsonNode scenarioNode: scenariosNode) {
                scribe.addScenario(readScenario(scenarioNode));
            }
        }

        threadLocalScribeRunner.set(scribe);
        return scribe;
    }

    /**
     * Merges the specified scribe runner with the data contained in the specified json node.
     * @param scribe the scribe runner
     * @param root the json node
     * @return the merged scribe runner
     */
    private ScribeRunner mergeScribe(ScribeRunner scribe, JsonNode root) {
        JsonNode scenariosNode = root.get(SCENARIOS);
        if (scenariosNode != null && scenariosNode.isArray()) {
            for (JsonNode scenarioNode: scenariosNode) {
                ScenarioRunner scenario = scribe.getScenario(scenarioNode.get(SCENARIO).asText());
                if (scenario == null) {
                    scribe.addScenario(readScenario(scenarioNode));
                } else {
                    JsonNode stepsNode = scenarioNode.get(STEPS);
                    if (stepsNode != null && stepsNode.isArray()) {
                        for (JsonNode stepNode: stepsNode) {
                            StepRunner step = scenario.getStep(stepNode.get(STEP).asText());
                            if (step == null) {
                                scenario.addStep(readStep(stepNode, scenario));
                            } else {
                                refreshStep(step, stepNode);
                            }
                        }
                    }
                }
            }
        }

        return scribe;
    }
}
