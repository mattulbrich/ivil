/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.ProofObligation;
import de.uka.iti.pseudo.environment.ProofObligationManager;
import de.uka.iti.pseudo.environment.creation.EnvironmentCreationService;
import de.uka.iti.pseudo.gui.actions.RecentProblemsMenu;
import de.uka.iti.pseudo.gui.editor.PFileEditor;
import de.uka.iti.pseudo.gui.util.InputHistory;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.serialisation.ProofExport;
import de.uka.iti.pseudo.proof.serialisation.ProofImport;
import de.uka.iti.pseudo.proof.serialisation.ProofXML;
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
    private static final String CMDLINE_PO = "-po";
    private static final String CMDLINE_PROOF = "-proof";
    private static final String CMDLINE_SAMPLES = "-samples";
    private static final String CMDLINE_LAST = "-last";

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
    private static final InputHistory INPUT_HISTORY = new InputHistory("termInput", 20);

    private static JFileChooser fileChooser[] = new JFileChooser[2];

    /*
     * - setup the settings from default resource, file and command line.
     * - set assertion state accordingly
     * - set directories accordingly
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
            @Override
            public void run() {
                try {
                    printVersion();

                    CommandLine commandLine = makeCommandLine();
                    commandLine.parse(args);

                    if(commandLine.isSet(CMDLINE_HELP)) {
                        commandLine.printUsage(System.out);
                        System.exit(0);
                    }

                    if(commandLine.isSet(CMDLINE_CONFIG)) {
                        Properties p = new Properties();
                        p.load(new FileReader(commandLine.getString(CMDLINE_CONFIG, "")));
                        Settings.getInstance().putAll(p);
                    }

                    List<String> fileArguments = commandLine.getArguments();

                    if(commandLine.isSet(CMDLINE_LAST)) {
                        // -last
                        String mostRecentProblem = RecentProblemsMenu.getMostRecentProblem();
                        if(mostRecentProblem != null) {
                            openProverFromURL(new URL(mostRecentProblem));
                        }

                    } else if(fileArguments.isEmpty()) {
                        // no file args
                        startupWindow = new StartupWindow();
                        startupWindow.setVisible(true);
                        if(commandLine.isSet(CMDLINE_SAMPLES)) {
                            startupWindow.showSampleBrowser();
                        }
                    } else {

                        // at least one file/url arg
                        File file = new File(fileArguments.get(0));
                        if (commandLine.isSet(CMDLINE_EDIT)) {
                            openEditor(file);
                        } else {
                            ProofCenter center;
                            if(commandLine.isSet(CMDLINE_PO)) {
                                // proof obligation set
                                String proofObl = commandLine.getString(CMDLINE_PO, "");
                                URL url = new URL("file", null, file.getAbsolutePath() + "#" + proofObl);
                                center = openProverFromURL(url);
                            } else {
                                center = openProver(file);
                            }

                            if(commandLine.isSet(CMDLINE_PROOF)) {
                                String proofFile = commandLine.getString(CMDLINE_PROOF, "");
                                ProofImport proofImport = new ProofXML();
                                proofImport.importProof(new FileInputStream(proofFile),
                                        center.getProof(), center.getEnvironment(), null);
                                center.fireNotification(ProofCenter.PROOFTREE_HAS_CHANGED);
                                center.getProof().changesSaved();
                            }
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
            if (resource != null) {
                version = Util.readURLAsString(resource);
            }
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
        cl.addOption(CMDLINE_PO, "id", "Specify the proof obligation to prove.");
        cl.addOption(CMDLINE_PROOF, "file", "Load proof from this file.");
        cl.addOption(CMDLINE_SAMPLES, null, "Open the sample browser");
        cl.addOption(CMDLINE_LAST, null, "Reload the most recent problem");
        return cl;
    }


    public static PFileEditor openEditor(File file) throws IOException {
        PFileEditor editor = new PFileEditor(file);
        editor.setSize(600, 800);
        showFileEditor(editor);
        return editor;
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

        if (PROOF_CENTERS.isEmpty() && EDITORS.isEmpty()) {
            exit(0);
        }
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
    public static @Nullable ProofCenter openProver(File file)
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
     * @return a freshly created proof center, <code>null</code> if the user
     *         aborted the creation
     */
    public static @Nullable ProofCenter openProverFromURL(URL url)
            throws FileNotFoundException, ParseException,
            ASTVisitException, TermException, IOException,
            StrategyException, EnvironmentException {

        ProofObligationManager proofObMan =
                EnvironmentCreationService.createEnvironmentByExtension(url);

        Environment env = proofObMan.getEnvironment();

        if (!proofObMan.hasProofObligations()) {
            throw new EnvironmentException(
                    "This environment does not contain a problem description");
        }

        String fragment = url.getRef();
        ProofObligation po = null;

        if (fragment == null || fragment.length() == 0) {
            // no #fragment specified
            if(proofObMan.hasDefaultProofObligation()) {

                po = proofObMan.getDefaultProofObligation();

            } else {

                // there are many, no default: ask the user
                String[] availablePOs = proofObMan.getAvailableProofObligationNames();
                String name = (String) JOptionPane.showInputDialog(null,
                        "Please choose the proof obligation to verify.",
                        "Choose obligation", JOptionPane.QUESTION_MESSAGE, null,
                        availablePOs, null);
                if(name == null) {
                    // abort button pressed
                    return null;
                }
                po = proofObMan.getProofObligation(name);
                assert po != null : "No null elements in problemSeqs";
            }

        } else {
            // there IS a fragment.
            po = proofObMan.getProofObligation(fragment);

            if (po == null) {
                throw new EnvironmentException("Unknown proof obligation '" +
                        fragment + "' in URL " + url);
            }
        }

        return openProver(po, proofObMan, url);
    }

