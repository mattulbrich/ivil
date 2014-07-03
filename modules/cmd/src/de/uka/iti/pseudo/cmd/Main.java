/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.cmd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.CommandLine;
import de.uka.iti.pseudo.util.CommandLineException;
import de.uka.iti.pseudo.util.CompletedFuture;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.Util;
import de.uka.iti.pseudo.util.settings.Settings;

/**
 * This class provides the entry point for
 */
public class Main {

    private static enum ProofObligationOption {
        ALL, DEFAULT, SELECTED
    }


    /*
     * The constants for the command line option processing
     */
    private static final String CMDLINE_HELP = "-help";
    private static final String CMDLINE_CHECKPROOFS = "-c";
    private static final String CMDLINE_PROOFOBLIGATIONS = "-p";
    private static final String CMDLINE_VERBOSE = "-v";
    private static final String CMDLINE_ALL_OBLIGATIONS = "-a";
    private static final String CMDLINE_DEF_OBLIGATION = "-d";
    private static final String CMDLINE_TIMEOUT = "-t";
    private static final String CMDLINE_THREADS = "-threads";
    private static final String CMDLINE_SOURCE = "-s";
    private static final String CMDLINE_EXTENSIONS = "-x";
//    private static final String CMDLINE_PIPE = "-pipe";

    /**
     * The key to the settings to read the properties file name from
     */
    public static final String PROPERTIES_FILE_KEY = "pseudo.settingsFile";

    /**
     * The key to the settings which enables/disables assertions
     */
    public static final String ASSERTION_PROPERTY = "pseudo.enableAssertions";

    private static final int DEFAULT_TIMEOUT = 30;


    /*
     * Local fields that hold the values of the command line
     */
    private static boolean checkProofs;
    private static boolean verbose;
    private static int timeout;
    private static int numberThreads;
    private static boolean relayToSource;
    private static Object selectedProofObligations;

    /**
     * The thread pool in which the tasks will be executed.
     */
    private static ExecutorService executor;

    /**
     * The results of the execution are stored here.
     */
    private static List<Future<Result>> results = new ArrayList<Future<Result>>();
    private static Set<String> fileExtensions;

    /**
     * Prepare the command line options object.
     *
     * @return the command line
     */
    private static @NonNull
    CommandLine makeCommandLine() {
        CommandLine cl = new CommandLine();
        cl.addOption(CMDLINE_HELP, null, "Print usage");
        cl.addOption(CMDLINE_VERBOSE, null, "Be verbose in messages");
        cl.addOption(CMDLINE_CHECKPROOFS, null, "Proof file to check (pxml)");
        cl.addOption(CMDLINE_PROOFOBLIGATIONS, "<obligs>", "Comma-separated list of proof obligation IDs.");
//        cl.addOption(CMDLINE_RECURSIVE, null, "Apply recursively.");
        cl.addOption(CMDLINE_ALL_OBLIGATIONS, null, "Proof all obligations in files.");
        cl.addOption(CMDLINE_DEF_OBLIGATION, null, "Show the default obligations in files.");
        cl.addOption(CMDLINE_TIMEOUT, "[secs]", "time to run before interrupting (-1 for no timeout)");
        cl.addOption(CMDLINE_THREADS, "[no]", "number of simultaneously running threads");
        cl.addOption(CMDLINE_SOURCE, null, "relay error messages to sources");
//        cl.addOption(CMDLINE_PIPE, null, "only YES, NO or ERROR will be printed to stdout");
        cl.addOption(CMDLINE_EXTENSIONS, "<exts>",
                "comma separated list of scanned supported extensions (defaults to .p)");
        return cl;
    }

    /**
     * The entry point of the command line tool.
     *
     * This method parses the command line and creates tasks for each file. The
     * tasks are then delegated to the executor which executes them in one of
     * its threads. After the execution the results of the tasks is printed to
     * stdout.
     *
     * The application is terminated by this method. The return value is the
     * number of errors found in the files, or -1 if an exception has been
     * raised.
     */
    public static void main(String[] args) {

        try {
            loadProperties();
            ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(
                    Settings.getInstance().getBoolean(ASSERTION_PROPERTY, true));

            CommandLine commandLine = makeCommandLine();
            commandLine.parse(args);

            if (args.length == 0 || commandLine.isSet(CMDLINE_HELP)) {
                System.out.println("Usage: ivilc [options] [file(s)|dir(s)]");
                System.out.println();
                commandLine.printUsage(System.out);
                System.exit(0);
            }

            verbose = commandLine.isSet(CMDLINE_VERBOSE);
            timeout = commandLine.getInteger(CMDLINE_TIMEOUT, DEFAULT_TIMEOUT);
            numberThreads = commandLine.getInteger(CMDLINE_THREADS, 4);
            relayToSource = commandLine.isSet(CMDLINE_SOURCE);
            checkProofs = commandLine.isSet(CMDLINE_CHECKPROOFS);
            fileExtensions = Util.readOnlyArraySet(
                    commandLine.getString(CMDLINE_EXTENSIONS, ".p").split(","));

            if(commandLine.isSet(CMDLINE_ALL_OBLIGATIONS)) {
                selectedProofObligations = "ALL";
            }

            if(commandLine.isSet(CMDLINE_DEF_OBLIGATION)) {
                if (selectedProofObligations != null) {
                    throw new CommandLineException("Cannot specify more than one of " +
                            CMDLINE_ALL_OBLIGATIONS + ", " +
                            CMDLINE_PROOFOBLIGATIONS +
                            " and " + CMDLINE_DEF_OBLIGATION + ".");
                }
                selectedProofObligations = "DEFAULT";
            }

            if(commandLine.isSet(CMDLINE_PROOFOBLIGATIONS)) {
                if (selectedProofObligations != null) {
                    throw new CommandLineException("Cannot specify more than one of " +
                            CMDLINE_ALL_OBLIGATIONS + ", " +
                            CMDLINE_PROOFOBLIGATIONS +
                            " and " + CMDLINE_DEF_OBLIGATION + ".");
                }
                selectedProofObligations =
                        commandLine.getString(CMDLINE_PROOFOBLIGATIONS, "").split(",");
            }

            if(selectedProofObligations == null) {
                // that is the current default behaviour
                selectedProofObligations = "DEFAULT_IF_PRESENT";
            }


            printVersion();

//             if(verbose) {
//                 Log.setMinLevel(Log.ALL);
//             }

            executor = Executors.newFixedThreadPool(numberThreads);

            List<String> fileArguments = commandLine.getArguments();
            for (String file : fileArguments) {
                handleArgument(file);
            }

            executor.shutdown();

            int errorcount = 0;

            for (Future<Result> futResult : results) {
                Result result;
                try {
                    result = futResult.get();
                    result.print(System.err);
                    if(!result.success) {
                        errorcount++;
                    }
                } catch (Exception e) {
                    if(verbose) {
                        e.printStackTrace();
                    } else {
                        System.err.println(e.getMessage());
                    }
                    errorcount++;
                }
            }

            if(errorcount == 0) {
                System.err.println("All proof obligations have been discharged.");
            }

            System.exit(errorcount);

        } catch (Exception ex) {
            Log.stacktrace(Log.ERROR, ex);
            System.exit(-1);
        }

    }

