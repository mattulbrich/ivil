package de.uka.iti.pseudo.cmd;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.util.CommandLine;
import de.uka.iti.pseudo.util.CommandLineException;
import de.uka.iti.pseudo.util.settings.Settings;

public class Main {
    private static final String CMDLINE_RECURSIVE = "-r";
    private static final String CMDLINE_HELP = "-help";
    private static final String CMDLINE_CHECKONLY = "-c";
    private static final String CMDLINE_VERBOSE = "-v";
    private static final String CMDLINE_ALLSUFFIX = "-all";
    
    public static final String PROPERTIES_FILE_KEY = "pseudo.settingsFile";
    public static final String ASSERTION_PROPERTY = "pseudo.enableAssertions";
    
    private static boolean recursive;
    private static boolean checkOnly;
    private static boolean allSuffix;
    
    private static CommandLine makeCommandLine() {
        CommandLine cl = new CommandLine();
        cl.addOption(CMDLINE_HELP, null, "Print usage");
        cl.addOption(CMDLINE_VERBOSE, null, "Be verbose in messages");
        cl.addOption(CMDLINE_CHECKONLY, null, "Only read and check proofs,  do not try proving yourself");
        cl.addOption(CMDLINE_RECURSIVE, null, "Apply recursively.");
        cl.addOption(CMDLINE_ALLSUFFIX, null, "Read all files (not only *.p)");
        return cl;
    }

    /**
     * @param args
     * @throws CommandLineException 
     */
    public static void main(String[] args) throws CommandLineException {
        
        loadProperties();
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(
                Settings.getInstance().getBoolean(ASSERTION_PROPERTY, true));
        
        CommandLine commandLine = makeCommandLine();
        commandLine.parse(args);
        
        if(commandLine.isSet(CMDLINE_HELP)) {
            commandLine.printUsage(System.out);
            System.exit(0);
        }
        
        recursive = commandLine.isSet(CMDLINE_RECURSIVE);
        checkOnly = commandLine.isSet(CMDLINE_CHECKONLY);
        allSuffix = commandLine.isSet(CMDLINE_ALLSUFFIX);
        
        List<String> fileArguments = commandLine.getArguments();
        for (String file : fileArguments) {
            handleFile(null, file);
        }
    }
    
    private static void handleFile(File directory, String fileName) {
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
    
    private static void handleSingleFile(File file) {
        
    }
    
    /**
     * add all properties from the system and from a certain file to
     * the properties in {@link ProofCenter}.
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

}
