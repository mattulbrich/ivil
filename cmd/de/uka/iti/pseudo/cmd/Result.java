package de.uka.iti.pseudo.cmd;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Result {
    private File file;
    private List<String> messages = new ArrayList<String>();
    boolean success;
    
    public Result(boolean result, File file, String... messages) {
        this.success = result;
        this.file = file;
        this.messages.addAll(Arrays.asList(messages));
    }

    public Result(boolean result, File file, ArrayList<String> messages) {
        this.success = result;
        this.file = file;
        this.messages.addAll(messages);
    }

    public boolean getSuccess() {
        // TODO Implement Result.getSuccess
        return false;
    }

    public String getFile() {
        return file.toString();
    }

    public void print(PrintStream err) {
        err.println(file + " :");
        for (String m : messages) {
            err.println(m);
        }
    }
    
}