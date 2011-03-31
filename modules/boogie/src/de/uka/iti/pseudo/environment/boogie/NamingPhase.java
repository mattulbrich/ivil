package de.uka.iti.pseudo.environment.boogie;

import java.util.HashMap;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.VariableDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.expression.VariableUsageExpression;

/**
 * Extracts naming and scoping information out of an AST.
 * 
 * @author timm.felden@felden.com
 * 
 */
public final class NamingPhase {
    EnvironmentCreationState state;

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
    final HashMap<Pair<String, Scope>, VariableDeclaration> variableSpace = new HashMap<Pair<String, Scope>, VariableDeclaration>();

    // not directly a namespace, but very handy for goto usage; maps names and
    // procedure bodies to Labelstatements
    final HashMap<Pair<String, Scope>, ASTElement> labelSpace = new HashMap<Pair<String, Scope>, ASTElement>();

    // store information where constants are used to allow for treatment of
    // complete in extends specifications
    final HashMap<String, List<VariableDeclaration>> constantUsage = new HashMap<String, List<VariableDeclaration>>();

    void create(EnvironmentCreationState state) throws EnvironmentCreationException, TypeSystemException {
        assert null == this.state : "already created names!";
        this.state = state;

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

    /**
     * Finds declaration of the corresponding variable. This is guaranteed to
     * work after the naming phase only.
     * 
     * @param node
     * 
     * @return the declaration of the used variable
     */
    public VariableDeclaration findVariable(VariableUsageExpression node) {
        assert null != state;
        return findVariable(node.getName(), node);
    }

    /**
     * Finds declaration of the corresponding variable. This is guaranteed to
     * work after the naming phase only.
     * 
     * @param name
     *            the name of the variable to be searched
     * 
     * @param node
     *            the node from where the variable is searched; this is used to
     *            determine the initial scope to be searched
     * 
     * @return the declaration of the used variable
     */
    public VariableDeclaration findVariable(String name, ASTElement node) {
        Scope scope = state.scopeMap.get(node);
        Pair<String, Scope> key;
        VariableDeclaration rval;

        while (scope != null) {
            key = new Pair<String, Scope>(name, scope);
            rval = state.names.variableSpace.get(key);
            if (null != rval)
                return rval;

            scope = scope.parent;
        }
        return null;
    }
}
