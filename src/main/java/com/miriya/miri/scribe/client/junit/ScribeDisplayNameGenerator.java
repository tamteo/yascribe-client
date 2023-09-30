package com.miriya.miri.scribe.client.junit;

import org.junit.jupiter.api.DisplayNameGenerator;

import java.io.File;
import java.lang.reflect.Method;

/**
 * <pre>
 * Defined a generator of scribe displayed names:
 *  - parent scribe node : "Scribe: scribe_file_name"
 *  - child node : "Dataset: dataset_file_name"
 * </pre>
 */
public class ScribeDisplayNameGenerator implements DisplayNameGenerator {
    private final File scribeFile;
    private final File datasetFile;

    /**
     * Creates a new instance.
     * @param scribeFile the scribe file
     * @param datasetFile the dataset file of the scribe
     */
    public ScribeDisplayNameGenerator(File scribeFile, File datasetFile) {
        this.scribeFile = scribeFile;
        this.datasetFile = datasetFile;
    }

    @Override
    public String generateDisplayNameForClass(Class<?> testClass) {
        String name = "Scribe: ";
        return scribeFile != null ? name + scribeFile.getName() : name;
    }

    @Override
    public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
        return nestedClass.getSimpleName();
    }

    @Override
    public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
        String name = "Dataset: ";
        return datasetFile != null ? name + datasetFile.getName() : name + "none";
    }
}
