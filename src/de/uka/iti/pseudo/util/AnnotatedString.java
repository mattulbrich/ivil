/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */

package de.uka.iti.pseudo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import nonnull.Nullable;


// TODO doc

/**
 * This class allows to build a string with nested attributes. An attribute is
 * an object of type T to which a subsequence of the string belongs. This is
 * very useful for creating term string representation so that for any position
 * in the term the smallest enclosing term can be obtained easily.
 */

public class AnnotatedString<T> implements CharSequence {

    /**
     * The Class Element is used to keep information on one annotated block.
     */
    private static class Element<T> {

        int begin;
        int end;
        T attr;

        @Override 
        public String toString() {
            return "Element[begin=" + begin + ";end=" + end + ";attr=" + attr
                    + "]";
        }
    }
    
    /**
     * The underlying builder that is used to construct the result
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
    public char charAt(int index) {
        return builder.charAt(index);
    }

    public int length() {
        return builder.length();
    }

    public CharSequence subSequence(int start, int end) {
        return builder.subSequence(start, end);
    }

    // TODO javadoc
    
    /**
     * Gets the attribute at.
     * 
     * @param index
     *            the index
     * 
     * @return the attribute at
     */
    public @Nullable T getAttributeAt(int index) {
        Element<T> elem = getElement(index);
        if (elem != null)
            return elem.attr;
        else
            return null;
    }


    /**
     * Append.
     * 
     * @param string
     *            the string
     * 
     * @return the attributed string< t>
     */
    public AnnotatedString<T> append(String string) {
        builder.append(string);
        return this;
    }

    /**
     * Begin.
     * 
     * @param attr
     *            the attr
     * 
     * @return the attributed string< t>
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
     * End.
     * 
     * @return the attributed string< t>
     */
    public AnnotatedString<T> end() {
        
        Element<T> element = elementStack.pop();
        element.end = length();
        return this;
    }

    /**
     * Checks for empty stack.
     * 
     * @return true, if successful
     */
    public boolean hasEmptyStack() {
        return elementStack.isEmpty();
    }

    // public AttributedString<T> append(AttributedString<? extends T> astring)
    // {
    // builder.append(astring.builder);
    // attributes.addAll(astring.attributes);
    //        
    // assert length() == attributes.size();
    // return this;
    // }

    // I could hashmap that
    /**
     * Gets the begin.
     * 
     * @param index
     *            the index
     * 
     * @return the begin
     */
    public int getBegin(int index) {
        Element<T> elem = getElement(index);
        if (elem != null)
            return elem.begin;
        else
            return 0;
    }

    /**
     * Gets the end.
     * 
     * @param index
     *            the index
     * 
     * @return the end
     */
    public int getEnd(int index) {
        Element<T> elem = getElement(index);
        if (elem != null)
            return elem.end;
        else
            return length();
    }

    /**
     * Gets the element.
     * 
     * @param index
     *            the index
     * 
     * @return the element
     */
    private Element<T> getElement(int index) {
        Element<T> retval = null;
        for (Element<T> element : allElements) {

            if (element.begin <= index && element.end > index)
                retval = element;
        }
        return retval;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override public String toString() {
        return builder.toString();
    }

    public char getLastCharacter() {
        return charAt(length() - 1);
    }

}
