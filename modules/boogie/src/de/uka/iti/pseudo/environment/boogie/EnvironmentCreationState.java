package de.uka.iti.pseudo.environment.boogie;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.creation.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.ParseException;
import de.uka.iti.pseudo.parser.boogie.ast.CompilationUnit;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;

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
 * @note most translated names have prefixes to ensure, that there are no
 *       collisions with system names, such as $eq and so on
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
    final Decoration<Type> ivilTypeMap = new Decoration<Type>();

    Environment env;

    // Phase 1: namespace and scope creation
    NamingPhase names = null;

    // Phase 2: type decoration and sort creation
    TypingPhase types = null;

    // Phase 3: translation of semantic constructs into ivil environment
    TranslationPhase translation = null;

    public EnvironmentCreationState(CompilationUnit root) {
        this.root = root;

        // load sys/boogie.p
        File file = new File("sys/boogie.p");
        EnvironmentMaker em = null;
        try {
            em = new EnvironmentMaker(new Parser(), file);

        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (de.uka.iti.pseudo.parser.ParseException e1) {
            e1.printStackTrace();
        } catch (de.uka.iti.pseudo.parser.ASTVisitException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (null == em)
            return;

        em.getEnvironment().setFixed();

        // create the environment where things from bpl file will be stored
        try {
            env = new Environment(root.getURL().toString(), em.getEnvironment());
        } catch (EnvironmentException e) {
            e.printStackTrace();
            assert false;
        }
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

    public void createEnvironment() throws ParseException {
        if (null == types)
            createTypesystem();

        if (null != translation)
            return;
        else
            translation = new TranslationPhase();

        translation.create(this);
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

        System.out.println("directly used type names:");
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
        allDecorations.add(ivilTypeMap);

        ASTVisitor debug = new DebugVisitor(allDecorations);
        try {
            debug.visit(root);
        } catch (ASTVisitException e) {
            e.printStackTrace();
        }

        env.dump();

        return false;
    }

    public Environment make() throws ParseException {
        try {
            createNamespaces();

            createTypesystem();

        } catch (EnvironmentCreationException e) {
            printDebugInformation();
            throw new UnsupportedOperationException(
                    "An unexpected exception was thrown while making the environment.\n"
                            + "Please tell the developers how you got here.", e);

        } catch (RuntimeException e) {
            printDebugInformation();
            throw e;

        }

        try {
            createEnvironment();

        } catch (RuntimeException e) {
            e.printStackTrace();

            // this should not happen, so print detailed information
            printDebugInformation();

            throw e;
        } finally {
            printDebugInformation();
        }

        return env;
    }

    public Term getProblem() throws ParseException {
        if (null == translation)
            createEnvironment();

        return translation.getProblem();
    }
}
