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
package de.uka.iti.pseudo.cmd;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.CommandLine;
import de.uka.iti.pseudo.util.CommandLineException;
import de.uka.iti.pseudo.util.Util;
import de.uka.iti.pseudo.util.settings.Settings;

public class Main {
    private static final String CMDLINE_RECURSIVE = "-r";
    private static final String CMDLINE_HELP = "-help";
    private static final String CMDLINE_CHECKONLY = "-c";
    private static final String CMDLINE_VERBOSE = "-v";
    private static final String CMDLINE_ALLSUFFIX = "-all";
    private static final String CMDLINE_TIMEOUT = "-t";
    private static final String CMDLINE_THREADS = "-threads";
    private static final String CMDLINE_SOURCE = "-s";
    
    public static final String PROPERTIES_FILE_KEY = "pseudo.settingsFile";
    public static final String ASSERTION_PROPERTY = "pseudo.enableAssertions";

    private static final String VERSION_PATH = "/META-INF/VERSION";

    
    private static boolean recursive;
    private static boolean checkOnly;
    private static boolean allSuffix;
    private static boolean verbose;
    private static int timeout;
    private static int numberThreads;
    private static boolean relayToSource;
    
    private static ExecutorService executor;
    private static List<Future<Result>> results =
        new ArrayList<Future<Result>>();
    
    private static CommandLine makeCommandLine() {
        CommandLine cl = new CommandLine();
        cl.addOption(CMDLINE_HELP, null, "Print usage");
        cl.addOption(CMDLINE_VERBOSE, null, "Be verbose in messages");
        cl.addOption(CMDLINE_CHECKONLY, null, "Only read and check proofs,  do not try proving yourself");
        cl.addOption(CMDLINE_RECURSIVE, null, "Apply recursively.");
        cl.addOption(CMDLINE_ALLSUFFIX, null, "Read all files (not only *.p)");
        cl.addOption(CMDLINE_TIMEOUT, "[secs]", "time to run before interrupting (-1 for no timeout)");
        cl.addOption(CMDLINE_THREADS, "[secs]", "number of simultaneously running threads");
        cl.addOption(CMDLINE_SOURCE, null, "relay error messages to sources");
        return cl;
    }

    /**
     * @param args
     * @throws CommandLineException
     * @throws IOException 
     * @throws ASTVisitException 
     * @throws ParseException 
     * @throws TermException 
     */
    public static void main(String[] args) throws CommandLineException, ParseException, ASTVisitException, IOException, TermException {
        
        printVersion();
        
        loadProperties();
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(
                Settings.getInstance().getBoolean(ASSERTION_PROPERTY, true));
        
        CommandLine commandLine = makeCommandLine();
        commandLine.parse(args);
        
        if(args.length == 0 || commandLine.isSet(CMDLINE_HELP)) {
            commandLine.printUsage(System.out);
            System.exit(0);
        }
        
        recursive = commandLine.isSet(CMDLINE_RECURSIVE);
        checkOnly = commandLine.isSet(CMDLINE_CHECKONLY);
        allSuffix = commandLine.isSet(CMDLINE_ALLSUFFIX);
        verbose = commandLine.isSet(CMDLINE_VERBOSE);
        timeout = commandLine.getInteger(CMDLINE_TIMEOUT, 5000);
        numberThreads = commandLine.getInteger(CMDLINE_THREADS, 4);
        relayToSource = commandLine.isSet(CMDLINE_SOURCE);
        
        executor = Executors.newFixedThreadPool(numberThreads);
        
        List<String> fileArguments = commandLine.getArguments();
        for (String file : fileArguments) {
            handleFile(null, file);
        }
        
        executor.shutdown();
        
        int errorcount = 0;
        
        for (Future<Result> futResult: results) {
            Result result;
            try {
                result = futResult.get();
                if(result.getSuccess())
                    errorcount ++;
                result.print(System.err);
            } catch (Exception e) {
                e.printStackTrace();
                errorcount++;
            }
        }
        
        System.exit(errorcount);
    }
    
    private static void handleFile(File directory, String fileName) throws ParseException, ASTVisitException, IOException, TermException {
        File file = new File(directory, fileName);
        if(file.isDirectory()) {
            if(recursive) {
                String[] children = file.list();
                for (String child : children) {
                    handleFile(file, child);
                }
            }
        } else {
            if(allSuffix || file.getName().endsWith(".p")) {
                handleSingleFile(file);
            }
        }
    }
    
    private static void handleSingleFile(File file) throws ParseException, ASTVisitException, IOException, TermException  {
        
        AutomaticFileProver prover = new AutomaticFileProver(file);
        
        if(!prover.hasProblem()) {
            if(verbose) {
                System.err.println(file + " does not contain a problem ... ignored");
            }
            return;
        }
        
        prover.setTimeout(timeout);
        prover.setRelayToSource(relayToSource);
        
        results.add(executor.submit(prover));
       
    }
    
    /**
     * add all properties from the system and from a certain file to
     * the properties in {@link Settings}.
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

}
