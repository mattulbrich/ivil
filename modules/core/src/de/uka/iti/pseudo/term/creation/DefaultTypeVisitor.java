package de.uka.iti.pseudo.term.creation;

import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVisitor;

@SuppressWarnings("nullness")
public class DefaultTypeVisitor<A> implements TypeVisitor<Void, A> {

    @Override
    public Void visit(TypeApplication typeApplication, A argument)
            throws TermException {
        typeApplication.acceptDeep(this, argument);
        return null;
    }

    @Override
    public Void visit(TypeVariable typeVariable, A argument)
            throws TermException {
        return null;
    }

    @Override
    public Void visit(SchemaType schemaTypeVariable, A argument)
            throws TermException {
        return null;
    }
}
