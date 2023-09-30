package com.miriya.miri.scribe.client.junit;

import org.junit.platform.launcher.TestPlan;

/**
 * Defined an event to notify that a test plan is started.
 *
 * @param testPlan the started test plan
 */
public record TestPlanStartedEvent(TestPlan testPlan) {
}
