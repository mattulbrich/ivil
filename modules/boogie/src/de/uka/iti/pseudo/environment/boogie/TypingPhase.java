package de.uka.iti.pseudo.environment.boogie;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.VariableDeclaration;
import de.uka.iti.pseudo.parser.boogie.util.DefaultASTVisitor;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.Type;

/**
 * Extracts type information out of an AST and creates sorts in the output
 * Environment.
 * 
 * 
 * @author timm.felden@felden.com
 * 
 */
public final class TypingPhase {

    static class TypeInstantiationVisitor extends DefaultASTVisitor {

        public TypeInstantiationVisitor(final EnvironmentCreationState state) {
            this.state = state;
        }

        final private EnvironmentCreationState state;

        public List<String> errors = new LinkedList<String>();

        @Override
        protected void defaultAction(ASTElement node) throws ASTVisitException {
            if (state.schemaTypes.has(node) && !state.typeMap.has(node)) {
                final Type t = state.context.instantiate(state.schemaTypes.get(node));
                if (t instanceof SchemaType)
                    errors.add(node.toString() + ": the type of this node could not be inferred");
                state.typeMap.add(node, t);
            }

            for (ASTElement e : node.getChildren())
                e.visit(this);
        }
    }

    final Map<String, Sort> sortMap = new HashMap<String, Sort>();
    final Map<ProcedureDeclaration, List<VariableDeclaration>> modifiable = new HashMap<ProcedureDeclaration, List<VariableDeclaration>>();

    void create(final EnvironmentCreationState state) throws TypeSystemException {
        try {
            // add constraints
            new TypeMapBuilder(state);

        } catch (ASTVisitException e) {

            // this exception is expected
            throw new TypeSystemException("TypeMap creation failed because of " + e.toString(), e);
        }

        // always synthesize types to help the user in case of typing errors
        try {

            // give each ASTElement the inferred type
            TypeInstantiationVisitor v = new TypeInstantiationVisitor(state);
            v.visit(state.root);
            if (v.errors.size() != 0) {
                StringBuilder sb = new StringBuilder("Type errors occured:\n");
                for (String s : v.errors)
                    sb.append(s).append("\n");
                throw new TypeSystemException(sb.toString());
            }
        } catch (ASTVisitException e) {

            // this exception is expected
            throw new TypeSystemException("TypeMap creation failed because of " + e.toString(), e);
        }

        // make sure we did not forget something
        assert state.scopeMap.size() == state.typeMap.size() || state.printDebugInformation() : "found "
                + (state.scopeMap.size() - state.typeMap.size()) + " untyped ASTElements";

        // TODO this should not be needed
        // new TypeChecker(state);

        // ensure correctness of modifies
        new ModifiesChecker(state);
    }

}
