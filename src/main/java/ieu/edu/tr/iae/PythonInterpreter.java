package ieu.edu.tr.iae;

import java.io.*;

public class PythonInterpreter extends Compiler {

    public PythonInterpreter(File workingDirectory) {
        super(workingDirectory);
    }

    @Override
    public Output compile(String filePath, String args) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("python", filePath);
        processBuilder.directory(workingDirectory);
        Process process = processBuilder.start();


        InputStream output = process.getInputStream();
        InputStream error = process.getErrorStream();


        String outputResult = consumeStream(output);
        String errorResult = consumeStream(error);


        int exitCode = process.waitFor();

        return new Output( exitCode,errorResult,outputResult);
    }

    public String consumeStream(InputStream inputStream) throws IOException {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append(System.lineSeparator());
            }
        }
        return result.toString();
    }
}
