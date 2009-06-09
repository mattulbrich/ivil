package de.uka.iti.pseudo.parser;

public interface ASTLocatedElement {
    
    ASTLocatedElement BUILTIN = new ASTLocatedElement() {
		public String getLocation() { return "#builtin"; }   	
    };

	public String getLocation();
    
}
