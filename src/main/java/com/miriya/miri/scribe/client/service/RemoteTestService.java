package com.miriya.miri.scribe.client.service;

import com.miriya.miri.scribe.grpc.*;
import com.miriya.miri.scribe.client.util.ScribeProperties;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.DynamicNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

/**
 * Defined a test service to request remote test executions.
 */
public class RemoteTestService {
    private static RemoteTestService instance;

    /**
     * Gets the {@link RemoteTestService} instance.
     * @return an instance
     */
    public static RemoteTestService getInstance() {
        if (instance == null) {
            instance = new RemoteTestService();
        }
        return instance;
    }

    private static final String LANGUAGE = "english";
    private static final String CONTEXT = "selenium";
    private static final String LIBRARY = "";
    private final ManagedChannel channel;
    private final ScribeRemoteTestServiceGrpc.ScribeRemoteTestServiceStub remoteTestStub;

    private RemoteTestService() {
        channel = ManagedChannelBuilder.forAddress(
                        ScribeProperties.getServerName(),
                        ScribeProperties.getServerPort())
                .usePlaintext()
                .build();
        remoteTestStub = ScribeRemoteTestServiceGrpc.newStub(channel);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> channel.shutdown()));
    }

    /**
     * Remote executes the Scribe test.
     * @param scribeFile the Scribe file
     * @param datasetFile the Scribe dataset file
     * @return a stream of tests as {@link DynamicNode}
     * @throws IOException if reading files failed
     */
    public Stream<DynamicNode> remoteExecuteTestPlan(File scribeFile, File datasetFile) throws IOException {
        ScribeTestPlanRequest.Builder builder = ScribeTestPlanRequest.newBuilder()
                .setScribeEnv(ScribeEnv.newBuilder()
                        .setLanguage(LANGUAGE)
                        .setContext(CONTEXT)
                        .setLibrary(LIBRARY)
                        .build())
                .setTestConfig(ScribeTestConfig.newBuilder()
                        .setReportType(2)
                        .setBrowser(ScribeProperties.getBrowserName())
                        .build())
                .setScribe(Files.readString(scribeFile.toPath()));
        if (datasetFile != null) builder.setDataset(Files.readString(datasetFile.toPath()));

        ScribeTestPlanSetStreamer streamer = new ScribeTestPlanSetStreamer(scribeFile, datasetFile);
        remoteTestStub.executeTestPlan(builder.build(), streamer);

        return streamer.getNodes();
    }
}
