package com.miriya.miri.scribe.client.service;

import com.miriya.miri.scribe.grpc.RemoteScribeTestPlan;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.DynamicNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Defined a streamer of remote test plan sets.
 */
public class ScribeTestPlanSetStreamer implements StreamObserver<RemoteScribeTestPlan> {
    private final List<RemoteScribeTestPlan> testPlans = new ArrayList<>();
    private final List<ScribeTestPlanReportStreamer> streamers = new ArrayList<>();
    private final List<DynamicNode> nodes = new ArrayList<>();

    private final File scribeFile;
    private final File datasetFile;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    public ScribeTestPlanSetStreamer(File scribeFile, File datasetFile) {
        this.scribeFile = scribeFile;
        this.datasetFile = datasetFile;
    }

    @Override
    public void onNext(RemoteScribeTestPlan testPlan) {
        testPlans.add(testPlan);
        ScribeTestPlanReportStreamer streamer = new ScribeTestPlanReportStreamer(scribeFile, datasetFile, testPlan);
        streamers.add(streamer);
        streamer.stream();
    }

    @Override
    public void onError(Throwable t) {
        countDownLatch.countDown();
    }

    @Override
    public void onCompleted() {
        countDownLatch.countDown();
    }

    /**
     * Gets the test nodes of the being streamed test plan set.
     * @return the stream of test nodes
     */
    Stream<DynamicNode> getNodes() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {}

        return streamers.stream()
                .map(ScribeTestPlanReportStreamer::getTestPlanDynamicNode)
                .collect(Collectors.toList())
                .stream();
    }
}
