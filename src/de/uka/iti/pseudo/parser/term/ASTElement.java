/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.parser.term;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import nonnull.NonNull;

import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.parser.ASTVisitException;

public abstract class ASTElement implements ASTLocatedElement {

    private String fileName;

    private List<ASTElement> children;
    private ASTElement parent = null;

    public abstract void visit(ASTVisitor v) throws ASTVisitException;

    public void setFilename(String fileName) {
        this.fileName = fileName;
        for (ASTElement element : getChildren()) {
            element.setFilename(fileName);
        }
    }

    public List<ASTElement> getChildren() {
        return children;
    }

    protected void addChildren(List<? extends ASTElement> elements) {
        for (ASTElement fileElement : elements) {
            addChild(fileElement);
        }
    }

    protected void addChild(@NonNull ASTElement element) {
        if (this.children == null)
            this.children = new LinkedList<ASTElement>();

        this.children.add(element);
        element.parent = this;
    }

    public Iterator<ASTElement> iterator() {
        if (children == null)
            return Collections.<ASTElement> emptyList().iterator();
        else
            return children.iterator();
    }
    
    public String toString() {
        return getClass().getSimpleName();
    }

    public void dumpTree() {
        dumpTree(0);
    }

    private void dumpTree(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("  ");
        }
        System.out.println(this);

        for (ASTElement child : getChildren()) {
            child.dumpTree(level + 1);
        }
    }

    public String getFileName() {
        return fileName;
    }
    
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

	protected abstract Token getLocationToken();

    public ASTElement getParent() {
        return parent;
    }

    public void replaceChild(ASTElement org, ASTElement replacement) {
        int index = children.indexOf(org);
        if(index != -1) {
            children.set(index, replacement);
            replacement.parent = this;
            org.parent = null;
        }
    }


}
