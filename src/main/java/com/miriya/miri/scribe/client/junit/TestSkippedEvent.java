package com.miriya.miri.scribe.client.junit;

import org.junit.platform.launcher.TestIdentifier;

/**
 * Defined a test skipped event.
 * @param testIdentifier the identifier of the skipped test
 * @param reason the skip reason
 */
public record TestSkippedEvent(TestIdentifier testIdentifier, String reason) {
}
