package com.miriya.miri.scribe.client.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.File;
import java.util.Collection;

/**
 * Defined utilities to manage scribe datasets.
 */
@Slf4j
public class DatasetUtils {
    /**
     * Gets the dataset file of the specified scribe.
     * @param scribeFile the scribe file
     * @return the collection of dataset files or an empty collection
     */
    public static Collection<File> getDatasetFiles(File scribeFile) {
        String regex = FilenameUtils.removeExtension(scribeFile.getName()) + ".dataset.[0-9]+.yaml";
        return FileUtils.listFiles(scribeFile.getParentFile(), new RegexFileFilter(regex), null);
    }
}
