/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.proof.serialisation;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.NoSuchElementException;
import java.util.Stack;

import nonnull.NonNull;

/**
 * This is a very simple XML writer which keeps track of open elements and
 * allows automatic indention.
 * 
 * It does not check whether the content is correctly formatted. It does not
 * support namespaces, uri or similiar more advanced xml stuff.
 * 
 * To encode special characters as xml-entities use the method
 * {@link #appendEncoded(String)} which ensures quotation marks, less-than and
 * greater-than symbols to be correctly escaped.
 */
public class XMLWriter extends FilterWriter {

    /**
     * the stack of open elements
     */
    private Stack<String> elements = new Stack<String>();

    /**
     * true if a line has just been broken using {@link #newline()}
     */
    private boolean onNewLine;

    /**
     * Instantiates a new XMLWriter wrapping another writer
     * 
     * @param out
     *            the writer to which all output is sent
     */
    public XMLWriter(@NonNull Writer out) {
        super(out);
    }

    /**
     * Start a new element.
     * 
     * Attributes included into the openening tag can be added. They are
     * presented as pairs of strings. First the attribute name then the
     * attribute value. The attribute parameters must therefore appear in an
     * even number
     * 
     * @param element
     *            the element name to be started
     * @param attributes
     *            an even number as name/value pairs
     * 
     * @return this
     * 
     * @throws IllegalArgumentException
     *             if the length of attributes is not even
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public XMLWriter start(String element, String... attributes)
            throws IOException {
        if (attributes.length % 2 != 0)
            throw new IllegalArgumentException(
                    "attributes and values must come in pairs");

        possiblyIndent();

        write("<");
        write(element);
        for (int i = 0; i < attributes.length; i += 2) {
            write(" ");
            write(attributes[i]);
            write("=\"");
            write(attributes[i + 1]);
            write("\"");
        }
        write(">");

        elements.push(element);
        return this;
    }

    /**
     * End the element on top of the stack.
     * 
     * If first thing on a line, a number of whitespaces may be inserted to
     * indent the line.
     * 
     * @return this
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws NoSuchElementException
     *             if there is no open xml-element
     */
    public XMLWriter end() throws IOException {
        String element = elements.pop();
        possiblyIndent();
        write("</");
        write(element);
        write(">");
        return this;
    }

    /**
     * write a new line character.
     * 
     * If the next line begins with a opening or closing tag, the line will be
     * correctly indented. It will not if "data" is sent.
     * 
     * @return this
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public XMLWriter newline() throws IOException {
        write("\n");
        onNewLine = true;
        return this;
    }

    private void possiblyIndent() throws IOException {
        if (onNewLine) {
            for (int i = 0; i < elements.size(); i++) {
                write("  ");
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * This method returns an XMLWriter, however
     * 
     * @return this
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public XMLWriter append(CharSequence csq) throws IOException {
        super.append(csq);
        return this;
    }

    /**
     * Append some text with XML-sensitive charaters encoded.
     * 
     * This methods add text to the output stream, but escapes the following
     * characters:
     * <ul>
     * <li>the ampersand &amp; to <code>&amp;amp;<code>
     * <li>the less-than &lt; to <code>&amp;lt;</code>
     * <li>the greater-than &gt; to <code>&amp;gt;</code>
     * <li>the quotation mark &quot; to <code>&amp;quot;</code>
     * </ul>
     * 
     * Other characters are taken as they are.
     * 
     * @param value the string to escape and append
     * 
     * @return this
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public XMLWriter appendEncoded(String value) throws IOException {
        value = value.replace("&", "&amp;");
        value = value.replace("<", "&lt;");
        value = value.replace(">", "&gt;");
        value = value.replace("\"", "&quot;");
        return append(value);
    }

    // the three write methods should invalidate onNewLine

    @Override public void write(char[] cbuf, int off, int len)
            throws IOException {
        super.write(cbuf, off, len);
        onNewLine = false;
    }

    @Override public void write(int c) throws IOException {
        super.write(c);
        onNewLine = false;
    }

    @Override public void write(String str, int off, int len)
            throws IOException {
        super.write(str, off, len);
        onNewLine = false;
    }

}
