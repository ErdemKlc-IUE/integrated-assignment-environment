package ieu.edu.tr.iae;

public class Output {
    private String output;
    private String result;
    private int exitCode;
    private String error;

    public Output(int exitCode, String output, String error) {
        this.output = output;

        this.exitCode = exitCode;
        this.error = error;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }



    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
