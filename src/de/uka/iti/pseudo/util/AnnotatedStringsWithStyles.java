package de.uka.iti.pseudo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

// TODO Documentation needed
public class AnnotatedStringsWithStyles<Annotation> extends AnnotatedString<Annotation> {

    private List<String> styles = new ArrayList<String>();
    private List<Integer> positions = new ArrayList<Integer>();
    
    private Stack<String> styleStack = new Stack<String>();
    
    public void setStyle(String style) {
        String oldStyle; 
        if(styles.isEmpty())
            oldStyle = "";
        else
            oldStyle = styles.get(styles.size() - 1) + " ";
        
        String newStyle = oldStyle + style;
        appendStyle(newStyle);
    }

    private void appendStyle(String newStyle) {
        styleStack.push(newStyle);
        styles.add(newStyle);
        positions.add(length());
    }
    
    public void resetPreviousStyle() {
        styleStack.pop();
        if(!styleStack.isEmpty()) {
            appendStyle(styleStack.pop());
        }
    }

    
    public void appendToDocument(StyledDocument document) {

        assert styles.size() == positions.size();
        
        int length = styles.size();
        String string = toString();
        
        try {
            for (int i = 0; i < styles.size() - 1; i++) {
                Integer begin = positions.get(i);
                Integer end = positions.get(i+1);
                String str = string.substring(begin, end);
                String style = styles.get(i);
                document.insertString(document.getLength(), str, document.getStyle(style));
            }
            
            if(length > 0) {
                String str = string.substring(positions.get(length-1));
                String style = styles.get(length-1);
                document.insertString(document.getLength(), str, document.getStyle(style));
            }
        } catch (BadLocationException e) {
            // This is designed to never happen
            throw new Error(e);
        }
    }
    
    @Override public boolean hasEmptyStack() {
        return super.hasEmptyStack() && styleStack.isEmpty();
    }
}
