package com.skystreamtv.element_ez_stream.updater.utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Files Util for copy files and directories to new locations
 */

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Files {

    private long done = 0;
    private long total;
    private ProgressListener listener;

    public Files(ProgressListener listener) {
        this.listener = listener;
    }

    public void copyDirectory(File srcDir, File destDir) throws IOException {
        calculateTotal(srcDir);
        copyDirectory(srcDir, destDir, true);
    }

    private void calculateTotal(File srcDir) {
        File[] files = srcDir.listFiles();
        for (File file : files) {
            if (file.isFile())
                total += file.length();
            else if (file.isDirectory() && !file.getName().contains("addon_data")) {
                calculateTotal(file);
            }
        }
    }

    private void copyDirectory(File srcDir, File destDir,
                               boolean preserveFileDate) throws IOException {
        copyDirectory(srcDir, destDir, null, preserveFileDate);
    }

    private void copyDirectory(File srcDir, File destDir,
                               FileFilter filter, boolean preserveFileDate) throws IOException {
        if (srcDir == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (!srcDir.exists()) {
            throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
        }
        if (!srcDir.isDirectory()) {
            throw new IOException("Source '" + srcDir + "' exists but is not a directory");
        }
        if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
            throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
        }

        // Cater for destination being directory within the source directory (see IO-141)
        List<String> exclusionList = null;
        if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath())) {
            File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
            if (srcFiles != null && srcFiles.length > 0) {
                exclusionList = new ArrayList<>(srcFiles.length);
                for (File srcFile : srcFiles) {
                    File copiedFile = new File(destDir, srcFile.getName());
                    exclusionList.add(copiedFile.getCanonicalPath());
                }
            }
        }
        doCopyDirectory(srcDir, destDir, filter, preserveFileDate, exclusionList);
    }

    private void doCopyDirectory(File srcDir, File destDir, FileFilter filter,
                                 boolean preserveFileDate, List exclusionList) throws IOException {
        if (destDir.exists()) {
            if (!destDir.isDirectory()) {
                throw new IOException("Destination '" + destDir + "' exists but is not a directory");
            }
        } else {
            if (!destDir.mkdirs()) {
                throw new IOException("Destination '" + destDir + "' directory cannot be created");
            }
            if (preserveFileDate) {
                destDir.setLastModified(srcDir.lastModified());
            }
        }
        if (!destDir.canWrite()) {
            throw new IOException("Destination '" + destDir + "' cannot be written to");
        }
        // recurse
        File[] files = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + srcDir);
        }

//        File root = Environment.getExternalStorageDirectory();
//        BufferedWriter out;
//        File logFile = new File(root, "log.txt");
//        FileWriter logwriter = new FileWriter(logFile, true);
//        out = new BufferedWriter(logwriter);

        for (File file : files) {
            if (file.getName().contains("addon_data") || file.getName().contains("favorites") ||
                    file.getName().contains("profile") || file.getCanonicalPath().contains("addon_data") ||
                    (file.getName().contains("sources") && !file.getName().contains("resources")) ||
                    file.getName().contains("onechannel"))
                continue;

//            out.write(file.getCanonicalPath() + "\n");

            File copiedFile = new File(destDir, file.getName());
            if (exclusionList == null || !exclusionList.contains(file.getCanonicalPath())) {
                if (file.isDirectory()) {
                    doCopyDirectory(file, copiedFile, filter, preserveFileDate, exclusionList);
                } else {
                    doCopyFile(file, copiedFile, preserveFileDate);
                }
            }
        }
//        out.close();
    }

    private void doCopyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' exists but is a directory");
        }

        FileInputStream input = new FileInputStream(srcFile);
        try {
            FileOutputStream output = new FileOutputStream(destFile);
            try {
                IOUtils.copy(input, output);
            } finally {
                IOUtils.closeQuietly(output);
            }
        } finally {
            IOUtils.closeQuietly(input);
        }

        if (srcFile.length() != destFile.length()) {
            throw new IOException("Failed to copy full contents from '" +
                    srcFile + "' to '" + destFile + "'");
        }
        if (preserveFileDate) {
            destFile.setLastModified(srcFile.lastModified());
        }

        done += srcFile.length();
        if (listener != null) {
            int progress = (int) (66 + (done * 33 / total));
            listener.publishFileProgress(progress);
        }
    }

    public interface ProgressListener {
        void publishFileProgress(int progress);
    }
}
