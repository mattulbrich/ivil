/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */

package de.uka.iti.pseudo.parser.file;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.parser.ASTVisitException;

/**
 * The class ASTFileElement is the base class for all AST elements that arise
 * when parsing a file.
 * 
 * Terms are atomic elements here themselves. The term AST structures are
 * distinct from these elements
 * 
 */
public abstract class ASTFileElement implements ASTLocatedElement {

    /**
     * The file name of the parsed file.
     */
    private String fileName;

    /**
     * The children elements
     */
    List<ASTFileElement> children;

    /**
     * The "accept" method of the visitor pattern. All extending classes must
     * call an appropriate visit method on the visitor
     * 
     * @param v
     *            the visitor to call back
     * 
     * @throws ASTVisitException
     *             may be thrown by the visitor visit method
     */
    public abstract void visit(ASTFileVisitor v) throws ASTVisitException;

    /**
     * Sets the filename of this element. This property is propagated to all
     * children.
     * 
     * @param fileName
     *            the new filename
     */
    public void setFilename(String fileName) {
        this.fileName = fileName;
        for (ASTFileElement element : getChildren()) {
            element.setFilename(fileName);
        }
    }

    /**
     * Adds a list of elements to the list of children.
     * 
     * @param children
     *            elements to add
     */
    protected void addChildren(List<? extends ASTFileElement> children) {
        for (ASTFileElement fileElement : children) {
            addChild(fileElement);
        }
    }

    /**
     * Adds one AST element to the list of children
     * 
     * @param element
     *            the element to add
     */
    protected void addChild(ASTFileElement element) {
        if (this.children == null)
            this.children = new LinkedList<ASTFileElement>();

        assert element != null;

        this.children.add(element);
    }

    /**
     * Gets the list of children.
     * 
     * @return the children
     */
    public List<ASTFileElement> getChildren() {
        if (children == null)
            return Collections.emptyList();
        else
            return children;
    }

    /**
     * AST elements are represented as strings by their short class name
     */
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * Dump the ast to standard out.
     */
    public void dumpTree() {
        dumpTree(0);
    }

    /*
     * Dump tree to stdout, indented by level
     */
    private void dumpTree(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("  ");
        }
        System.out.println(this);

        for (ASTFileElement child : getChildren()) {
            child.dumpTree(level + 1);
        }
    }

    /**
     * Gets the associated file name.
     * 
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets a string that describes the location at which this element stood in
     * the sources.
     * 
     * The format is [filename]:[line number]:[column number]
     * 
     * The location is produced using a the location information of a dedicated
     * token associated to every element. This token is obtained by
     * {@link #getLocationToken()}.
     * 
     * @return a location describing string
     */
    public String getLocation() {
        String retval;
        if (fileName != null)
            retval = fileName;
        else
            retval = "";

        Token token = getLocationToken();
        if (token != null)
            retval += ":" + token.beginLine + ":" + token.beginColumn;

        return retval;
    }

    /**
     * Gets the location token associated with this element.
     * 
     * Every extending class must provide an implementation which returns
     * a token which is used to describe the location of the element. usually
     * the first token of the element.
     * 
     * @return the location token
     * @see #getLocation()
     */
    protected abstract Token getLocationToken();

}
