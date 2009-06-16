package de.uka.iti.pseudo.environment;

public class SourceAnnotation extends ProgramAnnotation {
    
    private String source;

    public SourceAnnotation(String source, int statementNo) {
        super(statementNo);
        this.source = source;
    }

    public String toString() {
        return source;
    }

}
