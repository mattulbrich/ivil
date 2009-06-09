package de.uka.iti.pseudo.term;

// TODO DOC

public interface TypeVisitor {

    Type visit(TypeApplication typeApplication) throws TermException;
    
    Type visit(TypeVariable typeVariable) throws TermException;

}
