package de.uka.iti.ivil.jbc.environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * The concrete proof obligation holds most of the state needed in order to
 * create the proof obligation. In general it should be no problem to process
 * many proof obligations in parallel, but a single concrete proof obligation
 * should be handled sequentially.
 * 
 * @author timm.felden@felden.com
 */
final public class ConcreteProofObligation<A> extends ProofObligation {

    /**
     * content of this proof obligation
     */
    public StringBuilder content = new StringBuilder();

    /**
     * The problem associated with this ProofObligation
     */
    public String problem = null;

    /**
     * The name resolver.
     */
    public final NameResolver resolver = new NameResolver(this);

    /**
     * A set of used functions. Only functions that have irregular names will be
     * stored here in order to avoid duplicate definitions. It is the duty of
     * the user to avoid clashes with regular function names, such as type names
     * or system names. Users of this mechanism should ensure that they don't
     * create typing issues if the reuse existing names.
     * 
     * <ul>
     * <li>FLOAT_ is used for float literals
     * <li>R_name_type is used for register names.
     * <li>F_name_type is used for fields
     * <li>T_name is used for types.
     * <li>TA_name is used for type arguments (which are bound variables and
     * therefore will be renamed later)
     * <li>$stack_levelTYPE is used for stack names.
     * <li>CA_name_type is used for call arguments.
     * <li>CResult_type is used for call results.
     * <li>SPEC_* can be used freely by the specification language.
     * <li>$SPEC_* can be used freely by the specification language.
     * </ul>
     */
    private final HashSet<String> usedFunctionNames = new HashSet<String>();

    private final String name;

    /**
     * Exception which has been thrown, if a load error occurred.
     */
    private Throwable error = null;
    /**
     * List of warnings that occurred during construction.
     */
    private List<Exception> warnings = new LinkedList<Exception>();

    /**
     * The contract resolver of this node. It is to be set by the POG that
     * created the node and must not be null.
     */
    private final ContractResolver contractResolver;

    /**
     * {@link ProofObligationGenerator} specific data. This is useful to keep
     * information, which can be acquired during the single threaded potree
     * construction effectively, but is needed during the load phase, which can
     * be processed concurrently.
     */
    private A data;

    public ConcreteProofObligation(ProofObligation parent, String name, ContractResolver contractResolver) {
        super(parent);
        this.name = name;
        this.contractResolver = contractResolver;

        // the initial state for concrete POs is load
        setState(State.load);

        // any concrete PO needs to request java/lang/Object, because otherwise
        // the preamble would not form a legal .p file.
        try {
            resolver.requestClass("java/lang/Object");
        } catch (BytecodeCompilerException e) {
            throw new Error(
                    "The root of the type hierarchy could not be found, i.e. no class file for java/lang/Object was found!",
                    e);
        }

        // it is mandatory, that a constant for 0.0 is always present
        getFloatLiteral(0.0);

        assert parent != null : "Concrete PO is not allowed to be root.";
    }

    @Override
    protected void updateState() {
        assert false : "concrete obligations musst not have children!";
    }

    @Override
    public String getName() {
        return name;
    }

    public void setError(Throwable e) {
        assert null == error : "this PO already failed!";
        error = e;
    }

    public boolean hasError() {
        return null != error;
    }

    public Throwable getError() {
        assert hasError() : "no error occured!";
        return error;
    }

    public void addWarning(Exception e) {
        warnings.add(e);
    }

    public List<Exception> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    public void save(File tmp) throws BytecodeCompilerException {
        assert !problem.equals("false") && getState() != State.loadFailed : "this proof obligation can not be saved";

        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter(tmp.getAbsolutePath()));
            out.write(Environment.getPreamble());
            out.write("\n\n");
            out.write("(* the proof obligation starts here: *)");
            out.write("\n\n");
            out.write(content.toString());
            out.write("\nproblem ");
            out.write(problem);
            out.close();

        } catch (IOException e) {
            throw new BytecodeCompilerException("saving " + this + " failed", e);
        }
    }

    public ContractResolver getContractResolver() {
        return contractResolver;
    }

    /**
     * General interface to request the presence of a user defined function
     * symbol (@see usedFunctionNames).
     * 
     * @param name
     *            the full name of the function symbol including the prefix
     * @param type
     *            the type of the function symbol, which is only used, if the
     *            symbol is not yet present
     * @param modifiers
     *            modifiers of the symbol such as unique or assignable; can be
     *            null to indicate that no modifier is used
     */
    public void requestFunction(String name, String type, String modifiers) {
        if (usedFunctionNames.contains(name))
            return;

        if (null == type || "null".equals(type))
            throw new BytecodeCompilerError("Sort null is illegal!");

        content.append("function ").append(type).append(" ").append(name);
        if (null != modifiers)
            content.append(" ").append(modifiers);
        content.append("\n");
        usedFunctionNames.add(name);
    }

    /**
     * Like requestFunction, but ensures that the name has not yet been
     * requested.
     * 
     * @return the name that has actually been used to create the function
     *         symbol.
     */
    public String requestFreshFunction(String name, String type, String modifiers) {
        if (usedFunctionNames.contains(name)) {
            int i = 1;
            while (usedFunctionNames.contains(name + i))
                i++;
            name = name + i;
        }
        requestFunction(name, type, modifiers);
        return name;
    }

    /**
     * turns a floating point value into a literal. These literals can be
     * unique, as the rounding to actual floats already occured.
     */
    strictfp public String getFloatLiteral(double f) {
        String literal = "FLOAT_" + Double.toString(f).replace('+', 'p').replace('-', 'm').replace('.', '_');
        requestFunction(literal, "float", "unique");
        return literal;
    }

    public A getData() {
        return data;
    }

    public void setData(A data) {
        this.data = data;
    }

}
