package de.uka.iti.pseudo.parser.boogie.environment;

import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.ParseException;
import de.uka.iti.pseudo.parser.boogie.ast.CompilationUnit;

/**
 * Objects of this class are used to hold state while creating an Environment
 * that can be used by the proofer. Stages of creation are separated into
 * different functions to allow for better testing and for easier implementation
 * of new features. If you are only interested in converting a CompilationUnit
 * into an ivil Environment, use make().<br>
 * 
 * 
 * In order to understand the design behind the various builders, look at them
 * as functions with closures.
 * 
 * @author timm.felden@felden.com
 * 
 */
public final class EnvironmentCreationState {

    final CompilationUnit root;

    // scope information, that is used to determine, whether a name is visible
    // or not
    final Scope globalScope = new Scope(null, null);
    final Decoration<Scope> scopeMap = new Decoration<Scope>();

    // type information used for typechecking and lowering of expressions and
    // declarations
    final Decoration<UniversalType> typeMap = new Decoration<UniversalType>();

    // Phase 1: namespace and scope creation
    NamingPhase names = null;

    // Phase 2: type decoration and sort creation
    TypingPhase types = null;

    public EnvironmentCreationState(CompilationUnit root) {
        this.root = root;
    }

    public void createNamespaces() throws EnvironmentCreationException, ParseException {
        if (null != names)
            return;
        else
            names = new NamingPhase();

        names.create(this);
    }

    public void createTypesystem() throws ParseException {
        if (null == names)
            createNamespaces();

        if (null != types)
            return;
        else
            types = new TypingPhase();

        types.create(this);
    }

    /**
     * Prints debug information to System.out
     * 
     * @return false to enable printing of debuginformation on failing
     *         assertions via "|| printDebugInformation()"
     */
    public boolean printDebugInformation() {

        System.out.println("The tree contains " + root.getTreeSize() + " ASTElements\n");

        // Print namespace information
        System.out.println("function names:");
        for (String n : names.functionSpace.keySet()) {
            System.out.println("\t" + n);
        }
        System.out.println("");

        System.out.println("procedure names:");
        for (String n : names.procedureSpace.keySet()) {
            System.out.println("\t" + n);
        }
        System.out.println("");

        System.out.println("directly usable type names:");
        for (String n : names.typeSpace.keySet()) {
            System.out.println("\t" + n);
        }
        System.out.println("");

        System.out.println("type parameters:");
        for (Pair<String, Scope> n : names.typeParameterSpace.keySet()) {
            System.out.println("\t" + n.first + "\t\t" + n.second.toString());
        }
        System.out.println("");

        // System.out.println("seen types:");
        // for (UniversalType t : typeMap.valueSet()) {
        // if (t != null)
        // System.out.println("\t" + t.name);
        // }
        // System.out.println("");

        System.out.println("variable and constant declarations:");
        for (Pair<String, Scope> n : names.variableSpace.keySet()) {
            System.out.println("\t" + n.first + "\t\t" + n.second.toString());
        }
        System.out.println("");

        System.out.println("explicit labels:");
        for (Pair<String, Scope> n : names.labelSpace.keySet()) {
            System.out.println("\t" + n.first + "\t\tinside body scope " + n.second);
        }
        System.out.println("");

        // Print decorated AST
        List<Decoration<?>> allDecorations = new LinkedList<Decoration<?>>();

        allDecorations.add(scopeMap);
        allDecorations.add(typeMap);

        ASTVisitor debug = new DebugVisitor(allDecorations);
        try {
            debug.visit(root);
        } catch (ASTVisitException e) {
            e.printStackTrace();
        }

        return false;
    }

    public Environment make() throws ParseException {
        try {
            createNamespaces();

            createTypesystem();

            return null;

        } catch (EnvironmentCreationException e) {
            throw new UnsupportedOperationException(
                    "An unexpected exception was thrown while making the environment.\n"
                            + "Please tell the developers how you got here.", e);

        } catch (RuntimeException e) {

            // printDebugInformation();
            throw e;
        } finally {

            // printDebugInformation();
        }
    }
}
