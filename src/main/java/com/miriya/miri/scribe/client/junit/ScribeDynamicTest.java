package com.miriya.miri.scribe.client.junit;

import com.miriya.miri.scribe.client.service.RemoteTestService;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * Defined a test class to generate scribe tests as dynamic tests ({@link TestFactory}.
 */
class ScribeDynamicTest {

    /**
     * Gets the test method that generates dynamic tests.
     * @return the test method
     */
    static Method getTestMethod() {
        try {
            return ScribeDynamicTest.class.getMethod("run");
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private File scribeFile;
    private File datasetFile;

    /**
     * Gets the scribe file to generate tests.
     * @return the scribe file or null if not set
     */
    File getScribeFile() {
        return scribeFile;
    }

    /**
     * Sets the scribe file to generate tests.
     * @param scribeFile the scribe file
     */
    void setScribeFile(File scribeFile) {
        this.scribeFile = scribeFile;
    }

    /**
     * Gets the scribe dataset.
     * @return the dataset file or null if not set
     */
    File getDatasetFile() {
        return datasetFile;
    }

    /**
     * Sets the scribe dataset.
     * @param datasetFile the data set file
     */
    void setDatasetFile(File datasetFile) {
        this.datasetFile = datasetFile;
    }

    @Execution(ExecutionMode.SAME_THREAD)
    @TestFactory
    public Stream<DynamicNode> run() throws IOException {
        return scribeFile != null ? RemoteTestService.getInstance().remoteExecuteTestPlan(scribeFile, datasetFile) :
                new ArrayList<DynamicNode>().stream();
    }
}
