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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.parser.ASTVisitException;

public abstract class ASTFileElement implements Iterable<ASTFileElement>, ASTLocatedElement {

    private String fileName;

    List<ASTFileElement> children;

    public abstract void visit(ASTFileVisitor v) throws ASTVisitException;

    public void setFilename(String fileName) {
        this.fileName = fileName;
        for (ASTFileElement element : this) {
            element.setFilename(fileName);
        }
    }

    protected void addChildren(List<? extends ASTFileElement> children) {
        for (ASTFileElement fileElement : children) {
            addChild(fileElement);
        }
    }

    protected void addChild(ASTFileElement element) {
        if (this.children == null)
            this.children = new LinkedList<ASTFileElement>();

        assert element != null;

        this.children.add(element);
    }
    
    public List<ASTFileElement> getChildren() {
        if(children == null)
            return Collections.emptyList();
        else
            return children;
    }

    public Iterator<ASTFileElement> iterator() {
        if (children == null)
            return Collections.<ASTFileElement> emptyList().iterator();
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

        for (ASTFileElement child : this) {
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
    
}
