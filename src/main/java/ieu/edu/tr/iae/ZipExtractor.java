package ieu.edu.tr.iae;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipExtractor {

    public void extractZipFilesInDirectory(String directoryPath) throws IOException {
        File directory = new File(directoryPath);
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("The provided path is not a directory.");
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".zip")) {
                System.out.println(file.getName());
                extractZipFile(file, directory);
            }
        }
    }

    private void extractZipFile(File zipFile, File destinationDirectory) throws IOException {
        byte[] buffer = new byte[4096];

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry = zipInputStream.getNextEntry();
            while (entry != null) {
                String entryFileName = entry.getName();
                File entryFile = new File(destinationDirectory, entryFileName);

                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    // Ensure the parent directories exist
                    entryFile.getParentFile().mkdirs();

                    // Extract the file
                    try (FileOutputStream outputStream = new FileOutputStream(entryFile)) {
                        int readBytes;
                        while ((readBytes = zipInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, readBytes);
                        }
                    }
                }

                zipInputStream.closeEntry();
                entry = zipInputStream.getNextEntry();
            }
        }
    }
}
