package ieu.edu.tr.iae;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public abstract class Compiler {
    public final File workingDirectory;

    public Compiler(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public Output compile(String path, String args) throws Exception {
        Process process = Runtime.getRuntime().exec(path + " " + args, null, workingDirectory);
        process.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        String output = "";
        String error = "";
        while ((line = reader.readLine()) != null) {
            output += line + "\n";
        }
        while ((line = errorReader.readLine()) != null) {
            error += line + "\n";
        }
        Output outputObj = new Output(process.exitValue(), error, output);
        System.out.println(outputObj.getExitCode() +outputObj.getResult() + outputObj.getError());
        return outputObj;
    }

    public Output run(String command) throws Exception {
        System.out.println("run method");
        Process process = Runtime.getRuntime().exec(command, null, workingDirectory);
        process.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        String output = "";
        String error = "";
        while ((line = reader.readLine()) != null) {
            output += line + "\n";
        }
        while ((line = errorReader.readLine()) != null) {
            error += line + "\n";
        }
        Output outputObj = new Output(process.exitValue(), error, output);
        return outputObj;
    }

}
