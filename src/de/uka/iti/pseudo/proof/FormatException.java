package de.uka.iti.pseudo.proof;

// TODO DOC
// XXX or throw it away? Very rarely used. use proof exc instead?
public class FormatException extends Exception {
    
    private String kind;
    private String msg;
    private String content;
    
    public FormatException(String kind, String msg, String content) {
        this.kind = kind;
        this.msg = msg;
        this.content = content;
    }
    
    public String getMessage() {
        return "Error in " + kind + ": " + msg + "\nIn: " + content;
    }

}
