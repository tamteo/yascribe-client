package com.miriya.miri.scribe.client.junit;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestIdentifier;

/**
 * Defined an event to notify that a test execution is finished.
 *
 * @param testIdentifier      the identifier of the finished test
 * @param testExecutionResult the test result
 */
public record TestFinishedEvent(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
}
