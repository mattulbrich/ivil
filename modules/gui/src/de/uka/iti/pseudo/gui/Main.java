/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import nonnull.NonNull;
import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.creation.EnvironmentCreationService;
import de.uka.iti.pseudo.gui.editor.PFileEditor;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.serialisation.ProofExport;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.CommandLine;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.Pair;
import de.uka.iti.pseudo.util.Util;
import de.uka.iti.pseudo.util.settings.Settings;

/**
 * Main entry point for the GUI application.
 * 
 * Reads arguments from command line, but checks also for other resources where
 * properties may have been set.
 * 
 * <h2>Command line options</h2> See method {@link #makeCommandLine()} for all
 * command line options or run the program using the <code>-help</code> option.
 * 
 */

public class Main {

    private static final String CMDLINE_CONFIG = "-config";
    private static final String CMDLINE_HELP = "-help";
    private static final String CMDLINE_EDIT = "-edit";
    private static final String CMDLINE_PROG = "-prog";

    private static Settings settings;

    private static StartupWindow startupWindow;

    public static final String PROPERTIES_FILE_KEY = "pseudo.settingsFile";
    public static final String BASE_DIRECTORY_KEY = "pseudo.baseDir";
    public static final String BASE_DIRECTORY;
    public static final String SYSTEM_DIRECTORY_KEY = "pseudo.sysDir";
    public static final String ASSERTION_PROPERTY = "pseudo.enableAssertions";

    private static final String VERSION_PATH = "/META-INF/VERSION";

    private static final List<ProofCenter> PROOF_CENTERS = new LinkedList<ProofCenter>();
    private static final List<PFileEditor> EDITORS = new LinkedList<PFileEditor>();

    public static final int PROBLEM_FILE = 0;
    public static final int PROOF_FILE = 1;

    /**
     * the number of recent files which are stored in the preferences and shown
     * in the menu.
     */
    private static final int NUMBER_OF_RECENT_FILES = 10;

    private static JFileChooser fileChooser[] = new JFileChooser[2];

    /*
     * - setup the settings from default resource, file and command line. - set
     * assertion state accordingly - set directories accordingly
     */
    static {
        loadProperties();
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(settings.getBoolean(ASSERTION_PROPERTY, true));
        // needed for the dummy-url "none:built-in", "buffer"
        Util.registerURLHandlers();
        
        BASE_DIRECTORY = settings.getProperty(BASE_DIRECTORY_KEY, ".");
    }

