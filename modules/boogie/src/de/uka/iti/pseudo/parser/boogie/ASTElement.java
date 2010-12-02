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
package de.uka.iti.pseudo.parser.boogie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nonnull.NonNull;
import nonnull.Nullable;

/**
 * The class ASTElement is the base class for all AST elements that arise
 * when parsing files, programs or terms.
 */
@NonNull
public abstract class ASTElement implements ASTLocatedElement {

    /**
     * The name of the file in which this element is defined.
     * If not read from a file, this can be the resource
     */
    private String fileName;

    /**
     * The children in the syntax tree
     */
    private List<ASTElement> children;
    
    /**
     * The parent element (counterpart to children), is null for a root element
     */
    private ASTElement parent = null;

    /**
     * The "accept" method of the visitor pattern. Any extending class will call
     * the appropriate visit method of the ASTVisitor
     * 
     * @param v
     *            the visitor to visit
     * 
     * @throws ASTVisitException
     *             may be thrown by the ASTVisitor.visit mthod
     */
    public abstract void visit(ASTVisitor v) throws ASTVisitException;

    /**
     * Sets the filename for this element.
     * 
     * <p>Spread the filename to all children (and, hence, their children)
     * 
     * @param fileName the filename or resource name
     */
    public void setFilename(String fileName) {
        this.fileName = fileName;
        for (ASTElement element : getChildren()) {
            element.setFilename(fileName);
        }
    }

    /**
     * Gets the list of children.
     * 
     * @return the children
     */
    public List<ASTElement> getChildren() {
        if(children == null)
            return Collections.emptyList();
        else
            return children;
    }

    /**
     * Adds a list of elements to the list of children
     * 
     * @param elements the elements to add
     */
    protected void addChildren(List<? extends ASTElement> elements) {
        for (ASTElement fileElement : elements) {
            addChild(fileElement);
        }
    }

    /**
     * Adds one element to the list of children
     * 
     * @param element the element
     */
    protected void addChild(@NonNull ASTElement element) {
        if (this.children == null)
            this.children = new ArrayList<ASTElement>(2);

        this.children.add(element);
        element.parent = this;
    }

    /**
     * Dump tree to stdout
     */
    public void dumpTree() {
        dumpTree(0);
    }

    /*
     * Dump tree with indention level
     */
    private void dumpTree(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("  ");
        }
        System.out.println(this);

        for (ASTElement child : getChildren()) {
            child.dumpTree(level + 1);
        }
    }

    /**
     * Gets the file name or resource name
     * 
     * @return the file name to set
     */
    public @Nullable String getFileName() {
        return fileName;
    }
    
    /**
     * Gets a string that describes the location at which this element
     * stood in the sources.
     * 
     * The format is [filename]:[line number]:[column number]
     * 
     * @return a location describing string
     */
    public String getLocation() {
    	String retval;
    	if(fileName != null)
    		retval = fileName;
    	else
    		retval = "";
    	
    	Token token = getLocationToken();
    	if(token != null)
    		retval += ":" + token.beginLine + ":" + token.beginColumn;
    	
    	return retval;
    }

	/**
	 * Every extending class needs to provide a token from which the 
	 * location is extracted
	 * 
	 * @return the location token
	 */
	public abstract Token getLocationToken();

    /**
     * Gets the parent element, null if there is no such element
     * 
     * @return the parent
     */
    public @Nullable ASTElement getParent() {
        return parent;
    }

    /**
     * Replace one of the children.
     * 
     * The child is searched for by identity not by equality.
     * 
     * @param org
     *            the original ast element to be replaced
     * @param replacement
     *            the replacement element to put in its place
     */
    public void replaceChild(ASTElement org, ASTElement replacement) {
        int index = children.indexOf(org);
        if(index != -1) {
            children.set(index, replacement);
            replacement.parent = this;
            org.parent = null;
        }
    }

    public int getTreeSize() {
        int rval = 1;
        if (children != null)
            for (ASTElement e : children) {
                if (this == e.parent) // happens in e.g. var x,y: int
                    rval += e.getTreeSize();
            }

        return rval;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " \"" + getLocationToken().image + "\" " + " @ " + getLocation();
    }
}
