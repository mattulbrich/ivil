package de.uka.iti.pseudo.parser.boogie.environment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.ParseException;
import de.uka.iti.pseudo.parser.boogie.ast.CompilationUnit;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureBody;

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

    // namespaces are used to map names to ASTElements, to allow for access of
    // decorations by name and context
    final HashMap<String, ASTElement> functionSpace = new HashMap<String, ASTElement>();
    final HashMap<String, ASTElement> procedureSpace = new HashMap<String, ASTElement>();
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
    final HashMap<Pair<String, ProcedureBody>, ASTElement> labelSpace = new HashMap<Pair<String, ProcedureBody>, ASTElement>();

    public EnvironmentCreationState(CompilationUnit root) {
        this.root = root;
    }

    public void createNamespaces() throws EnvironmentCreationException, ParseException {

        try {
            // create scope annotation for ast nodes to be able to create all
            // namespaces properly
            new ScopeBuilder(root, globalScope, scopeMap);

        } catch (ASTVisitException e) {
            throw new EnvironmentCreationException("Scope creation failed because of " + e.toString());
        }

        try {
            // create maps for all five namespaces, which are scope sensitive to
            // be able to resolve names to astelements and later to objects
            new NamespaceBuilder(this);

        } catch (ASTVisitException e) {
            throw new TypeSystemException("Namespace creation failed because of " + e.toString());
        }
    }

    public void createTypesystem() throws EnvironmentCreationException, TypeSystemException {
        try {
            new TypeMapBuilder(this);
        } catch (ASTVisitException e) {

            // this exception is expected
            throw new TypeSystemException("TypeMap creation failed because of " + e.toString());

        }
        
        //make sure we did not forget something
        assert scopeMap.size() == typeMap.size() || printDebugInformation();

        // new TypeChecker(this);

        // remove duplicates

        // create a mapping from table types to ivil types
    }

    /**
     * Prints debug information to System.out
     * 
     * @return false to enable printing of debuginformation on failing
     *         assertions via "|| printDebugInformation()"
     */
    public boolean printDebugInformation() {
        
        //Print namespace information
        System.out.println("function names:");
        for (String n : functionSpace.keySet()) {
            System.out.println("\t" + n);
        }
        System.out.println("");

        System.out.println("procedure names:");
        for (String n : procedureSpace.keySet()) {
            System.out.println("\t" + n);
        }
        System.out.println("");

        System.out.println("directly usable type names:");
        for (String n : typeSpace.keySet()) {
            System.out.println("\t" + n);
        }
        System.out.println("");

        System.out.println("type parameters:");
        for (Pair<String, Scope> n : typeParameterSpace.keySet()) {
            System.out.println("\t" + n.first + "\t\t" + n.second.toString());
        }
        System.out.println("");

        System.out.println("seen types:");
        for (UniversalType t : typeMap.valueSet()) {
            if (t != null)
                System.out.println("\t" + t.name);
        }
        System.out.println("");

        System.out.println("variable and constant declarations:");
        for (Pair<String, Scope> n : variableSpace.keySet()) {
            System.out.println("\t" + n.first + "\t\t" + n.second.toString());
        }
        System.out.println("");

        System.out.println("explicit labels:");
        for (Pair<String, ProcedureBody> n : labelSpace.keySet()) {
            System.out.println("\t" + n.first + "\t\tinside body " + n.second.getLocation());
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
        try{
            createNamespaces();
            
            createTypesystem();

            // printDebugInformation();

            return null;

        } catch(EnvironmentCreationException e) {
            throw new UnsupportedOperationException(
                    "An unexpected exception was thrown while making the environment.\n"
                            + "Please tell the developers how you got here.", e);
        }
    }
}
