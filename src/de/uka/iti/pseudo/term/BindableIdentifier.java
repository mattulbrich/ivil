package de.uka.iti.pseudo.term;

// TODO Documentation needed
public interface BindableIdentifier {

    public String getName();
    
    public String toString(boolean typed);

    public Type getType();
}
