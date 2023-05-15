package ieu.edu.tr.iae;

public class Configuration {
    public String name;
    public String assignmentPath;
    public String compilerPath;
    public String args;
    public String expectedOutput;

    private static Configuration instance;

    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public Configuration(String name, String assignmentPath, String compilerPath, String args, String expectedOutput) {
        this.name = name;
        this.assignmentPath = assignmentPath;
        this.compilerPath = compilerPath;
        this.args = args;
        this.expectedOutput = expectedOutput;
    }
    public Configuration(){
    }
}