    /**
     * Handle file or directory according to the options.
     *
     * A directory is only descended to if {@link #recursive} is set to true.
     *
     * A particular file is only examined if its extension is ".p" or
     * {@link #allSuffix} is set to true.
     *
     * @param directory
     *            the directory under which the file lives
     * @param fileName
     *            the (local) name of the file
     * @throws TermException
     * @throws IOException
     * @throws ASTVisitException
     * @throws ParseException
     * @throws EnvironmentException
     */
    private static void handleArgument(String argument) throws ParseException, ASTVisitException, IOException, TermException, EnvironmentException {
        File file = new File(argument);
        if(file.isDirectory()) {
            handleRecursively(file);
        } else {
            if(checkProofs) {
                checkSingleFile(file);
            } else {
                handleSingleFile(file);
            }
        }
    }

    private static void handleRecursively(File dir) throws ParseException, ASTVisitException, IOException, TermException, EnvironmentException {
        assert dir.isDirectory();
        File[] children = dir.listFiles();
        for (File child : children) {
            if(child.isDirectory()) {
                handleRecursively(child);
            }
            String name = child.getName();
            int index = name.lastIndexOf('.');
            if(index > 0) {
                // if it has an extension
                String ext = name.substring(name.lastIndexOf('.'));
                if(fileExtensions.contains(ext)) {
                    if(checkProofs) {
                        checkSingleFile(child);
                    } else {
                        handleSingleFile(child);
                    }
                }
            }
        }

    }

    /**
     * Handle single file - do actually something on it.
     *
     * An {@link FileProblemProverBuilder} object is created for the file. This is a
     * {@link Callable} and is enqueued in the {@link #executor} which will
     * eventually do the task
     *
     * The result (a {@link Future} value) is added to the results lists.
     *
     * Parameters are set on the prover object.
     * @throws EnvironmentException
     */
    private static void handleSingleFile(File file) throws ParseException,
                ASTVisitException, IOException, TermException, EnvironmentException {

        FileProblemProverBuilder builder;
        try {
            builder = new FileProblemProverBuilder(file);
        } catch (Exception e) {
            if(verbose) {
                e.printStackTrace();
            }
            Result result = new Result(false, file, "",
                    "Error while parsing", e.getMessage());
            results.add(new CompletedFuture<Result>(result));
            return;
        }


        builder.setTimeout(timeout);
//        builder.setRuleLimit(ruleLimit);
        builder.setRelayToSource(relayToSource);
        builder.setProofObligations(selectedProofObligations);

        for (AutomaticProblemProver app : builder.createProblemProvers()) {
            Future<Result> future = executor.submit(app);
            assert future != null;
            results.add(future);
        }
    }

    /**
     * Load a proof for a ivil problem file.
     *
     * The proof file's name is deduced.
     *
     * @param file
     *            the file containing the problem
     */
    private static void checkSingleFile(File file) {
        AutomaticProblemChecker checker = new AutomaticProblemChecker(file);
        Future<Result> future = executor.submit(checker);
        results.add(future);
    }

    /**
     * add all properties from the system and from a certain file to the
     * properties in {@link Settings}.
     *
     * Command line and system overwrite the file
     */
    private static void loadProperties() {
        try {
            Settings settings = Settings.getInstance();
            settings.loadKeyAsFile(PROPERTIES_FILE_KEY);
            settings.putAll(System.getProperties());
        } catch (IOException e) {
            Log.log(Log.WARNING, "Cannot read properties file, continuing anyway ...");
        }
    }

    /**
     * Prints the version of ivil.
     */
    private static void printVersion() {
        String version = Util.getIvilVersion();
        System.out.println("This is ivil - " + version);
    }

}
