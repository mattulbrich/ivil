package de.uka.iti.pseudo.term.creation;

import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVisitor;

public class DefaultTypeVisitor implements TypeVisitor {

    public Type visit(TypeApplication typeApplication) throws TermException {
        Sort sort = typeApplication.getSort();
        Type[] arguments = typeApplication.getArguments();
        Type result[] = new Type[arguments.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = arguments[i].visit(this);
        }
        
        return new TypeApplication(sort, result);
    }

    public Type visit(TypeVariable typeVariable) throws TermException {
        return typeVariable;
    }

}
