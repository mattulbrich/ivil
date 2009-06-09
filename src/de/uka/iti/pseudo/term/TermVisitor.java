package de.uka.iti.pseudo.term;

// TODO DOC

public interface TermVisitor {

	void visit(Variable variable) throws TermException;

	void visit(ModalityTerm modalityTerm)  throws TermException;

	void visit(Binding binding) throws TermException;

	void visit(Application application) throws TermException;

    void visit(SchemaVariable schemaVariable) throws TermException;

}
