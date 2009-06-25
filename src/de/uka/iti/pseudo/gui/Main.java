package de.uka.iti.pseudo.gui;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.gui.editor.PFileEditor;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.util.settings.Settings;

public class Main {
    
    private static Settings settings;

    private static StartupWindow startupWindow;
    
    public static final String PROPERTIES_FILENAME = "pseudo.properties";
    public static final String BASE_DIRECTORY_KEY = "pseudo.baseDir";
    public static final String BASE_DIRECTORY;
    public static final String SYSTEM_DIRECTORY_KEY = "pseudo.sysDir";
    public static String SYSTEM_DIRECTORY;
    
    public static final String ASSERTION_PROPERTY = "pseudo.enableAssertions";

    private static final List<ProofCenter> PROOF_CENTERS = new LinkedList<ProofCenter>();
    private static final List<PFileEditor> EDITORS = new LinkedList<PFileEditor>();
    
    static {
        loadProperties();
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(getBoolean(ASSERTION_PROPERTY, true));
        
        BASE_DIRECTORY = settings.getProperty(BASE_DIRECTORY_KEY);
        try {
            SYSTEM_DIRECTORY = settings.getProperty(SYSTEM_DIRECTORY_KEY);
        } catch (NoSuchElementException e) {
            SYSTEM_DIRECTORY = BASE_DIRECTORY + File.separator + "sys";
        }
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
    
    public static void showFileEditor(PFileEditor editor) {
        if(startupWindow != null) {
            startupWindow.dispose();
            startupWindow = null;
        }
        editor.setVisible(true);
        EDITORS.add(editor);
    }
    
    public static void closeProofCenter(ProofCenter proofCenter) {
        assert PROOF_CENTERS.contains(proofCenter);
        
        MainWindow main = proofCenter.getMainWindow();
        main.dispose();
        PROOF_CENTERS.remove(proofCenter);
        
        if(PROOF_CENTERS.isEmpty() && EDITORS.isEmpty())
            System.exit(0);
    }
    
    public static void closeFileEditor(PFileEditor editor) {
        assert EDITORS.contains(editor);
        
        editor.dispose();
        EDITORS.remove(editor);
        
        if(PROOF_CENTERS.isEmpty() && EDITORS.isEmpty())
            System.exit(0);
    }

    /**
     * check whether at least one open proof center or one editor
     * has unsaved changes
     * @return true iff there are changes in one window
     */
    public static boolean windowsHaveChanges() {
        for (ProofCenter pc : PROOF_CENTERS) {
            if(pc.getProof().hasUnsafedChanges())
                return true;
        }
        for (PFileEditor editor : EDITORS) {
            if(editor.hasUnsafedChanges())
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
        try {
            settings = Settings.getInstance();
            settings.loadFromSystemDirectory(BASE_DIRECTORY_KEY, PROPERTIES_FILENAME);
            settings.putAll(System.getProperties());
        } catch (IOException e) {
            System.err.println("Cannot read file " + BASE_DIRECTORY_KEY +
                    File.pathSeparator + PROPERTIES_FILENAME + ", continue");
            e.printStackTrace();
        }
    }
        
    public static boolean getBoolean(String property, boolean def) {
        return settings.getBoolean(property, def);
    }

    public static Font getFont(String property) {
        return settings.getFont(property);
    }
    
    public static Color getColor(String key) {
        return settings.getColor(key);
    }

    public double getDouble(String key) {
        return settings.getDouble(key);
    }

    public int getInteger(String key) throws NumberFormatException {
        return settings.getInteger(key);
    }

    public String getProperty(String key, String defaultValue) {
        return settings.getProperty(key, defaultValue);
    }

    public String getProperty(String key) {
        return settings.getProperty(key);
    }

    public String[] getStrings(String key) {
        return settings.getStrings(key);
    }

}
