package de.uka.iti.pseudo.parser.boogie.environment;

import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.ast.CompilationUnit;

/**
 * Objects of this class are used to hold state while creating an Environment
 * that can be used by the proofer. Stages of creation are separated into
 * different functions to allow for better testing and for easier implementation
 * of new features. If you are only interested in converting a CompilationUnit
 * into an ivil Environment, use make()
 * 
 * @author timm.felden@felden.com
 * 
 */
public final class EnvironmentCreationState {

    private final CompilationUnit root;

    private final Scope globalScope = new Scope(null, null);
    private final Decoration<Scope> scopeMap = new Decoration<Scope>();

    // DEBUG
    private final List<Decoration<?>> allDecorations = new LinkedList<Decoration<?>>();

    public EnvironmentCreationState(CompilationUnit root) {
        this.root = root;

        allDecorations.add(scopeMap);
    }

    public void createNamespaces() throws EnvironmentCreationException {

        // create scope annotation for ast nodes to be able to create all
        // namespaces properly
        try {
            new ScopeBuilder(root, globalScope, scopeMap);
        } catch (ASTVisitException e) {
            throw new EnvironmentCreationException("Namespacecreation failed because of " + e.toString());
        }

        // create maps for all five namespaces, which are scope sensitive to be
        // able to resolve names to astelements and later to objects

        // ASTVisitor debug = new DebugVisitor(allDecorations);
        // try {
        // debug.visit(root);
        // } catch (ASTVisitException e) {
        // e.printStackTrace();
        // }
    }

    public void createTypesystem() throws EnvironmentCreationException {
        // create type table and check restrictions on type declarations and
        // usage

        // create a mapping from table types to ivil types
    }

    public Environment make(){
        try{
            throw new EnvironmentCreationException("make is not yet implemented");
            
        } catch(EnvironmentCreationException e) {
            throw new UnsupportedOperationException(
                    "An unexpected exception was thrown while making the environment.\n"
                            + "Please tell the developers how you got here.", e);
        }
    }
}
