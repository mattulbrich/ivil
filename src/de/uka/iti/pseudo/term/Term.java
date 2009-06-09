package de.uka.iti.pseudo.term;

public abstract class Term {
	
	private static final Term[] NO_ARGUMENTS = new Term[0];
	
	private Term[] subterms;
	
	private Type type;

	protected Term(Term[] subterms) throws TermException {
		
		assert subterms != null;
		this.subterms = subterms;
		
		Type type = inferType();
		assert type == null;

		this.type = type;
	}
	
	protected Term(Term[] subterms, Type type) {
		
		assert subterms != null;
		assert type != null;
		
		this.subterms = subterms;
		this.type = type;
	}
	
	protected Term() throws TermException {
		this(NO_ARGUMENTS);
	}
	
	public Term(Type type) {
		this(NO_ARGUMENTS, type);
	}

	public Type getType() {
		return type;
	}

	public int countSubterms() {
		return subterms.length;
	}
	
	public Term getSubterm(int i) {
		return subterms[i];
	}
	
	protected Type inferType() {
		throw new Error("The method may only be called on certain subclasses");
	};

	
}
