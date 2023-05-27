package ieu.edu.tr.iae;

public class Output {

    private String result;
    private int exitCode;
    private String error;

    public Output(int exitCode, String error,String result) {

        this.result = result;
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

}
