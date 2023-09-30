package com.miriya.miri.scribe.client.junit;

import com.google.common.eventbus.EventBus;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * Defined the test execution listener specific to the Stylus Engine.
 */
public class StylusTestExecutionListener implements TestExecutionListener {
    private static EventBus eventBus = new EventBus();

    /**
     * Registers the specified event listener to the {@link StylusTestExecutionListener} event bus.
     * @param listener the event listener to register
     */
    public static void registerListener(Object listener) {
        eventBus.register(listener);
    }

    /**
     * Unregisters the specified event listener from the {@link StylusTestExecutionListener} event bus.
     * @param listener the event listener
     */
    public static void unregisterListener(Object listener) {
        eventBus.unregister(listener);
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        eventBus.post(new TestPlanStartedEvent(testPlan));
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        eventBus.post(new TestPlanFinishedEvent(testPlan));
    }

    @Override
    public void dynamicTestRegistered(TestIdentifier testIdentifier) {
        eventBus.post(new TestRegisteredEvent(testIdentifier));
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        eventBus.post(new TestStartedEvent(testIdentifier));
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        eventBus.post(new TestSkippedEvent(testIdentifier, reason));
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        eventBus.post(new TestFinishedEvent(testIdentifier, testExecutionResult));
    }
}
