/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.cmd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import nonnull.NonNull;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.CommandLine;
import de.uka.iti.pseudo.util.Util;
import de.uka.iti.pseudo.util.settings.Settings;

/**
 * This class provides the entry point for
 */
public class Main {

    /*
     * The constants for the command line option processing
     */
    private static final String CMDLINE_RECURSIVE = "-r";
    private static final String CMDLINE_HELP = "-help";
    private static final String CMDLINE_CHECKONLY = "-c";
    private static final String CMDLINE_VERBOSE = "-v";
    private static final String CMDLINE_ALLSUFFIX = "-all";
    private static final String CMDLINE_TIMEOUT = "-t";
    private static final String CMDLINE_RULELIMIT = "-n";
    private static final String CMDLINE_THREADS = "-threads";
    private static final String CMDLINE_SOURCE = "-s";
    private static final String CMDLINE_PIPE = "-pipe";

    /**
     * The key to the settings to read the properties file name from
     */
    public static final String PROPERTIES_FILE_KEY = "pseudo.settingsFile";

    /**
     * The key to the settings which enables/disables assertions
     */
    public static final String ASSERTION_PROPERTY = "pseudo.enableAssertions";

    /*
     * Local fields that hold the values of the command line
     */
    private static boolean pipeMode;
    private static boolean recursive;
    private static boolean checkOnly;
    private static boolean allSuffix;
    private static boolean verbose;
    private static int timeout;
    private static int ruleLimit;
    private static int numberThreads;
    private static boolean relayToSource;

    /**
     * The thread pool in which the tasks will be executed.
     */
    private static ExecutorService executor;

    /**
     * The results of the execution are stored here.
     */
    private static List<Future<Result>> results = new ArrayList<Future<Result>>();

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
        cl.addOption(CMDLINE_CHECKONLY, null, "Only read and check proofs,  do not try proving yourself");
        cl.addOption(CMDLINE_RECURSIVE, null, "Apply recursively.");
        cl.addOption(CMDLINE_ALLSUFFIX, null, "Read all files (not only *.p)");
        cl.addOption(CMDLINE_TIMEOUT, "[secs]", "time to run before interrupting (-1 for no timeout)");
        cl.addOption(CMDLINE_RULELIMIT, "[no]", "number of rule applications before interrupting (-1 for no timeout)");
        cl.addOption(CMDLINE_THREADS, "[no]", "number of simultaneously running threads");
        cl.addOption(CMDLINE_SOURCE, null, "relay error messages to sources");
        cl.addOption(CMDLINE_PIPE, null, "only YES, NO or ERROR will be printed to stdout");
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
                System.out.println("Usage: ivilc [options] [files|dirs]");
                System.out.println();
                commandLine.printUsage(System.out);
                System.exit(0);
            }

            recursive = commandLine.isSet(CMDLINE_RECURSIVE);
            checkOnly = commandLine.isSet(CMDLINE_CHECKONLY);
            allSuffix = commandLine.isSet(CMDLINE_ALLSUFFIX);
            verbose = commandLine.isSet(CMDLINE_VERBOSE);
            timeout = commandLine.getInteger(CMDLINE_TIMEOUT, 5);
            ruleLimit = commandLine.getInteger(CMDLINE_RULELIMIT, -1);
            numberThreads = commandLine.getInteger(CMDLINE_THREADS, 4);
            relayToSource = commandLine.isSet(CMDLINE_SOURCE);
            pipeMode = commandLine.isSet(CMDLINE_PIPE);

            if (!pipeMode)
                printVersion();

            // if(verbose) {
            // Log.setMinLevel(Log.ALL);
            // }

            executor = Executors.newFixedThreadPool(numberThreads);

            List<String> fileArguments = commandLine.getArguments();
            for (String file : fileArguments) {
                handleFile(null, file);
            }

            executor.shutdown();

            int errorcount = 0;

            for (Future<Result> futResult : results) {
                Result result;
                try {
                    result = futResult.get();
                    if(pipeMode)
                        System.out.println(result.getSuccess() ? "YES" : "NO");
                    else if (!result.getSuccess()) {
                        errorcount++;
                        result.print(System.err);
                    }
                } catch (Exception e) {
                    if(pipeMode)
                        System.out.println("ERROR");
                    else
                        e.printStackTrace();
                    errorcount++;
                }
            }
            
            if(!pipeMode && errorcount == 0) {
                System.out.println("All proof obligations have been discharged.");
            }

            System.exit(errorcount);
        } catch (Exception ex) {
            if (pipeMode)
                System.out.println("ERROR");

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
     */
    private static void handleFile(File directory, String fileName) throws ParseException, ASTVisitException,
            IOException, TermException {
        File file = new File(directory, fileName);
        if (file.isDirectory()) {
            if (recursive) {
                String[] children = file.list();
                for (String child : children) {
                    handleFile(file, child);
                }
            }
        } else {
            if (allSuffix || file.getName().endsWith(".p")) {
                handleSingleFile(file);
            }
        }
    }

    /**
     * Handle single file - do actually something on it.
     * 
     * An {@link AutomaticFileProver} object is created for the file. This is a
     * {@link Callable} and is enqueued in the {@link #executor} which will
     * eventually do the task
     * 
     * The result (a {@link Future} value) is added to the results lists.
     * 
     * Parameters are set on the prover object.
     */
    private static void handleSingleFile(File file) throws ParseException, ASTVisitException, IOException,
            TermException {

        AutomaticFileProver prover = new AutomaticFileProver(file);

        if (!prover.hasProblem()) {
            if (verbose) {
                System.err.println(file + " does not contain a problem ... ignored");
            }
            return;
        }

        prover.setTimeout(timeout);
        prover.setRuleLimit(ruleLimit);
        prover.setRelayToSource(relayToSource);

        Future<Result> future = executor.submit(prover);
        assert future != null;
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
            System.err.println("Cannot read properties file, continuing anyway ...");
            e.printStackTrace();
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
