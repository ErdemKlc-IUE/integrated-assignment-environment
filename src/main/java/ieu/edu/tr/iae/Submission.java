package ieu.edu.tr.iae;

public class Submission {
    private String id;
    private String output;
    private String expectedOutput;

    public Submission(String id, String output, String expectedOutput) {
        this.id = id;
        this.output = output;
        this.expectedOutput = expectedOutput;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }
}
