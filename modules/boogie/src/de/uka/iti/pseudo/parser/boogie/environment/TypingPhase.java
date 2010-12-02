package de.uka.iti.pseudo.parser.boogie.environment;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;

/**
 * Extracts type information out of an AST and creates sorts in the output
 * Environment.
 * 
 * 
 * @author timm.felden@felden.com
 * 
 */
public final class TypingPhase {

    void create(EnvironmentCreationState state) throws TypeSystemException {
        try {
            new TypeMapBuilder(state);
        } catch (ASTVisitException e) {

            // this exception is expected
            throw new TypeSystemException("TypeMap creation failed because of " + e.toString());

        }

        // make sure we did not forget something
        assert state.scopeMap.size() == state.typeMap.size() || state.printDebugInformation() : "found "
                + (state.scopeMap.size() - state.typeMap.size()) + " untyped ASTElements";

        new TypeChecker(state);

        // remove duplicates @note: this is maybe unneded, as it could be
        // integrated to ivil type creation

        // create a mapping from table types to ivil types
    }

}
