package com.miriya.miri.scribe.client.junit;

import org.junit.platform.launcher.TestIdentifier;

/**
 * Defined an event to notify that a test execution is started.
 *
 * @param testIdentifier the identifier of the started test
 */
public record TestStartedEvent(TestIdentifier testIdentifier) {
}
