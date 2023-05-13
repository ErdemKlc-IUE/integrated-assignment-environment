package ieu.edu.tr.iae;

import ieu.edu.tr.iae.Output;

import java.io.*;

public class CCompiler extends Compiler {
    public static String compilerPath = "gcc";
    public static String args = "main.c";

    public CCompiler(File workingDirectory) {
        super(workingDirectory);
    }

    @Override
    public Output compile(String path, String args) throws Exception {
        String command = compilerPath + " " + args + " -o output";

        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        processBuilder.directory(workingDirectory);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        String output = readProcessOutput(process.getInputStream());

        int exitCode = process.waitFor();

        return new Output(exitCode, output, null);
    }

    private String readProcessOutput(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString();
    }
}