//    /**
//     * Open a new {@link ProofCenter} for a given environment and the name of a
//     * program.
//     *
//     * <p>
//     * The problem term is created as <code>[0; programIdentifier]</code>. The
//     * URL to be stored in the history is created from the resource with
//     * <code>#programIdentifier</code> amended.
//     *
//     * <p>
//     * If the resource does not define a problem term, the fragment part of the
//     * url is inspected. If it refers to a program <code>PP</code> in the parsed
//     * environment, the term <code>[0; P]</code> is used as problem term. If
//     * there is no program fragment, or the fragment does not refer to a program
//     * in the environment, an exception is raised.
//     *
//     * @param env
//     *            the environment to create the problem for
//     *
//     * @param program
//     *            the name of the program to create the problem term for.
//     *
//     * @return a freshly created proof center
//     */
//    public static ProofCenter openProver(Environment env, Program program)
//            throws TermException, EnvironmentException, IOException, StrategyException {
//
//        String resource = env.getResourceName();
//
//        assert resource.indexOf('#') == -1 : "Resource already has a program reference";
//        // assert env.getAllPrograms().contains(program);
//
//        resource += "#" + program;
//
//        LiteralProgramTerm problemTerm = LiteralProgramTerm.getInst(0, Modality.BOX, program, Environment.getTrue());
//        Sequent problemSeq = new Sequent(Collections.<Term>emptyList(), Collections.singletonList(problemTerm));
//
//        String proofIdentifier = ProofScript.PROGRAM_IDENTIFIER_PREFIX + program.getName();
//        return openProver(problemSeq, new URL(resource));
//    }


