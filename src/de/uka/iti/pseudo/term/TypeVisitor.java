package de.uka.iti.pseudo.term;

public interface TypeVisitor {

    Type visit(TypeApplication typeApplication) throws TermException;
    
    Type visit(TypeVariable typeVariable);

}
