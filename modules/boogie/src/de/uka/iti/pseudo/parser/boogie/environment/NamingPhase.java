package de.uka.iti.pseudo.parser.boogie.environment;

import java.util.HashMap;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;

/**
 * Extracts naming and scoping information out of an AST.
 * 
 * @author timm.felden@felden.com
 * 
 */
public final class NamingPhase {
    // namespaces are used to map names to ASTElements, to allow for access of
    // decorations by name and context
    final HashMap<String, ASTElement> functionSpace = new HashMap<String, ASTElement>();
    final HashMap<String, ProcedureDeclaration> procedureSpace = new HashMap<String, ProcedureDeclaration>();
    final HashMap<String, ASTElement> attributeSpace = new HashMap<String, ASTElement>();
    // contains identifiers that can start a named type, such as bool, int, S(if
    // S is defined somewhere), ...
    final HashMap<String, ASTElement> typeSpace = new HashMap<String, ASTElement>();
    // as type parameters can shadow types, we have to put them into their own
    // space
    final HashMap<Pair<String, Scope>, ASTElement> typeParameterSpace = new HashMap<Pair<String, Scope>, ASTElement>();

    // variable names are scope sensitive
    final HashMap<Pair<String, Scope>, ASTElement> variableSpace = new HashMap<Pair<String, Scope>, ASTElement>();

    // not directly a namespace, but very handy for goto usage; maps names and
    // procedure bodies to Labelstatements
    final HashMap<Pair<String, Scope>, ASTElement> labelSpace = new HashMap<Pair<String, Scope>, ASTElement>();

    void create(EnvironmentCreationState state) throws EnvironmentCreationException, TypeSystemException {
        try {
            // create scope annotation for ast nodes to be able to create all
            // namespaces properly
            new ScopeBuilder(state.root, state.globalScope, state.scopeMap);

        } catch (ASTVisitException e) {
            throw new EnvironmentCreationException("Scope creation failed because of " + e.toString());
        }

        try {
            // create maps for all five namespaces, which are scope sensitive to
            // be able to resolve names to astelements and later to objects
            new NamespaceBuilder(state);

        } catch (ASTVisitException e) {
            throw new TypeSystemException("Namespace creation failed because of " + e.toString());
        }
    }
}
