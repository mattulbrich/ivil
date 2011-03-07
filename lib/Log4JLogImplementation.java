import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

import de.uka.iti.pseudo.util.Log;

/**
 * <h3>Usage</h3>
 *
 * Compile in the main directory using:
 *    ant build
 *    javac -classpath lib/log4j.jar:modules/core/classes -d modules/core/classes lib/Log4JLogImplementation.java
 *    ant jar
 * 
 * Run it using:
 *    ./ivil -DX-Dpseudo.logClass=Log4JLogImplementation
 * 
 * Copy doc/log4j.properties to main directory and adapt to your needs.
 */
public class Log4JLogImplementation implements Log.LogImplementation {

    static {
        PropertyConfigurator.configureAndWatch("log4j.properties", 60 * 1000);
    }

    @Override
    public void doLog(int level, String string) {
        StackTraceElement trace = getTrace(4);
        String className = trace.getClassName();
        String methodName = trace.getMethodName();
        Logger logger = Logger.getLogger(className);
        Level l4level = convertLevel(level);
        logger.log(l4level, "in " + methodName + ": " + string, null);
    }

    @Override
    public void doStackTrace(int level, Throwable e) {
        StackTraceElement trace = getTrace(4);
        String className = trace.getClassName();
        String methodName = trace.getMethodName();
        Logger logger = Logger.getLogger(className);
        Level l4level = convertLevel(level);
        logger.log(l4level, "EXCEPTION in class " + className + "."
                + methodName, null);
    }

    private Level convertLevel(int level) {

        if (level <= Log.ALL)
            return Level.ALL;

        if (level <= Log.TRACE)
            return Level.TRACE;

        if (level <= Log.VERBOSE)
            return Level.DEBUG;

        if (level <= Log.DEBUG)
            return Level.INFO;

        if (level <= Log.WARNING)
            return Level.WARN;

        if (level <= Log.ERROR)
            return Level.ERROR;

        return Level.FATAL;
    }

    /*
     * return information about some execution context. The context of interest
     * may have appeared several levels higher.
     * 
     * @author MU
     * 
     * @param level to go up in the context hierarchy
     * 
     * @return a String giving information about the stack of the calling
     * function.
     */
    private static StackTraceElement getTrace(int level) {
        StackTraceElement[] trace = new Exception().getStackTrace();
        if (trace.length > level) {
            return trace[level];
        }
        // TODO have something better
        return null;
    }

    // testing only
    public static void main(String[] args) {

        Log4JLogImplementation simpImpl = new Log4JLogImplementation();
        Log.setLogImplementation(simpImpl);

        PatternLayout layout = new PatternLayout(
                "%d{ISO8601} %-5p [%t] %c: %m%n");
        ConsoleAppender consoleAppender = new ConsoleAppender(layout);
        Logger.getRootLogger().addAppender(consoleAppender);
        Logger.getRootLogger().setLevel(Level.ALL);

        Log.enter((Object) args);

        // should not do anything
        Log.leave();
        Log.log(Log.VERBOSE, "VERBOSE: Should not be printed");
        Log.log(Log.DEBUG, "DEBUG: Should be printed");
        Log.log(Log.WARNING, "WARNING: Should be printed");
        Log.log(Log.ERROR, "ERROR: Should be printed");
        Log.log(88, "88: Should be printed");

        try {
            throw new Exception("Hello World");
        } catch (Exception e) {
            Log.stacktrace(e);
        }

        Log.leave();
    }

    @Override
    public boolean isLogging(int level) {
        // TODO do something more clever here !!
        return true;
    }

}
