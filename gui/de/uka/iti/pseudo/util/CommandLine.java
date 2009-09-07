package de.uka.iti.pseudo.util;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CommandLine {
    
    private static class Option {
        String description;
        String image;
        String value;
        String parameter;
    }
    
    private Map<String, Option> options = new LinkedHashMap<String, Option>();
    
    private List<String> arguments = new LinkedList<String>();

    public CommandLine() {
    }
    
    public void addOption(String image, String parameter, String description) {
        Option o = new Option();
        o.image = image;
        o.parameter = parameter;
        o.description = description;
        options.put(image, o);
    }
    
    public void parse(String args[]) throws CommandLineException {
        int cnt = 0;
        while (cnt < args.length && args[cnt].startsWith("-")) {
            
            if("--".equals(args[cnt])) {
                cnt++;
                break;
            }
            
            String current = args[cnt];
            Option option = options.get(current);
            
            if(option == null) {
                throw new CommandLineException("Unknown command line option: " + current);
            }
            
            if(option.parameter != null) {
                if(cnt == args.length - 1)
                    throw new CommandLineException("Command line option " + current + " expects a parameter but did not receive one");
                cnt ++;
                option.value = args[cnt];
            } else {
                option.value = "true";
            }
            
            cnt ++;
        }
        
        while(cnt < args.length) {
            arguments.add(args[cnt]);
            cnt ++;
        }
    }
    
    public List<String> getArguments() {
        return arguments;
    }
    
    public Boolean isSet(String param) {
        
        Option option = options.get(param);
        assert option != null : param + " is unknown option";
        assert option.parameter == null : param + " is not boolean";
        
        return "true".equals(option.value);
    }
    
    public String getString(String param, String defaultValue) {
        Option option = options.get(param);
        assert option != null : param + " is unknown option";
        
        String value = option.value;
        return value == null ? defaultValue : value; 
    }
    
    public int getInteger(String param, int defaultValue) throws CommandLineException {
        Option option = options.get(param);
        assert option != null : param + " is unknown option";
        
        String value = option.value;
        if(value == null)
            return defaultValue;
        
        try {
            return Integer.decode(value);
        } catch (NumberFormatException e) {
            throw new CommandLineException(param + " expects an integer argument, but received: " + option.value);
        }
    }

    public void printUsage(PrintStream stream) {
        int maxlen = 0;
        
        for (Option option : options.values()) {
            int len = option.image.length();
            if(option.value != null)
                len += 1 + option.value.length();
            maxlen = Math.max(len, maxlen);
        }
        
        maxlen += 2;
        
        for (Option option : options.values()) {
            String s = option.image;
            if(option.value != null)
                s += " " + option.value;
            stream.print(s);
            for (int i = maxlen - s.length(); i > 0; i--) {
                stream.print(" ");
            }
            stream.println(option.description);
        }
        
        stream.flush();
    }

}
