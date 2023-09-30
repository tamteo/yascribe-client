package com.miriya.miri.scribe.client.junit;

import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.engine.config.CachingJupiterConfiguration;
import org.junit.jupiter.engine.config.JupiterConfiguration;

import java.io.File;

/**
 * Defined a {@link JupiterConfiguration} for the Stylus engine.
 */
public class StylusConfiguration extends CachingJupiterConfiguration {
    private File scribeFile;
    private File datasetFile;

    public StylusConfiguration(JupiterConfiguration delegate) {
        super(delegate);
    }

    /**
     * Gets the current scribe file.
     * @return the scribe file or null
     */
    public File getScribeFile() {
        return scribeFile;
    }

    /**
     * Gets the current scribe dataset file.
     * @return the dataset fiel or null
     */
    public File getDatasetFile() {
        return datasetFile;
    }

    /**
     * Sets the current scribe file.
     * @param scribeFile the scribe file
     */
    void setScribeFile(File scribeFile) {
        this.scribeFile = scribeFile;
    }

    /**
     * Sets the current scribe dataset file.
     * @param datasetFile the dataset file
     */
    void setDatasetFile(File datasetFile) {
        this.datasetFile = datasetFile;
    }

    @Override
    public DisplayNameGenerator getDefaultDisplayNameGenerator() {
        return new ScribeDisplayNameGenerator(scribeFile, datasetFile);
    }
}
