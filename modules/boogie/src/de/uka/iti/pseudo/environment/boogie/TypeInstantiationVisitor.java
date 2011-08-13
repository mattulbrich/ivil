package de.uka.iti.pseudo.environment.boogie;

import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.util.DefaultASTVisitor;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVisitor;
import de.uka.iti.pseudo.term.creation.DefaultTypeVisitor;

class TypeInstantiationVisitor extends DefaultASTVisitor {

    public TypeInstantiationVisitor(final EnvironmentCreationState state) {
        this.state = state;
    }

    final private EnvironmentCreationState state;
    
    final private TypeVisitor<Void, Void> schemaTypeChecker = new DefaultTypeVisitor<Void>() {
        @Override
        public Void visit(SchemaType type, Void p) throws TermException {
            throw new TermException();
        }
    };

    public List<String> errors = new LinkedList<String>();

    @Override
    protected void defaultAction(ASTElement node) throws ASTVisitException {
        if (state.schemaTypes.has(node) && !state.typeMap.has(node)) {
            final Type t = state.context.instantiate(state.schemaTypes.get(node));
            try{
                t.accept(schemaTypeChecker, null);
                state.typeMap.add(node, t);
            } catch (TermException e) {
                errors.add(node.toString() + ": the type of this node could not be inferred");
            }
        }

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }
}