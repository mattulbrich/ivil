package de.uka.iti.pseudo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

// TODO Documentation needed
public class AnnotatedStringWithStyles<Annotation> extends AnnotatedString<Annotation> {
    
    public static interface AttributeSetFactory {
        public AttributeSet makeStyle(String descr);
    }

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

    
    public void appendToDocument(Document document, AttributeSetFactory factory) {

        assert styles.size() == positions.size();
        
        int length = styles.size();
        String string = toString();
        
        try {
            
            String str;
            String style;
            
            if(length > 0) {
                str = string.substring(0, positions.get(0));
                document.insertString(document.getLength(), str, factory.makeStyle(""));

                for (int i = 0; i < length - 1; i++) {
                    Integer begin = positions.get(i);
                    Integer end = positions.get(i+1);
                    str = string.substring(begin, end);
                    style = styles.get(i);
                    document.insertString(document.getLength(), str, factory.makeStyle(style));
                }
                
                str = string.substring(positions.get(length-1));
                style = styles.get(length-1);
                document.insertString(document.getLength(), str, factory.makeStyle(style));
            } else {
                document.insertString(document.getLength(), string, factory.makeStyle(""));
            }
        } catch (BadLocationException e) {
            // This is designed to never happen
            throw new Error(e);
        }
    }
    
    @Override public boolean hasEmptyStack() {
        return super.hasEmptyStack() && styleStack.isEmpty();
    }
    
//    @Override public void clear() {
//        super.clear();
//        styleStack.clear();
//        styles.clear();
//        positions.clear();
//    }
}
