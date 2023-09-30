package com.miriya.miri.scribe.client.junit;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestFactoryTestDescriptor;
import org.junit.jupiter.engine.execution.DefaultTestInstances;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistrar;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Defined the scribe test descriptor as a {@link ClassTestDescriptor}.
 */
public class ScribeTestDescriptor extends ClassTestDescriptor {
    private final File scribeFile;
    private final File datasetFile;

    /**
     * Creates a new instance.
     * @param uniqueId the descriptor unique id
     * @param configuration the jupiter configuration
     * @param scribeFile the file of the scribe to describe
     * @param datasetFile the scribe dataset file
     */
    public ScribeTestDescriptor(UniqueId uniqueId, JupiterConfiguration configuration,
                                File scribeFile, File datasetFile) {
        super(uniqueId, ScribeDynamicTest.class, configuration);
        this.scribeFile = scribeFile;
        this.datasetFile = datasetFile;

        Method method = ScribeDynamicTest.getTestMethod();
        UniqueId childUID = uniqueId.append(TestFactoryTestDescriptor.SEGMENT_TYPE, method.getName());
        addChild(new TestFactoryTestDescriptor(childUID, ScribeDynamicTest.class, method, configuration));
    }

    @Override
    public ExecutionMode getExecutionMode() {
        return toExecutionMode(org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT);
    }

    @Override
    protected TestInstances instantiateTestClass(
            JupiterEngineExecutionContext parentExecutionContext, ExtensionRegistry registry,
            ExtensionRegistrar registrar, ExtensionContext extensionContext,
            ThrowableCollector throwableCollector) {
        ScribeDynamicTest instance = new ScribeDynamicTest();
        instance.setScribeFile(scribeFile);
        instance.setDatasetFile(datasetFile);

        return DefaultTestInstances.of(instance);
    }
}