    public static void main(final String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    printVersion();

                    CommandLine commandLine = makeCommandLine();
                    commandLine.parse(args);

                    if (commandLine.isSet(CMDLINE_HELP)) {
                        commandLine.printUsage(System.out);
                        System.exit(0);
                    }

                    List<String> fileArguments = commandLine.getArguments();

                    if (fileArguments.isEmpty()) {
                        startupWindow = new StartupWindow();
                        startupWindow.setVisible(true);
                    } else {
                        File file = new File(fileArguments.get(0));
                        if (commandLine.isSet(CMDLINE_EDIT)) {
                            openEditor(file);
                        } else if(commandLine.isSet(CMDLINE_PROG)) {
                            String program = commandLine.getString(CMDLINE_PROG, "");
                            URL url = new URL("file", null, file.getAbsolutePath() + "#" + program);
                            openProverFromURL(url);
                        } else {
                            openProver(file);
                        }
                    }
                } catch (Throwable ex) {
                    Log.log(Log.ERROR, "Exception during startup: " + ex.getMessage());
                    Log.stacktrace(ex);
                    System.exit(1);
                }
            }
        });
    }

    private static void printVersion() {
        String version = "<unknown version>";
        try {
            URL resource = Main.class.getResource(VERSION_PATH);
            if (resource != null)
                version = Util.readURLAsString(resource);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("This is ivil - " + version);
    }

    private static CommandLine makeCommandLine() {
        CommandLine cl = new CommandLine();
        cl.addOption(CMDLINE_HELP, null, "Print usage");
        cl.addOption(CMDLINE_EDIT, null, "Edit the file instead of opening a prover frame");
        cl.addOption(CMDLINE_CONFIG, "file", "Read configuration from a file overwriting defaults.");
        cl.addOption(CMDLINE_PROG, "program", "Specify the program to use as problem.");
        return cl;
    }

    public static void openEditor(File file) throws IOException {
        PFileEditor editor = new PFileEditor(file);
        editor.setSize(600, 800);
        showFileEditor(editor);
    }

    private static void showFileEditor(PFileEditor editor) {
        if (startupWindow != null) {
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

        if (PROOF_CENTERS.isEmpty() && EDITORS.isEmpty())
            System.exit(0);
    }


    /**
     * Open a new {@link ProofCenter} for an environment loaded from a file.
     * <p>
     * Throws an {@link EnvironmentException} if the file does not specify
     * problem term. The {@link EnvironmentCreationService} is chosen by the
     * file extension of the resource specified by the url.
     * 
     * @param file
     *            the file to read the environment and problem term from.
     * 
     * @return a freshly created proof center
     * @see #openProverFromURL(URL)
     */
    public static ProofCenter openProver(File file)
            throws FileNotFoundException, ParseException, ASTVisitException,
            TermException, IOException, StrategyException, EnvironmentException {
        
        return openProverFromURL(file.toURI().toURL());
        
    }

    /**
     * Open a new {@link ProofCenter} for an environment loaded from a URL.
     * 
     * <p>
     * The {@link EnvironmentCreationService} is chosen by the file extension of
     * the resource specified by the url.
     * 
     * <p>
     * If the resource does not define a problem term, the fragment part of the
     * url is inspected. If it refers to a program <code>PP</code> in the parsed
     * environment, the term <code>[0; P]</code> is used as problem term. If
     * there is no program fragment, or the fragment does not refer to a program
     * in the environment, an exception is raised.
     * 
     * @param url
     *            the URL to read the environment from.
     * 
     * @return a freshly created proof center
     */
    public static ProofCenter openProverFromURL(URL url)
            throws FileNotFoundException, ParseException, ASTVisitException,
            TermException, IOException, StrategyException, EnvironmentException {
        
        Pair<Environment, Term> result =
            EnvironmentCreationService.createEnvironmentByExtension(url);

        Environment env = result.fst();
        Term problemTerm = result.snd();
        
        if(problemTerm == null) {
            String fragment = url.getRef();
            if(fragment == null || fragment.length() == 0)
                throw new EnvironmentException("Cannot load an environment without problem, no program specified");

            Program p = env.getProgram(fragment);
            if(p == null)
                throw new EnvironmentException("Unknown program '" + fragment + "' mentioned in URL " + url);
            
            problemTerm = new LiteralProgramTerm(0, false, p);
        }
        
        return openProver(env, problemTerm, url);
    }
    /**
     * Open a new {@link ProofCenter} for a given environment and the name of a
     * program.
     * 
     * <p>
     * The problem term is created as <code>[0; programIdentifier]</code>. The
     * URL to be stored in the history is created from the resource with
     * <code>#programIdentifier</code> amended.
     * 
     * <p>
     * If the resource does not define a problem term, the fragment part of the
     * url is inspected. If it refers to a program <code>PP</code> in the parsed
     * environment, the term <code>[0; P]</code> is used as problem term. If
     * there is no program fragment, or the fragment does not refer to a program
     * in the environment, an exception is raised.
     * 
     * @param env
     *            the environment to create the problem for
     * 
     * @param program
     *            the name of the program to create the problem term for.
     * 
     * @return a freshly created proof center
     */
    public static ProofCenter openProver(Environment env, Program program) 
            throws TermException, EnvironmentException, IOException, StrategyException {
        
        String resource = env.getResourceName();
        
        assert resource.indexOf('#') == -1 : "Resource already has a program reference";
        // assert env.getAllPrograms().contains(program);
        
        resource += "#" + program;
        
        LiteralProgramTerm problemTerm = new LiteralProgramTerm(0, false, program);
        
        return openProver(env, problemTerm, new URL(resource));
    }

    
    public static ProofCenter openProver(Environment env, Term problemTerm) 
            throws IOException, StrategyException, TermException {
        String resource = env.getResourceName();
        return openProver(env, problemTerm, new URL(resource));
    }
    
    /**
     * The internal method which does the actual opening. It creates a new
     * {@link Proof} object, then - with that - a new {@link ProofCenter}, opens
     * its main window and adds the given url to the list of recent urls.
     * 
     * @return a freshly created proof center
     */
    private static ProofCenter openProver(Environment env, Term problemTerm, URL urlToRemember) 
            throws IOException, StrategyException, TermException {
        Proof proof = new Proof(problemTerm);
        ProofCenter proofCenter = new ProofCenter(proof, env);

        showProofCenter(proofCenter);
        addToRecentProblems(urlToRemember);
        
        return proofCenter;
    }

    /**
     * adds a problem's URL to recent files; should be called after successfully
     * loading a problem. adding an url twice will remove the older duplicate;
     * if more then 10 entries are in the recent list, the oldest one will
     * perish
     * 
     * @param url
     *            location of the problem file
     */
    private static void addToRecentProblems(@NonNull URL url) {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        String recent[] = prefs.get("recent problems", "").split("\n");
        List<String> newRecent = new ArrayList<String>(recent.length + 1);
        String toAdd = url.toString();
        newRecent.add(toAdd);

        for (String p : recent) {
            if (!toAdd.equals(p))
                newRecent.add(p);
        }

        StringBuilder next = new StringBuilder(2 * NUMBER_OF_RECENT_FILES);
        for (int i = 0; i < NUMBER_OF_RECENT_FILES && i < newRecent.size(); i++) {
            if (i > 0) {
                next.append("\n");
            }
            next.append(newRecent.get(i));
        }

        prefs.put("recent problems", next.toString());
    }

    private static void showProofCenter(ProofCenter proofCenter) {
        if (startupWindow != null) {
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

        if (PROOF_CENTERS.isEmpty() && EDITORS.isEmpty())
            System.exit(0);
    }

    /**
     * check whether at least one open proof center or one editor has unsaved
     * changes
     * 
     * @return true iff there are changes in one window
     */
    public static boolean windowsHaveChanges() {
        for (ProofCenter pc : PROOF_CENTERS) {
            if (pc.getProof().hasUnsafedChanges())
                return true;
        }
        for (PFileEditor editor : EDITORS) {
            if (editor.hasUnsafedChanges())
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
                    fileChooser[index].addChoosableFileFilter(new ExporterFileFilter(export));
                }
            else {
                for(EnvironmentCreationService service : EnvironmentCreationService.getServices()) {
                    fileChooser[index].addChoosableFileFilter(new FileNameExtensionFilter(
                        service.getDescription(), service.getDefaultExtension()));
                }
            }
        }
        return fileChooser[index];
    }

    /**
     * add all properties from the system and from a certain file to the
     * properties in {@link ProofCenter}.
     * 
     * Command line and system overwrite the file
     */
    private static void loadProperties() {
        try {
            settings = Settings.getInstance();
            settings.loadKeyAsFile(PROPERTIES_FILE_KEY);
            settings.putAll(System.getProperties());
        } catch (FileNotFoundException e) {
            Log.log(Log.WARNING, "Cannot read property file, continuing anyway ...");
            // no stacktrace here
        } catch (IOException e) {
            Log.log(Log.WARNING, "Error reading property file, continuing anyway ...");
            Log.stacktrace(e);
        }
    }

}