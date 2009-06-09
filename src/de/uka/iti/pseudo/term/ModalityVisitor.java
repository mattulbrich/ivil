package de.uka.iti.pseudo.term;

// TODO DOC

public interface ModalityVisitor {

    void visit(AssignModality assignModality) throws TermException;

    void visit(CompoundModality compoundModality) throws TermException;

    void visit(IfModality ifModality) throws TermException;

    void visit(SkipModality skipModality) throws TermException;

    void visit(WhileModality whileModality) throws TermException;

    void visit(SchemaModality schemaModality) throws TermException;

}
