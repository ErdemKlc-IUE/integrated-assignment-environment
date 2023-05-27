package ieu.edu.tr.iae;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class ZipExtractor {

    public void extractAndCompileZipFilesInDirectory(String directoryPath, TreeView<Submission> treeView)
            throws IOException {
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
                System.out.println("Extracting and compiling: " + file.getName());
                Submission submission = extractAndCompileZipFile(file);
                if (submission != null) {
                    TreeItem<Submission> submissionItem = new TreeItem<>(submission);
                    treeView.getRoot().getChildren().add(submissionItem);
                }
            }
        }
    }

    private Submission extractAndCompileZipFile(File zipFile) throws IOException {
        byte[] buffer = new byte[4096];

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile))) {
            File extractDirectory = createExtractDirectory(zipFile);
            ZipEntry entry = zipInputStream.getNextEntry();
            while (entry != null) {
                String entryFileName = entry.getName();
                File entryFile = new File(extractDirectory, entryFileName);

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

                    // Compile and execute the file based on the file extension
                    String fileExtension = getFileExtension(entryFileName);
                    Output output = compileAndRunFile(entryFile, fileExtension);
                    return new Submission(zipFile.getName(), output.getResult(),
                            Configuration.getInstance().expectedOutput);
                }

                zipInputStream.closeEntry();
                entry = zipInputStream.getNextEntry();
            }
        }

        return null;
    }

    private File createExtractDirectory(File zipFile) {
        String extractDirectoryPath = zipFile.getParent() + File.separator + getFileNameWithoutExtension(zipFile);
        File extractDirectory = new File(extractDirectoryPath);
        extractDirectory.mkdirs();
        return extractDirectory;
    }

    private String getFileNameWithoutExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    private Output compileAndRunFile(File file, String fileExtension) throws IOException {
        String filePath = file.getPath();

        String executionOutput = "";

        switch (fileExtension) {
            case "java":
                JavaCompiler javaCompiler = new JavaCompiler(file.getParentFile());
                System.out.println(file.getParentFile());
                try {
                    Output compilationOutput = javaCompiler.compile(filePath, "");
                    System.out.println(compilationOutput.getResult());

                    executionOutput = compilationOutput.getResult();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "py":
                PythonInterpreter pythonInterpreter = new PythonInterpreter(file.getParentFile());
                try {
                    Output executionOutputObj = pythonInterpreter.compile(filePath, "");
                    executionOutput = executionOutputObj.getResult();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "c":
                CCompiler cCompiler = new CCompiler(file.getParentFile());
                try {
                    Output compilationOutput = cCompiler.compile(filePath, "");
                    System.out.println(compilationOutput.getResult());
                    executionOutput = compilationOutput.getResult();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "cpp":
                CPPCompiler cppCompiler = new CPPCompiler(file.getParentFile());
                try {
                    Output compilationOutput = cppCompiler.compile(filePath, "");
                    executionOutput = compilationOutput.getResult();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("Unsupported file extension: " + fileExtension);
                break;
        }

        return new Output(1, executionOutput, "");
    }
}
