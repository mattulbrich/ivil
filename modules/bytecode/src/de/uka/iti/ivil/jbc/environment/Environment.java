/**
 *
 */
package de.uka.iti.ivil.jbc.environment;

import java.io.File;
import java.util.HashSet;

import de.uka.iti.ivil.jbc.environment.ProofObligation.State;

/**
 * This class represents an environment that is used to hold all informations
 * needed to create and work with proof obligations.
 *
 * @author timm.felden@felden.com
 */
public final class Environment {

    /**
     * System directory, where rt.jar and other system data is found.
     */
    public static final String SYS_DIR = ".";

    /**
     * the root of the PO hierarchy.
     */
    private final ProofObligation root;

    /**
     * The common environment is used to reduce memory footprint and load time
     * for proof obligation. It contains the environment required by all java
     * bytecode PO-Generators.
     */
    private static final String jbcPreamble = "preamble";

    // load jbcPreamble
//    static {
//
//        String preamble;
//        try {
//            final String path = "jbc_default_preamble.p";
//
//            StringBuffer fileData = new StringBuffer(1000);
//            BufferedReader reader;
//            reader = new BufferedReader(new FileReader(path));
//            char[] buf = new char[1024];
//            int numRead = 0;
//            while ((numRead = reader.read(buf)) != -1) {
//                fileData.append(buf, 0, numRead);
//            }
//            reader.close();
//            preamble = fileData.toString();
//        } catch (Exception e) {
//            e.printStackTrace();
//            preamble = "failed to open preamble";
//        }
//        jbcPreamble = preamble;
//    }

    public Environment(String path) throws BytecodeCompilerException {
        root = new DirectoryProofObligation(path);

        // resolve PO-generators
        createPOTree(path, root);
    }

    private void createPOTree(String path, ProofObligation parent) throws BytecodeCompilerException {
        File f = new File(path);
        if (f.isDirectory()) {
            parent = new DirectoryProofObligation(path, parent);
            for (String s : f.list()) {
                createPOTree(path + "/" + s, parent);
            }

        } else {
            // create matching PO-generator
            ProofObligationGenerator generator;
            try {
                generator = POGeneratorFactory.getGeneratorByPath(path);
            } catch (Exception e) {
                e.printStackTrace();
                parent.setState(State.loadFailed);
                return;
            }
            if (null == generator) {
                // unsupported file type;
                return;
            }
            try {
                generator.load(this, path, parent);
            } catch (Exception e) {
                throw new BytecodeCompilerException("The proof obligation generator " + generator + " failed to load file: "
                        + path, e);
            }
        }

    }

    public ProofObligation getRoot() {
        return root;
    }

    public static String getPreamble() {
        return jbcPreamble;
    }

    public HashSet<ConcreteProofObligation<?>> getConcreteProofObligations() {
        assert root != null : "load a problem first!";
        HashSet<ConcreteProofObligation<?>> obligations = new HashSet<ConcreteProofObligation<?>>();
        collectConcreteProofObligations(root, obligations);
        return obligations;
    }

    private void collectConcreteProofObligations(ProofObligation root, HashSet<ConcreteProofObligation<?>> obligations) {
        if (root instanceof ConcreteProofObligation) {
            obligations.add((ConcreteProofObligation<?>) root);
        } else {
            for (ProofObligation p : root.children) {
                collectConcreteProofObligations(p, obligations);
            }
        }
    }
}
