package de.uka.iti.pseudo.term;

import java.util.Collection;

public class TypeVariable extends Type {

    private String typeVar;
    
    /* to distinguish from normal type variables, they can have same name */
    private boolean formal;

    public TypeVariable(String typeVar) {
    	this(typeVar, false);
    }
    
    public TypeVariable(String typeVar, boolean formal) {
        this.typeVar = typeVar;
        this.formal = formal;
	}

	@Override
    public void collectTypeVariables(Collection<String> coll) {
        coll.add(typeVar);
    }
    
    @Override
    public String toString() {
        return typeVar; 
    }

	public boolean isFormal() {
		return formal;
	}

}
