package ieu.edu.tr.iae;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

                    // Compile the file based on the file extension
                    String fileExtension = getFileExtension(entryFileName);
                    String compilationOutput = compileFile(entryFile, fileExtension);
                    return new Submission(zipFile.getName(), compilationOutput,
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

    private String compileFile(File file, String fileExtension) throws IOException {
        String filePath = file.getAbsolutePath();
        String compilationOutput;

        switch (fileExtension) {
            case "java":
                compilationOutput = compileJavaFile(filePath);
                break;
            case "py":
                compilationOutput = runPythonInterpreter(filePath);
                break;
            case "c":
                compilationOutput = compileCFile(filePath);
                break;
            case "cpp":
                compilationOutput = compileCPPFile(filePath);
                break;
            default:
                System.out.println("Unsupported file extension: " + fileExtension);
                compilationOutput = "";
                break;
        }

        return compilationOutput;
    }

    private String compileJavaFile(String filePath) throws IOException {
        String directoryPath = new File(filePath).getParent();
        JavaCompiler compiler = new JavaCompiler(new File(directoryPath));
        try {
            Output compilationOutput = compiler.compile(filePath,Configuration.getInstance().args);
            System.out.println("----");
            System.out.println(compilationOutput.getResult());
            System.out.println("-----");

            return compilationOutput.getResult();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String runPythonInterpreter(String filePath) throws IOException {
        String directoryPath = new File(filePath).getParent();
        PythonInterpreter interpreter = new PythonInterpreter(new File(directoryPath));
        try {
            Output executionOutput = interpreter.compile(filePath,"");
            System.out.println("----");
            System.out.println(executionOutput.getResult());
            System.out.println("-----");

            return executionOutput.getResult();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String compileCFile(String filePath) throws IOException {
        String directoryPath = new File(filePath).getParent();
        CCompiler compiler = new CCompiler(new File(directoryPath));
        try {
            Output compilationOutput = compiler.compile(filePath,"");
            System.out.println("----");
            System.out.println(compilationOutput.getResult());
            System.out.println("-----");

            return compilationOutput.getResult();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String compileCPPFile(String filePath) throws IOException {
        String directoryPath = new File(filePath).getParent();
        CPPCompiler compiler = new CPPCompiler(new File(directoryPath));
        try {
            Output compilationOutput = compiler.compile(filePath,"");
            System.out.println("----");
            System.out.println(compilationOutput.getResult());
            System.out.println("-----");

            return compilationOutput.getResult();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
