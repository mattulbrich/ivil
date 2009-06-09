package de.uka.iti.pseudo.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.proof.Proof;

public class Main {
    
    public static Properties PROPERTIES = new Properties();

    private static StartupWindow startupWindow;
    
    public static final String PROPERTIES_FILENAME = "pseudo.properties";
    public static final String BASE_DIRECTORY = System.getProperty("pseudo.baseDir", ".");
    public static final String SYSTEM_DIRECTORY = System.getProperty("pseudo.sysDir", BASE_DIRECTORY + "/sys");
    
    public static final String ASSERTION_PROPERTY = "pseudo.enableAssertions";

    private static final List<ProofCenter> PROOF_CENTERS = new LinkedList<ProofCenter>();
    
    static {
        loadProperties();
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(getBoolean(ASSERTION_PROPERTY, true));
    }

    public static void main(String[] args) throws Exception {
        
        if(args.length == 0) {
            startupWindow = new StartupWindow();
            startupWindow.setVisible(true);
        } else {
            // XXX now, this is 'f course still alpha
            Parser fp = new Parser();

            String arg =  args[0];

            EnvironmentMaker em = new EnvironmentMaker(fp, new File(arg));
            Environment env = em.getEnvironment();
            env.dump();

            Proof proof = new Proof(em.getProblemTerm());
            ProofCenter proofCenter = new ProofCenter(proof, env);
            showProofCenter(proofCenter);
        }
    }

    public static void showProofCenter(ProofCenter proofCenter) {
        if(startupWindow != null) {
            startupWindow.dispose();
            startupWindow = null;
        }
        MainWindow main = proofCenter.getMainWindow();
        main.setVisible(true);
        PROOF_CENTERS.add(proofCenter);
    }
    
    public static void closeProofCenter(ProofCenter proofCenter) {
        assert PROOF_CENTERS.contains(proofCenter);
        
        MainWindow main = proofCenter.getMainWindow();
        main.dispose();
        PROOF_CENTERS.remove(proofCenter);
        
        if(PROOF_CENTERS.isEmpty())
            System.exit(0);
    }

    /**
     * check whether at least one open proof center has unsafed changes
     * @return true iff there are changes in one proof center
     */
    public static boolean proofCentersHaveChanges() {
        for (ProofCenter pc : PROOF_CENTERS) {
            if(pc.getProof().hasUnsafedChanges())
                return true;
        }
        return false;
    }

    /**
     * add all properties from the system and from a certain file to
     * the properties in {@link ProofCenter}.
     * 
     * Command line and system overwrite the file
     */
    private static void loadProperties() {
        
        File propFile = new File(BASE_DIRECTORY, PROPERTIES_FILENAME);
        if(propFile.canRead()) {
            try {
                PROPERTIES.load(new FileInputStream(propFile));
                System.err.println("read from " + propFile);
            } catch (IOException e) {
                System.err.println("Cannot read file " + propFile + " though I should ..., continue");
                e.printStackTrace();
            }
        }
        
        PROPERTIES.putAll(System.getProperties());
    }
    
    /**
     * get a boolean system property.
     * 
     * @param property
     *            name of the property to be retrieved
     * @param def
     *            the default value if the property is not set
     * @return the value to which the property is set or def, if not set
     */
    public static boolean getBoolean(String property, boolean def) {
        String v = PROPERTIES.getProperty(property);
        if(v != null) {
            return Boolean.valueOf(def);
        } else {
            return def;
        }
    }

}