//    public static ProofCenter openProver(Environment env, Sequent problemTerm)
//            throws IOException, StrategyException, TermException {
//        String resource = env.getResourceName();
//        return openProver(env, proofIdentifier, problemTerm, new URL(resource));
//    }

    /**
     * The internal method which does the actual opening. It creates a new
     * {@link Proof} object, then - with that - a new {@link ProofCenter}, opens
     * its main window and adds the given url to the list of recent urls.
     *
     * @return a freshly created proof center
     */
    private static ProofCenter openProver(ProofObligation po,
            ProofObligationManager proofObMan, URL urlToRemember)
            throws IOException, StrategyException, TermException, EnvironmentException {

        Proof proof = po.initProof();
        ProofCenter proofCenter = new ProofCenter(proof, proof.getEnvironment(),
                proofObMan.getProofScriplets());

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
        List<String> newRecent = new ArrayList<String>(recent.length+1);

        String toAdd = url.toString();
        newRecent.add(toAdd);

        // add old recent files w/o the parameter
        for (int i = 0; i < recent.length &&
                newRecent.size() < NUMBER_OF_RECENT_FILES; i++) {

            if (!toAdd.equals(recent[i])) {
                newRecent.add(recent[i]);
            }
        }

        assert newRecent.size () <= NUMBER_OF_RECENT_FILES;

        String prefString = Util.join(newRecent, "\n");
        prefs.put("recent problems", prefString);

        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            // this is quite an unimportant error. ... Only log it.
            Log.log(Log.ERROR, "Could not store away the list of recently opened files.", e);
        }
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
        proofCenter.fireNotification(ProofCenter.TERMINATION);
        main.dispose();
        PROOF_CENTERS.remove(proofCenter);

        if (PROOF_CENTERS.isEmpty() && EDITORS.isEmpty()) {
            exit(0);
        }
    }

    public static void exit(int exitValue) {
        INPUT_HISTORY.saveToPreferences();
        System.exit(exitValue);
    }

    public static InputHistory getTermInputHistory() {
        return INPUT_HISTORY;
    }

    /**
     * check whether at least one open proof center or one editor
     * has unsaved changes
     * @return true iff there are changes in one window
     */
    public static boolean windowsHaveChanges() {
        for (ProofCenter pc : PROOF_CENTERS) {
            if (pc.getProof().hasUnsafedChanges()) {
                return true;
            }
        }
        for (PFileEditor editor : EDITORS) {
            if(editor.hasUnsavedChanges()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return a file chooser for the given type of files.
     *
     * If no chooser has been created yet, it is created, otherwise the existing
     * object is reused and returned (including the last set path).
     *
     * The different types are initialised differently. Problem file chooser
     * have the available {@link EnvironmentCreationService}s as filters whereas
     * proof choosers use the {@link ProofExport}s as filters.
     *
     * @param either
     *            {@link #PROBLEM_FILE} or {@link #PROBLEM_FILE}
     * @return the file chooser for the given index. Same on two calls.
     */
    public static @NonNull JFileChooser makeFileChooser(int index) {

        assert index == PROBLEM_FILE || index == PROOF_FILE;

        if (fileChooser[index] == null) {
            fileChooser[index] = new JFileChooser(".");

            if (index == PROOF_FILE) {
                for (ProofExport export : ServiceLoader.load(ProofExport.class)) {
                    ExporterFileFilter filter = new ExporterFileFilter(export);
                    fileChooser[index].addChoosableFileFilter(filter);
                }
            } else {

                for(EnvironmentCreationService service : EnvironmentCreationService.getServices()) {
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        service.getDescription(), service.getDefaultExtension());
                    fileChooser[index].addChoosableFileFilter(filter);
                }
            }

            FileFilter[] filters = fileChooser[index].getChoosableFileFilters();
            if(filters.length > 1) {
                fileChooser[index].setFileFilter(filters[1]);
            }
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
        } catch (FileNotFoundException e) {
            Log.log(Log.WARNING, "Cannot read property file, continuing anyway ...");
            // no stacktrace here
        } catch (IOException e) {
            Log.log(Log.WARNING, "Error reading property file, continuing anyway ...");
            Log.stacktrace(e);
        }
    }

}