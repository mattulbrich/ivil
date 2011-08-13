package de.uka.iti.pseudo.environment.boogie;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.VariableDeclaration;

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
            // add constraints
            new TypeMapBuilder(state);

        } catch (ASTVisitException e) {

            // this exception is expected
            throw new TypeSystemException("TypeMap creation failed because of " + e.toString(), e);
        }

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

        // ensure correctness of modifies
        new ModifiesChecker(state);
    }

}
