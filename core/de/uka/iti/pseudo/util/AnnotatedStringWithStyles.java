/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import nonnull.NonNull;

/**
 * This class is an extension to annotated strings.
 * 
 * Here, in addition to the base class which allows nested annotation, styles
 * can be added to parts of the string.
 * 
 * Styles are during the creation of the string. The {@link #setStyle(String)}
 * method may be called to start a new styled area. Text may be added and then
 * {@link #resetPreviousStyle()} may be called to set the previously set style
 * again.
 * 
 * Styles can be nested. If a new style is set while an old is active, their
 * attributes are combined.
 * 
 * The styled string can be appended to a {@link Document} using the method
 * {@link #appendToDocument(Document, AnnotatedStringWithStyles.AttributeSetFactory)}.
 * The set factory is used to convert the style strings (which is a
 * concatenation of strings) into SWING {@link AttributeSet}.
 * 
 * Consider the following example:
 * 
 * <pre>
 * 1. append(&quot;Hello &quot;);
 * 2. setStyle(&quot;A&quot;);
 * 3. append(&quot;world &quot;);
 * 4. setStyle(&quot;B&quot;);
 * 5. append(&quot;again &quot;);
 * 6. resetPreviousStyle();
 * 7. append(&quot;and again&quot;);
 * 8. resetPreviousStyle();
 * </pre>
 * 
 * The different sections of the string have different styles now, according to
 * <table>
 * <tr>
 * <th>chunk</th>
 * <th>style</th>
 * </tr>
 * <tr>
 * <td>Hello</td>
 * <td>""</td>
 * </tr>
 * <tr>
 * <td>world</td>
 * <td>"A"</td>
 * </tr>
 * <tr>
 * <td>again</td>
 * <td>"A B"</td>
 * </tr>
 * <tr>
 * <td>and again</td>
 * <td>"A"</td>
 * </tr>
 * </table>
 */
public class AnnotatedStringWithStyles<Annotation> extends
        AnnotatedString<Annotation> {

    /**
     * Interface which is used to convert strings to attributes
     */
    public static interface AttributeSetFactory {
        /**
         * given a space separated list of styles return a swing attribute set
         * that represents this list.
         * 
         * @param descr
         *            a space separated list of styles
         * @return an attribute set which matches the style description
         */
        public AttributeSet makeStyle(@NonNull String descr);
    }

    /*
     * These two lists keep all style annotations and their positions
     */
    private List<String> styles = new ArrayList<String>();
    private List<Integer> positions = new ArrayList<Integer>();

    /**
     * the stack of the lately set styles.
     */
    private Stack<String> styleStack = new Stack<String>();

    /**
     * start a chunk with a certain style. The style is added to the currently
     * active styles and subsequent calls to append will be formated according
     * to this style set.
     * 
     * @param style
     *            style description for the upcoming text.
     */
    public void setStyle(String style) {
        String oldStyle;
        if (styleStack.isEmpty())
            oldStyle = "";
        else
            oldStyle = styleStack.peek() + " ";

        String newStyle = oldStyle + style;
        styleStack.push(newStyle);
        appendStyle(newStyle);
    }

    /*
     * append the current style to the lists of positions and styles
     */
    private void appendStyle(String newStyle) {
        styles.add(newStyle);
        positions.add(length());
    }

    /**
     * reset the surrounding style for the currently set style. This is the
     * style combination w/o the last style component.
     */
    public void resetPreviousStyle() {
        styleStack.pop();
        if (!styleStack.isEmpty()) {
            appendStyle(styleStack.peek());
        } else {
            appendStyle("");
        }
    }

    /**
     * append the text of this object to a document.
     * 
     * The characters are formatted according to the style information in this
     * object.
     * 
     * The text is appended to the end of the document.
     * 
     * @param document
     *            the document to add the text to
     * @param factory
     *            attribute factory which renders styles to attribute sets.
     */
    public void appendToDocument(Document document, AttributeSetFactory factory) {

        assert styles.size() == positions.size();

        int length = styles.size();
        String string = toString();

        try {

            String str;
            String style;

            if (length > 0) {
                str = string.substring(0, positions.get(0));
                document.insertString(document.getLength(), str, factory
                        .makeStyle(""));

                for (int i = 0; i < length - 1; i++) {
                    Integer begin = positions.get(i);
                    Integer end = positions.get(i + 1);
                    str = string.substring(begin, end);
                    style = styles.get(i);
                    document.insertString(document.getLength(), str, factory
                            .makeStyle(style));
                }

                str = string.substring(positions.get(length - 1));
                style = styles.get(length - 1);
                document.insertString(document.getLength(), str, factory
                        .makeStyle(style));
            } else {
                document.insertString(document.getLength(), string, factory
                        .makeStyle(""));
            }
        } catch (BadLocationException e) {
            // This is designed to never happen
            throw new Error(e);
        }
    }

    /**
     * make sure that there is no open style chunk
     * 
     * @return true iff no style was set without being reset
     */
    @Override public boolean hasEmptyStack() {
        return super.hasEmptyStack() && styleStack.isEmpty();
    }

    /**
     * get the style string at a certain position in the string.
     * 
     * @param pos
     *            position within the string
     * @return the style with which the character will be rendered.
     */
    public String getStyleAt(int pos) {
        String style = "";
        for (int j = 0; j < positions.size(); j++) {
            if (positions.get(j) > pos)
                return style;
            style = styles.get(j);
        }
        return style;
    }

    // @Override public void clear() {
    // super.clear();
    // styleStack.clear();
    // styles.clear();
    // positions.clear();
    // }
}
