package de.uka.iti.pseudo.term;

// TODO DOC

public interface TermVisitor {

	void visit(Variable variable) throws TermException;

	void visit(Binding binding) throws TermException;

	void visit(Application application) throws TermException;

    void visit(SchemaVariable schemaVariable) throws TermException;

    void visit(SchemaProgram schemaProgramTerm) throws TermException;

    void visit(LiteralProgramTerm literalProgramTerm) throws TermException;

    void visit(UpdateTerm updateTerm) throws TermException;

    void visit(SchemaUpdateTerm schemaUpdateTerm) throws TermException;

}
