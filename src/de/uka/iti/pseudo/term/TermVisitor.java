package de.uka.iti.pseudo.term;

public interface TermVisitor {

	void visit(Variable variable);

	void visit(ModalityTerm modalityTerm);

	void visit(Binding binding);

	void visit(Application application);

}
