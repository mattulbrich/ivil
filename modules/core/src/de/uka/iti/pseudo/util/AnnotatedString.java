/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */

package de.uka.iti.pseudo.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

import nonnull.NonNull;
import nonnull.Nullable;

/**
 * This class allows to build a string of nested blocks. To each block is
 * assigned an attribute which is an object of type T.
 * 
 * This is very useful for creating string representation so that for any
 * position in the string the smallest enclosing subexpression can be obtained
 * easily.
 * 
 * This is comparable to XML texts. Consider
 * <code>"without Block &lt;block attr="value1"&gt;text in 1 and &lt;block attr="value2"&gt;
 * this in nested block&lt;/block&gt;after block&lt;/block&gt;"</code>
 * 
 * You can obtain this nesting by something like:
 * 
 * <pre>
 * AnnotatedString&lt;String&gt; as = new AnnotatedString&lt;String&gt;();
 * as.append(&quot;without Block &quot;);
 * as.begin(&quot;value1&quot;);
 * as.append(&quot;text in 1 and &quot;);
 * as.begin(&quot;value2&quot;);
 * as.append(&quot;this in nested block&quot;);
 * as.end();
 * as.append(&quot;after block&quot;);
 * as.end();
 * </pre>
 * 
 * <H3>Some example results:</H3>
 * <table>
 * <tr>
 * <th>index</th>
 * <th>corresp. text</th>
 * <th>annotation</th>
 * <th>annotation index</th>
 * </tr>
 * <tr>
 * <td>0-13</td>
 * <td>withou...</td>
 * <td>null</td>
 * <td>-1</td>
 * </tr>
 * <tr>
 * <td>14-27</td>
 * <td>text in...</td>
 * <td>"value1"</td>
 * <td>0</td>
 * </tr>
 * <tr>
 * <td>28-47</td>
 * <td>this in...</td>
 * <td>"value2"</td>
 * <td>1</td>
 * </tr>
 * <tr>
 * <td>48-58</td>
 * <td>after...</td>
 * <td>"value1"</td>
 * <td>0</td>
 * </tr>
 * <tr>
 * <td>59</td>
 * <td>null</td>
 * <td></td>
 * <td>-1</td>
 * </tr>
 * </table>
 */

public class AnnotatedString<T> implements CharSequence {

    /**
     * The Class Element is used to keep information on one annotated block.
     */
    public static class Element<T> {

        private int begin;
        private int end;
        private T attr;
        
        private Element() {
            // hide constructor from outside
        }

        @Override public String toString() {
            return "Element[begin=" + begin + ";end=" + end + ";attr=" + attr
                    + "]";
        }

        public int getBegin() {
            return begin;
        }

        public int getEnd() {
            return end;
        }

        public T getAttr() {
            return attr;
        }
        
    }

    /**
     * The underlying builder that is used to construct the result.
     */
    private StringBuilder builder = new StringBuilder();

    /**
     * We push newly created annotations on this stack.
     */
    private Stack<Element<T>> elementStack = new Stack<Element<T>>();

    /**
     * The list of all annotated elements.
     */
    private List<Element<T>> allElements = new ArrayList<Element<T>>();

    /*
     * we need to override these since we implement CharSequence
     */
    public char charAt(int index) throws IndexOutOfBoundsException {
        return builder.charAt(index);
    }

    /**
     * get the length of this annotated string
     * @see java.lang.CharSequence#length()
     */
    public int length() {
        return builder.length();
    }

    public CharSequence subSequence(int start, int end) {
        return builder.subSequence(start, end);
    }
    
    /**
     * Gets the last character of the character sequence.
     * 
     * @return the last character if there is one
     * 
     * @throws IndexOutOfBoundsException if the string is empty
     */
    public char getLastCharacter() {
        return charAt(length() - 1);
    }
    
    /**
     * Gets the attribute of the number-th block in the list 
     * 
     * @param number the number of the block
     * 
     * @return the attribute of the block at the given position.
     */
    public T getAttributeOf(int number) {
        return allElements.get(number).attr;
    }

