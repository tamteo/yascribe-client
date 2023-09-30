package com.miriya.miri.scribe.client.junit;

import com.miriya.miri.scribe.client.util.DatasetUtils;
import com.miriya.miri.scribe.client.util.ScribeProperties;
import com.miriya.miri.scribe.client.util.ScribeUtils;
import org.junit.jupiter.engine.config.DefaultJupiterConfiguration;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.discovery.DiscoverySelectorResolver;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.support.config.PrefixedConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

/**
 * Defined a JUnit test engine to discover and execute scribes.
 */
public class StylusTestEngine extends HierarchicalTestEngine<JupiterEngineExecutionContext> {
    private static final String ENGINE_ID = "pub-stylus-engine";

    @Override
    public String getId() {
        return ENGINE_ID;
    }

    @Override
    public Optional<String> getGroupId() {
        return Optional.of("com.miriya.miri.scribe");
    }

    @Override
    public Optional<String> getArtifactId() {
        return Optional.of("pub-stylus");
    }

    @Override
    protected HierarchicalTestExecutorService createExecutorService(ExecutionRequest request) {
        return new ForkJoinPoolHierarchicalTestExecutorService(new PrefixedConfigurationParameters(
                request.getConfigurationParameters(), "junit.jupiter.execution.parallel.config."));
    }

    @Override
    protected JupiterEngineExecutionContext createExecutionContext(ExecutionRequest request) {
        return new JupiterEngineExecutionContext(request.getEngineExecutionListener(),
                getJupiterConfiguration(request));
    }

    private JupiterConfiguration getJupiterConfiguration(ExecutionRequest request) {
        JupiterEngineDescriptor engineDescriptor = (JupiterEngineDescriptor) request.getRootTestDescriptor();
        return engineDescriptor.getConfiguration();
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        // first, load properties required to generate scribe tests
        ScribeProperties.load();

        StylusConfiguration configuration = createConfiguration(discoveryRequest);
        JupiterEngineDescriptor engineDescriptor = new JupiterEngineDescriptor(uniqueId, configuration);
        new DiscoverySelectorResolver().resolveSelectors(discoveryRequest, engineDescriptor);

        discoveryRequest.getSelectorsByType(ClassSelector.class).forEach(selector ->
                appendScribeAsDynamicTests(selector.getJavaClass(), engineDescriptor, configuration));

        return engineDescriptor;
    }

    private StylusConfiguration createConfiguration(EngineDiscoveryRequest discoveryRequest) {
        return new StylusConfiguration(
                new DefaultJupiterConfiguration(discoveryRequest.getConfigurationParameters()));
    }

    private void appendScribeAsDynamicTests(
            Class<?> javaClass, TestDescriptor engineDescriptor, StylusConfiguration configuration) {
        if (AnnotationSupport.isAnnotated(javaClass, ScribeTest.class)) {
            // get scribe files specified by the annotated test class
            // if no file is specified then get all scribes defined in the scribe folder
            Collection<File> files = ScribeUtils.getScribeFiles(javaClass.getAnnotation(ScribeTest.class).files());
            if (files.isEmpty()) {
                files = ScribeUtils.getAllScribeFiles();
            }

            files.forEach(file -> {
                configuration.setScribeFile(file);
                Collection<File> datasets = DatasetUtils.getDatasetFiles(file);
                if (datasets.isEmpty()) {
                    configuration.setDatasetFile(null);
                    UniqueId uid = engineDescriptor.getUniqueId().append("class", file.getName());
                    engineDescriptor.addChild(
                            new ScribeTestDescriptor(uid, configuration, file, null)
                    );
                } else {
                    datasets.forEach(dataset -> {
                        configuration.setDatasetFile(dataset);
                        UniqueId uid = engineDescriptor.getUniqueId().append("class",
                                file.getName() + ":" + dataset.getName());
                        engineDescriptor.addChild(
                                new ScribeTestDescriptor(uid, configuration, file, dataset)
                        );
                    });
                }
            });
        }
    }
}
