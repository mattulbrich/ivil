package de.uka.iti.ivil.jbc.environment;

/**
 * The ProofObligation Generator is the driver of the ProofObligation tree
 * creation in an environment. A generator specifies a file type and implements
 * a loader for this type.
 * 
 * ProofObligation generators are instantiated over the factory method provided
 * in this class.
 * 
 * TODO improve doc
 * 
 * @author timm.felden@felden.com
 */
abstract public class ProofObligationGenerator {

    /**
     * ProofObligationGeneror subclasses need public default constructors in
     * order to work with the POGeneratorFactory. It is recommended to register
     * the class there.
     */
    public ProofObligationGenerator() {
    }

    /**
     * Load has to create all ProofObligations specified by the file at path. It
     * is recommended, but not required, to create the ProofObligation objects
     * in the invoking thread and to load the content of the ProofObligations in
     * another thread.
     * 
     * It is required to create the children of parent in the invoking thread.
     * 
     * @param path
     *            to the file to be loaded
     * @param parent
     *            PO which is parent of all created POs, usually a directory
     */
    abstract public void load(final Environment env, final String path, ProofObligation parent)
            throws BytecodeCompilerException;
}