    /**
     * Gets the attribute of the most inner block to which the character at
     * index belongs.
     * 
     * @param index index of the character to inspect
     * 
     * @return the attribute of the innerst block which contains the character
     * at index, null if there is no such block.
     * 
     * @throws IndexOutOfBoundsException if index is outside the bounds of the string
     */
    public @Nullable T getAttributeAt(int index) {
        Element<T> elem = getElement(index);
        if (elem != null)
            return elem.attr;
        else
            return null;
    }

    /**
     * Gets the index of the innermost block to which the character at index
     * belongs.
     * 
     * @param index index of the character to inspect
     * 
     * @return the largest index of a block, so that the character at index
     * belongs to the block
     */
//    @Deprecated
//    public int getAttributeIndexAt(int index) {
//        Element<T> element = getElement(index);
//        if (element != null)
//            return allElements.indexOf(element);
//        else
//            return -1;
//    }

    /**
     * get a list of all attributes in the annotation blocks in order of
     * appearance.
     * 
     * @return a freshly created list of annotation objects.
     */
    public List<T> getAllAttributes() {
        ArrayList<T> retval = new ArrayList<T>();
        for (Element<T> elem : allElements) {
            retval.add(elem.attr);
        }
        return retval;
    }
    
    
    /**
     * Get a list of all annotation blocks in order of appearance.
     * 
     * @return an unmodifiable reference to the list of all elements.
     */
    public List<Element<T>> getAllAnnotations() {
        return Collections.unmodifiableList(allElements);
    }

    /**
     * Append text to the string. All open blocks remain open and the appended
     * texts belongs to them
     * 
     * @param string the string ao append
     * 
     * @return reference to <code>this</code>
     */
    public AnnotatedString<T> append(@NonNull String string) {
        builder.append(string);
        return this;
    }

    /**
     * Begin a new annotated block. Use the parameter as annotation object.
     * 
     * @param attr the annotation to use in the new block
     * 
     * @return reference to <code>this</code>
     */
    public AnnotatedString<T> begin(T attr) {
        Element<T> newElem = new Element<T>();
        newElem.begin = length();
        newElem.attr = attr;
        elementStack.push(newElem);
        allElements.add(newElem);

        return this;
    }

    /**
     * End the block that has been opened last.
     * 
     * @return reference to <code>this</code>
     * 
     * @throws EmptyStackException if there is no open block.
     */
    public AnnotatedString<T> end() throws EmptyStackException {
        Element<T> element = elementStack.pop();
        element.end = length();
        return this;
    }

    /**
     * Checks for open annotation blocks.
     * 
     * @return true iff there is no open block
     */
    public boolean hasEmptyStack() {
        return elementStack.isEmpty();
    }
    
    /**
     * Gets the beginning index of the block with the number number.
     * 
     * @param number
     *            the number of the block to inspect
     * 
     * @return the beginning of the block with the given number
     */
//    public int getBeginOf(int number) {
//        return allElements.get(number).begin;
//    }

    /**
     * Gets the beginning index of the innermost block which contains the
     * character at index.
     * 
     * @param index index to consider for lookup
     * 
     * @return the index of the innermost block, 0 if there is no such block
     */
    public int getBeginAt(int index) {
        Element<T> elem = getElement(index);
        if (elem != null)
            return elem.begin;
        else
            return 0;
    }

    /**
     * Gets the end index of the block with the number number.
     * 
     * @param number
     *            the number of the block to inspect
     * 
     * @return the end of the block with the given number
     */
//    public int getEndOf(int number) {
//        return allElements.get(number).end;
//    }

    /**
     * Gets the end index of the innermost block which contains the character at
     * index.
     * 
     * @param index index to consider for lookup
     * 
     * @return the index of the innermost block, equal to {@link #length()} if
     * no such block
     */
    public int getEndAt(int index) {
        Element<T> elem = getElement(index);
        if (elem != null)
            return elem.end;
        else
            return length();
    }

    // I could hashmap that or possibly keep it in an array.
    // If it is too slow
    /*
     * Gets the element at an index. Linear search is used. If multiple blocks
     * are found, the last (=innermost) wins.
     */
    private Element<T> getElement(int index) {
        Element<T> retval = null;
        for (Element<T> element : allElements) {

            if (element.begin <= index && element.end > index)
                retval = element;

            if (element.begin > index)
                break;
        }
        return retval;
    }

    @Override 
    public String toString() {
        return builder.toString();
    }
    
}
