package de.uka.iti.ivil.jbc.environment;

import de.uka.iti.ivil.jbc.util.ClassType;
import de.uka.iti.ivil.jbc.util.MethodName;

/**
 * A contract resolver is a class, that can translate method calls to contracts.
 * These resolvers can be used to translate special functions and regular
 * function calls. <br>
 * 
 * For example if one wants to add an assert statement, one could create a stub
 * class file with a method e.g. "void de.uka.ivil.spec.assert(boolean phi)"
 * which asserts the argument to be true. In that case the result of
 * getCallContracts would be
 * "new String[][] = new String[][]{new String[]{"assert CA_phi_int"}}"
 * 
 * The type T is passed to ConcreteProofObligiation arguments.
 * 
 * @author timm.felden@felden.com
 */
public interface ContractResolver {

    /**
     * Resolves the contracts to a method name. The this reference is always
     * called "CA_this_ref" if present.
     * 
     * @note getCallContract must be thread safe, if the same resolver is used
     *       in multiple concrete proof obligations
     * 
     * @param callee
     *            the name of the called method
     * 
     * @param argumentTerms
     *            an array of terms containing the state of the argument
     *            variables, indices correspond to the way indices are treated
     *            in the method type, i.e. no JVM category based index confusion
     *            happens here
     * 
     * @param resultName
     *            the name of the assignable function that has to be used as
     *            result
     * 
     * @return the requested method contract, where each statement is put into
     *         one string; an empty array is considered to be an unsuccessful
     *         attempt of contract resolution; if a method does indeed not
     *         require a contract, return the single statement skip.
     * 
     * @throws BytecodeCompilerException
     *             can be thrown in case of errors such as missing or malformed
     *             files
     */
    public String[] getCallContracts(ConcreteProofObligation<?> po, MethodName callee, String[] argumentTerms,
            String resultName) throws BytecodeCompilerException;

    /**
     * Delegates a call to monitorente/monitorexit to the POG. This is done so,
     * because the bytecode translator itself does not have a threading model.
     * 
     * @param target
     *            a string containing a term that is the object belonging to the
     *            monitor
     * @param po
     *            the target proof obligation
     * @param pc
     *            the corrent program counter
     * @param isEnter
     *            true iff the current opcode is monitorenter
     * @return a list of statements, that represents the translation of the
     *         operation
     * 
     * @throws BytecodeCompilerException
     *             can be thrown to signal errors
     */
    public String[] getMonitorContract(final String target, ConcreteProofObligation<?> po, int pc, boolean isEnter)
            throws BytecodeCompilerException;

    /**
     * Resolve a contract for a special function name. The name musst contain
     * all information needed in order to build the contract.
     * 
     * Special functions are guaranteed to be static, void and to take no
     * arguments.
     * 
     * @param code
     *            the code supplied with the special function
     * 
     * @param localNames
     *            an array of valid local variable names
     * 
     * @param localTranslatedNames
     *            an array of translated names for local variables, that can be
     *            used in terms
     * 
     * @param currentPC
     *            the pc value of the invokeStatic instruction
     * 
     * @return the contract to be placed where the special function occured
     * 
     *         TODO maybe localNames should be a map?
     */
    public String[] getSpecialContract(ConcreteProofObligation<?> po, String code, String[] localNames,
            String[] localTranslatedNames, int currentPC) throws BytecodeCompilerException;

    /**
     * This query tells the translator, whether the special function is a loop
     * invariant or not. This is required, as loop invariants can not be placed
     * where they occur in the code but have to be moved behind the next
     * (loop)jump target to be at the beginning of an intermediate code loop and
     * not just before it.
     * 
     * @return true iff you want the last line of the contract to be moved
     */
    public boolean endsWithLoopInvariantStatement(String code);

    /**
     * After loading a class, its invariants are requested and added to the
     * proof obligation. If you don't want to use the built in invariant
     * mechanism, return true.
     * 
     * @note you may omit the typeof(o,T) and the quantification over o in case
     *       of invariants over static properties of the class. In that case,
     *       you have to use $static instead of o
     * 
     * @param type
     *            the type name of the invariants class
     * 
     * @return an expression that may use the schema variables %h and %o, which
     *         has to be valid iff the invariant of an object %o of type
     *         typeName is fulfilled on a heap %h.
     * 
     * @throws BytecodeCompilerException
     *             may be thrown to signal arbitrary expectable errors
     */
    public String getInvariant(ConcreteProofObligation<?> po, ClassType type)
            throws BytecodeCompilerException;
}
