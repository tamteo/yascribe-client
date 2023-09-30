package com.miriya.miri.scribe.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.eventbus.Subscribe;
import com.miriya.miri.scribe.client.junit.StylusTestExecutionListener;
import com.miriya.miri.scribe.client.junit.TestPlanFinishedEvent;
import com.miriya.miri.scribe.client.runner.MarkdownReportWriter;
import com.miriya.miri.scribe.client.runner.ScribeTestPlanRunner;
import com.miriya.miri.scribe.client.util.ScribeProperties;
import com.miriya.miri.scribe.grpc.RemoteScribeTestPlan;
import com.miriya.miri.scribe.grpc.ScribeNodeTestServiceGrpc;
import com.miriya.miri.scribe.grpc.ScribeTestPlanReport;
import com.miriya.miri.scribe.grpc.ScribeTestPlanReportRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DynamicNode;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Defined a streamer of remote test plans.
 */
@Slf4j
class ScribeTestPlanReportStreamer implements StreamObserver<ScribeTestPlanReport> {
    private static final Map<String, ManagedChannel> nodeChannelMap = Collections.synchronizedMap(new HashMap<>());

    private static ManagedChannel getNodeChannel(String host, int port) {
        String key = host + ":" + port;
        ManagedChannel nodeChannel = nodeChannelMap.get(key);
        if (nodeChannel == null) {
            nodeChannel = ManagedChannelBuilder.forAddress(host, port)
                    .usePlaintext()
                    .build();
            nodeChannelMap.put(key, nodeChannel);
        }
        return nodeChannel;
    }

    private final File scribeFile;
    private final File datasetFile;
    private final RemoteScribeTestPlan testPlan;
    private final CountDownLatch firstReportLatch;
    private ScribeTestPlanRunner scribeRunner;
    private final ScribeNodeTestServiceGrpc.ScribeNodeTestServiceStub nodeStub;

    /**
     * Creates a new instance.
     * @param scribeFile the scribe file
     * @param datasetFile the dataset file
     * @param testPlan the remote test plan
     */
    ScribeTestPlanReportStreamer(File scribeFile, File datasetFile, RemoteScribeTestPlan testPlan) {
        this.scribeFile = scribeFile;
        this.datasetFile = datasetFile;
        this.testPlan = testPlan;
        this.firstReportLatch = new CountDownLatch(1);
        StylusTestExecutionListener.registerListener(this);

        nodeStub = ScribeNodeTestServiceGrpc.newStub(getNodeChannel(testPlan.getNodeHost(), testPlan.getNodePort()));
    }

    @Override
    public void onNext(ScribeTestPlanReport value) {
        if (scribeRunner == null) {
            // create runner from test report
            scribeRunner = new ScribeTestPlanRunner(value);
        } else {
            // refresh tests from report
            scribeRunner.refresh(value);
        }
        firstReportLatch.countDown();
    }

    @Override
    public void onError(Throwable t) {
        log.error("Error occurred on streaming report!", t);
        if (scribeRunner != null) {
            scribeRunner.onError(t);
            firstReportLatch.countDown();
        }
    }

    @Override
    public void onCompleted() {
        if (scribeRunner != null) {
            scribeRunner.onCompleted();
            firstReportLatch.countDown();
        }
    }

    DynamicNode getTestPlanDynamicNode() {
        try {
            if (firstReportLatch.await(1, TimeUnit.MINUTES)) {
                return scribeRunner.toDynamicNode();
            } else {
                throw new RuntimeException("Waiting for test reports cannot finish within 1 minute.");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Waiting for test reports interrupted.");
        }
    }

    @Subscribe
    public void onTestPlanFinished(TestPlanFinishedEvent event) {
        StylusTestExecutionListener.unregisterListener(this);
        saveReports();
    }

    private String makeReportFileName() {
        String fileName = "TEST-" + scribeFile.getName();

        int index = fileName.lastIndexOf(".feature");
        if (index != -1) {
            fileName = fileName.substring(0, index);
        } else {
            index = fileName.lastIndexOf(".scribe");
            if (index != -1) {
                fileName = fileName.substring(0, index);
            }
        }

        String datasetIndex = "";
        if (datasetFile != null) {
            datasetIndex = datasetFile.getName();
            index = datasetIndex.lastIndexOf(".yaml");
            if (index != -1) {
                datasetIndex = datasetIndex.substring(0, index);
            } else {
                index = datasetIndex.lastIndexOf(".yml");
                if (index != -1) {
                    datasetIndex = datasetIndex.substring(0, index);
                }
            }

            index = datasetIndex.lastIndexOf(".");
            if (index != 1) {
                datasetIndex = datasetIndex.substring(index);
            }
        }

        return String.format("%s%s-%s", fileName, datasetIndex, scribeRunner.getBrowser());
    }

    /**
     * Saves reports into files.
     */
    private void saveReports() {
        if (scribeRunner == null) return;

        // compute status and durations
        scribeRunner.computeStatusAndDuration();

        File reportDir = new File(ScribeProperties.getReportDir());
        if (!reportDir.exists()) reportDir.mkdirs();

        String fileName = makeReportFileName();

        try {
            ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            // StdDateFormat is ISO8601 since jackson 2.9
            mapper.setDateFormat(ScribeTestPlanRunner.getDateFormat());
            mapper.registerModule(new Jdk8Module());
            mapper.writeValue(new File(reportDir, fileName+ ".json"), scribeRunner);
        } catch (Exception e) {
            log.error("Failed to save scribe json report: " + fileName, e);
        }

        try {
            new MarkdownReportWriter().writeScribeReportData(new File(reportDir, fileName+ ".md"), scribeRunner);
        } catch (Exception e) {
            log.error("Failed to save scribe markdown report: " + fileName, e);
        }
    }

    /**
     * Streams the test plan and does not wait for test node creation.
     */
    void stream() {
        nodeStub.streamTestPlanReport(
                ScribeTestPlanReportRequest.newBuilder()
                        .setTestPlanUuid(testPlan.getTestPlanUuid())
                        .build(),
                this);
    }
 }
