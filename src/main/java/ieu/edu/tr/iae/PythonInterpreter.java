package ieu.edu.tr.iae;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class PythonInterpreter extends Compiler {
    public PythonInterpreter(File workingDirectory) {
        super(workingDirectory);
    }

    @Override
    public Output run(String command) throws Exception {
        command = "python " + command;
        return super.run(command);
    }
}