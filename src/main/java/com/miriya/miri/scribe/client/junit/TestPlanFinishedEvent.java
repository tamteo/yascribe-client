package com.miriya.miri.scribe.client.junit;

import org.junit.platform.launcher.TestPlan;

/**
 * Defined an event to notify that a test plan is finished.
 *
 * @param testPlan the finished test plan
 */
public record TestPlanFinishedEvent(TestPlan testPlan) {
}
