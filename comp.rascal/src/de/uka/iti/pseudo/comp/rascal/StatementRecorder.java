package de.uka.iti.pseudo.comp.rascal;

import java.util.ArrayList;
import java.util.List;

public class StatementRecorder {
    
    private static final String INDENT = "  ";
    
    private List<String> strings = new ArrayList<String>();
    private int currentLine = -1;

    public void add(int line, String string) {
        setLine(line);
        add(string);
    }

    public void add(String string) {
        strings.add(INDENT + string);
    }
    
    public void setLine(int line) {
        if(line != currentLine) {
            strings.add("sourceline "+line);
            currentLine = line;
        }
    }

    public void switchOffLine() {
        setLine(-1);
    }

    // without indention
    public void addLabel(String string) {
        strings.add(string + ":");
    }

    public List<String> getStrings() {
        return strings;
    }

    public void addAll(StatementRecorder recoder) {
        strings.addAll(recoder.strings);
    }

    public void setLineOf(TokenNode node) {
        setLine(node.getToken().beginLine);
    }

}
