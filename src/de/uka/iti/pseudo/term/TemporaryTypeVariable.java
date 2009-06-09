package de.uka.iti.pseudo.term;


public class TemporaryTypeVariable extends TypeVariable {
	
	private static int counter = 0;
	
	public TemporaryTypeVariable() {
		super("'" + counter, false);
		counter ++;
	}

	
	
}
