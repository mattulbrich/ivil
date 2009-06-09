package de.uka.iti.pseudo.parser.term;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.parser.ASTVisitException;

public abstract class ASTElement implements Iterable<ASTElement>, ASTLocatedElement {

    private String fileName;

    private List<ASTElement> children;

    public abstract void visit(ASTVisitor v) throws ASTVisitException;

    public void setFilename(String fileName) {
        this.fileName = fileName;
        for (ASTElement element : this) {
            element.setFilename(fileName);
        }
    }

    protected void addChildren(List<? extends ASTElement> elements) {
        for (ASTElement fileElement : elements) {
            addChild(fileElement);
        }
    }

    protected void addChild(ASTElement element) {
        if (this.children == null)
            this.children = new LinkedList<ASTElement>();

        assert element != null;

        this.children.add(element);
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

        for (ASTElement child : this) {
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
