package de.uka.iti.pseudo.term;

public class Variable extends Term {
	
	String name;

	public Variable(String name) {
		super(new TemporaryTypeVariable());
		this.name = name;
	}

}
