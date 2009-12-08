package de.uka.iti.pseudo.cmd;

import java.util.List;

import de.uka.iti.pseudo.util.CommandLine;
import de.uka.iti.pseudo.util.CommandLineException;

public class Main {
    private static final String CMDLINE_RECURSIVE = "-r";
    private static final String CMDLINE_HELP = "-help";
    private static final String CMDLINE_CHECKONLY = "-c";
    private static final String CMDLINE_VERBOSE = "-v";
    
    private static boolean recursive;
    private static boolean checkOnly;
    
    /**
     * @param args
     * @throws CommandLineException 
     */
    public static void main(String[] args) throws CommandLineException {
        CommandLine commandLine = makeCommandLine();
        commandLine.parse(args);
        
        if(commandLine.isSet(CMDLINE_HELP)) {
            commandLine.printUsage(System.out);
            System.exit(0);
        }
        
        recursive = commandLine.isSet(CMDLINE_RECURSIVE);
        checkOnly = commandLine.isSet(CMDLINE_CHECKONLY);
        
        List<String> fileArguments = commandLine.getArguments();
        
        for (String file : fileArguments) {
            handleFileArgument(file);
        }
    }
    
    private static void handleFileArgument(String file) {
        // TODO Implement Main.handleFileArgument
        
    }

    private static CommandLine makeCommandLine() {
        CommandLine cl = new CommandLine();
        cl.addOption(CMDLINE_HELP, null, "Print usage");
        cl.addOption(CMDLINE_VERBOSE, null, "Be verbose in messages");
        cl.addOption(CMDLINE_CHECKONLY, null, "Only read and check proofs,  do not try proving yourself");
        cl.addOption(CMDLINE_RECURSIVE, null, "Apply recursively.");
        return cl;
    }

}
