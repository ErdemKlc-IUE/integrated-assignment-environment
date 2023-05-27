package ieu.edu.tr.iae;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class CCompiler extends Compiler {
    public CCompiler(File workingDirectory) {
        super(workingDirectory);
    }

    @Override
    public Output compile(String filePath, String args) throws Exception {
        String command = "gcc " + args + " \"" + filePath + "\"";
        return super.compile(command, "");
    }

    @Override
    public Output run(String className) throws Exception {
        String command = "./main";
        return super.run(command);
    }
}