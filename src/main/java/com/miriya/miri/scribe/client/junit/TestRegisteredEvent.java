package com.miriya.miri.scribe.client.junit;

import org.junit.platform.launcher.TestIdentifier;

/**
 * Defined an event to notify that a test is registered.
 *
 * @param testIdentifier the identifier of the registered test
 */
public record TestRegisteredEvent(TestIdentifier testIdentifier) {
}
