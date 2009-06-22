package de.uka.iti.pseudo.environment;

public class SourceAnnotation implements Comparable<SourceAnnotation> {
    
    private String source;
    private int statementNo;

    public SourceAnnotation(String source, int statementNo) {
        this.statementNo = statementNo;
        this.source = source;
    }

    public String toString() {
        return source;
    }

    public int getStatementNo() {
        return statementNo;
    }

    public int compareTo(SourceAnnotation o) {
        return o.statementNo - statementNo;
    }

}
