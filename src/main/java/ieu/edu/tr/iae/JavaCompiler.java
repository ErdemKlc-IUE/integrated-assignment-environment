package ieu.edu.tr.iae;


import java.io.File;

public class JavaCompiler extends Compiler {
    public JavaCompiler(File workingDirectory) {
        super(workingDirectory);
    }

    @Override
    public Output compile(String filePath, String args) throws Exception {
        String command = "javac " + args + " \"" + filePath + "\"";
        return super.compile(command, "");
    }

    @Override
    public Output run(String className) throws Exception {
        String command = "java " + className;
        return super.run(command);
    }
}