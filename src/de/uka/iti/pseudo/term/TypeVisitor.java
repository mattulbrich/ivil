package de.uka.iti.pseudo.term;

/**
 * Allow visiting also for type structures. Even though this is a rather small
 * distinction it can make since in combination with for instance TermVisitor.
 */
public interface TypeVisitor {

    Type visit(TypeApplication typeApplication) throws TermException;
    
    Type visit(TypeVariable typeVariable) throws TermException;

}
