package de.uka.iti.pseudo.parser.file;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class ASTFileElement implements Iterable<ASTFileElement> {
	
	private String fileName;
	List<ASTFileElement> children; 

	public abstract void visit(ASTFileVisitor v);

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
		if(this.children == null)
			this.children = new LinkedList<ASTFileElement>();
		
		assert element != null;
		
		this.children.add(element);
	}

	public Iterator<ASTFileElement> iterator() {
		if(children == null)
			return Collections.<ASTFileElement>emptyList().iterator();
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
			child.dumpTree(level+1);
		}
	}
	
}
