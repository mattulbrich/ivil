/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.gui.editor.PFileEditor;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.serialisation.ProofExport;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.CommandLine;
import de.uka.iti.pseudo.util.settings.Settings;


/**
 * Main entry point for the GUI application.
 * 
 * Reads arguments from command line, but checks also for other resources where
 * properties may have been set.
 * 
 * <h2>Command line options</h2>
 * See method {@link #makeCommandLine()} for all command line options or run the
 * program using the <code>-help</code> option.
 * 
 */

public class Main {
    
    private static final String CMDLINE_CONFIG = "-config";

    private static final String CMDLINE_HELP = "-help";

    private static final String CMDLINE_EDIT = "-edit";

    private static Settings settings;

    private static StartupWindow startupWindow;
    
    public static final String PROPERTIES_FILE_KEY = "pseudo.settingsFile";
    public static final String BASE_DIRECTORY_KEY = "pseudo.baseDir";
    public static final String BASE_DIRECTORY;
    public static final String SYSTEM_DIRECTORY_KEY = "pseudo.sysDir";
    
    public static final String ASSERTION_PROPERTY = "pseudo.enableAssertions";

    private static final List<ProofCenter> PROOF_CENTERS = new LinkedList<ProofCenter>();
    private static final List<PFileEditor> EDITORS = new LinkedList<PFileEditor>();
    
    public static final int PROBLEM_FILE = 0;
    public static final int PROOF_FILE = 1;
    private static JFileChooser fileChooser[] = new JFileChooser[2];
    
    
    /*
     * - setup the settings from default resource, file and command line.
     * - set assertion state accordingly
     * - set directories accordingly 
     */
    static {
        loadProperties();
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(settings.getBoolean(ASSERTION_PROPERTY, true));
        
        BASE_DIRECTORY = settings.getProperty(BASE_DIRECTORY_KEY, ".");
    }

    
    public static void main(String[] args) throws Exception {
        
        CommandLine commandLine = makeCommandLine();
        commandLine.parse(args);
        
        if(commandLine.isSet(CMDLINE_HELP)) {
            commandLine.printUsage(System.out);
            System.exit(0);
        }
        
        List<String> fileArguments = commandLine.getArguments();
        
        if(fileArguments.isEmpty()) {
            startupWindow = new StartupWindow();
            startupWindow.setVisible(true);
        } else {
            File file = new File(fileArguments.get(0));
            
            if(commandLine.isSet(CMDLINE_EDIT)) {
                openEditor(file);
            }
            else {
                openProver(file);
            }
        }
    }
    

    private static CommandLine makeCommandLine() {
        CommandLine cl = new CommandLine();
        cl.addOption(CMDLINE_HELP, null, "Print usage");
        cl.addOption(CMDLINE_EDIT, null, "Edit the file instead of opening a prover frame");
        cl.addOption(CMDLINE_CONFIG, "file", "Read configuration from a file overwriting defaults.");
        return cl;
    }


    public static void openEditor(File file) throws IOException {
        PFileEditor editor = new PFileEditor(file);
        editor.setSize(600, 800);
        showFileEditor(editor);
    }
    
    private static void showFileEditor(PFileEditor editor) {
        if(startupWindow != null) {
            startupWindow.dispose();
            startupWindow = null;
        }
        editor.setVisible(true);
        EDITORS.add(editor);
    }
    
    public static void closeFileEditor(PFileEditor editor) {
        assert EDITORS.contains(editor);
        
        editor.dispose();
        EDITORS.remove(editor);
        
        if(PROOF_CENTERS.isEmpty() && EDITORS.isEmpty())
            System.exit(0);
    }

    public static ProofCenter openProverFromURL(URL url)
            throws FileNotFoundException, ParseException, ASTVisitException,
            TermException, IOException, StrategyException, EnvironmentException {

        File tempFile = File.createTempFile("ivil", ".p");
        tempFile.deleteOnExit();
        OutputStream os = null;
        InputStream is = null;
        try {
            os = new FileOutputStream(tempFile);
            is = url.openStream();
            byte buffer[] = new byte[1024];
            int read;
            while ((read = is.read(buffer)) >= 0) {
                os.write(buffer, 0, read);
            }
        } finally {
            if (os != null)
                os.close();
            if (is != null)
                is.close();
        }

        return openProver(tempFile);
    }

    public static ProofCenter openProver(File file)
            throws FileNotFoundException, ParseException, ASTVisitException,
            TermException, IOException, StrategyException, EnvironmentException {
        Parser fp = new Parser();

        EnvironmentMaker em = new EnvironmentMaker(fp, file);
        Environment env = em.getEnvironment();
        Term problemTerm = em.getProblemTerm();

        if (problemTerm == null)
            throw new EnvironmentException(
                    "Cannot load an environment without problem");

        Proof proof = new Proof(problemTerm);
        ProofCenter proofCenter = new ProofCenter(proof, env);
        showProofCenter(proofCenter);
        return proofCenter;
    }
    
    private static void showProofCenter(ProofCenter proofCenter) {
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



    public static JFileChooser makeFileChooser(int index) {

        assert index == PROBLEM_FILE || index == PROOF_FILE;

        if (fileChooser[index] == null) {
            fileChooser[index] = new JFileChooser(".");

            if (index == PROOF_FILE)
                for (ProofExport export : ServiceLoader.load(ProofExport.class)) {
                    fileChooser[index]
                            .addChoosableFileFilter(new ExporterFileFilter(
                                    export));
                }
            else

                fileChooser[index].setFileFilter(new FileNameExtensionFilter(
                        "ivil files", "p"));
        }
        return fileChooser[index];
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
            settings.loadKeyAsFile(PROPERTIES_FILE_KEY);
            settings.putAll(System.getProperties());
        } catch (IOException e) {
            System.err.println("Cannot read properties file, continuing anyway ...");
            e.printStackTrace();
        }
    }

}