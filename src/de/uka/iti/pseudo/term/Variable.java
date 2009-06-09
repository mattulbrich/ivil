package de.uka.iti.pseudo.term;

public class Variable extends Term {
	
	private String name;

	public Variable(String name) {
		super(new TemporaryTypeVariable());
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
