package de.uka.iti.pseudo.environment.boogie;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.VariableDeclaration;
import de.uka.iti.pseudo.parser.boogie.util.DefaultASTVisitor;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;

/**
 * Extracts type information out of an AST and creates sorts in the output
 * Environment.
 * 
 * 
 * @author timm.felden@felden.com
 * 
 */
public final class TypingPhase {

    final Map<String, Sort> sortMap = new HashMap<String, Sort>();
    final Map<ProcedureDeclaration, List<VariableDeclaration>> modifiable = new HashMap<ProcedureDeclaration, List<VariableDeclaration>>();

    void create(final EnvironmentCreationState state) throws TypeSystemException {
        try {
            // give each ASTElement a schema type
            (new DefaultASTVisitor() {
                @Override
                protected void defaultAction(ASTElement node) throws ASTVisitException {
                    state.schemaTypes.add(node, state.context.newSchemaType());
                    for (ASTElement e : node.getChildren())
                        e.visit(this);
                }
            }).visit(state.root);


            // add constraints
            new TypeMapBuilder(state);


            // give each ASTElement the inferred type
            (new DefaultASTVisitor() {
                @Override
                protected void defaultAction(ASTElement node) throws ASTVisitException {
                    if (!state.typeMap.has(node)) {
                        Type t = state.context.instantiate(state.schemaTypes.get(node));
                        // in case of maps we have to turn the schema map into a
                        // usable type
                        if (state.mapDB.hasType(t))
                            t = state.mapDB.getType((TypeApplication) t, node, state);

                        state.typeMap.add(node, t);
                    }

                    for (ASTElement e : node.getChildren())
                        e.visit(this);
                }
            }).visit(state.root);
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
