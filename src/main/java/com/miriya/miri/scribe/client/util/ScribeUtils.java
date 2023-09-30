package com.miriya.miri.scribe.client.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Defined a utility to load scribe files.
 */
public class ScribeUtils {
    /**
     * Gets scribe files from the specified folder.
     * @param folder the folder
     * @return a collection of files
     */
    public static Collection<File> getScribeFiles(String folder) {
        File scribeFolder = new File(folder);

        String[] extension = new String[] {"mscribe", "feature"};
        return scribeFolder.isDirectory() ?
                FileUtils.listFiles(scribeFolder, extension, true) :
                new ArrayList<>();
    }

    /**
     * Gets all scribe files from the scribe folder defined by {@link ScribeProperties#getScribeFolder()}.
     * @return a collection of files
     */
    public static Collection<File> getAllScribeFiles() {
        return getScribeFiles(ScribeProperties.getScribeFolder());
    }

    /**
     * Gets the scribe files defined by the specified names.
     * @param scribes the scribe names
     * @return a collection of files
     */
    public static Collection<File> getScribeFiles(String[] scribes) {
        if (scribes == null || scribes.length == 0) {
            return new ArrayList<>();
        }

        File scribeFolder = new File(ScribeProperties.getScribeFolder());
        List<File> files = new ArrayList<>();
        for (String scribe : scribes) {
            files.add(new File(scribeFolder, scribe));
        }

        return files;
    }

}
