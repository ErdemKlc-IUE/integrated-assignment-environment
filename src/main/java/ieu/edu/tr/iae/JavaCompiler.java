package ieu.edu.tr.iae;

import java.io.File;

public class JavaCompiler extends Compiler {

    public JavaCompiler(File workingDirectory) {
        super(workingDirectory);
    }

    @Override
    public Output compile(String filePath, String args) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("javac", args, filePath);
        processBuilder.directory(workingDirectory);
        Process process = processBuilder.start();

        // Consume the process output and error streams
        String output = consumeStream(process.getInputStream());
        String error = consumeStream(process.getErrorStream());

        int exitCode = process.waitFor();

        return new Output(exitCode, output, error);
    }
}