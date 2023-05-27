package ieu.edu.tr.iae;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class CPPCompiler extends Compiler {
    public CPPCompiler(File workingDirectory) {
        super(workingDirectory);
    }

    @Override
    public Output compile(String filePath, String args) throws Exception {
        String command = "g++ " + args + " \"" + filePath + "\"";
        return super.compile(command, "");
    }
}