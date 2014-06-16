// ant build ; javac -g -classpath modules/core/classes -d modules/core/classes lib/JavaUtilLogImplementation.java ; ant jar
// ivil -DX-Dpseudo.logClass=JavaUtilLogImplementation
import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import de.uka.iti.pseudo.util.Log;

/**
 * <h3>Usage</h3>
 *
 * Compile in the main directory using:
 *    ant build
 *    javac -classpath modules/core/classes -d modules/core/classes lib/JavaUtilLogImplementation.java
 *    ant jar
 *
 * Run it using:
 *    ./ivil -DX-Dpseudo.logClass=JavaUtilLogImplementation
 *
 * Assuming there is the viewer under lib call
 *    java -jar lib/JLogViewer.jar
 * starts the simple log inspector, equivalent to "chainsaw".
 */
public class JavaUtilLogImplementation implements Log.LogImplementation {

    private static final String CONFIG =
            "handlers = java.util.logging.FileHandler," +
            "           java.util.logging.ConsoleHandler," +
            "           java.util.logging.SocketHandler\n" +
            ".level = WARNING\n" +
            "java.util.logging.ConsoleHandler.level = ALL\n" +
            "java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter\n" +
            "java.util.logging.FileHandler.limit = 100000\n" +
            "java.util.logging.FileHandler.count = 10\n" +
            "java.util.logging.FileHandler.pattern = java-%g.log\n" +
            "java.util.logging.ConsoleHandler.level = WARNING\n" +
            "java.util.logging.SimpleFormatter.format=%1$tc %2$s%n%4$s: %5$s%6$s%n\n" +
            "java.util.logging.SocketHandler.host = localhost\n" +
            "java.util.logging.SocketHandler.port = 4459\n" +
            "java.util.logging.SocketHandler.level = ALL\n" +
            "de.uka.iti.level = ALL";

    static {
        try {
            LogManager.getLogManager().
                readConfiguration(new ByteArrayInputStream(CONFIG.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final boolean ALWAYS_STACKTRACE =
        Boolean.getBoolean("pseudo.log.alwaysStacktrace");

    @Override
    public void doLog(int level, String string) {
        Level l4level = convertLevel(level);
        Throwable stack = ALWAYS_STACKTRACE ? new Throwable() : null;
        StackTraceElement trace = getTrace(4);
        if(trace == null) {
            Logger logger = Logger.getLogger("de.uka.iti.pseudo");
            logger.warning("Cannot determine location for log");
            logger.log(l4level, string, stack);
        } else {
            String className = trace.getClassName();
            String methodName = trace.getMethodName();
            Logger logger = Logger.getLogger(className);
            logger.log(l4level, "in " + methodName + ": " + string, stack);
        }
    }

    @Override
    public void doStackTrace(int level, Throwable e) {
        Level l4level = convertLevel(level);
        StackTraceElement trace = getTrace(4);
        if(trace == null) {
            Logger logger = Logger.getLogger("de.uka.iti.pseudo");
            logger.warning("Cannot determine location for log");
            logger.log(l4level, "EXCEPTION", e);
        } else {
            String className = trace.getClassName();
            String methodName = trace.getMethodName();
            Logger logger = Logger.getLogger(className);
            logger.log(l4level, "EXCEPTION in class " + className + "."
                    + methodName, e);
        }
    }

    private Level convertLevel(int level) {

        if (level <= Log.ALL) {
            return Level.ALL;
        }

        if (level <= Log.TRACE) {
            return Level.FINE;
        }

        if (level <= Log.VERBOSE) {
            return Level.CONFIG;
        }

        if (level <= Log.DEBUG) {
            return Level.INFO;
        }

        if (level <= Log.WARNING) {
            return Level.WARNING;
        }

        if (level <= Log.ERROR) {
            return Level.SEVERE;
        }

        return Level.OFF;
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

        JavaUtilLogImplementation simpImpl = new JavaUtilLogImplementation();
        Log.setLogImplementation(simpImpl);

        System.out.println(LogManager.getLogManager().getProperty(".handlers"));

        Logger.getLogger("test").severe("warn message");

        Log.enter((Object) args);

        Log.leave();
        Log.log(Log.VERBOSE, "DEBUG: Should be printed");
        Log.log(Log.DEBUG, "INFO: Should be printed");
        Log.log(Log.WARNING, "WARNING: Should be printed");
        Log.log(Log.ERROR, "ERROR: Should be printed");
        Log.log(88, "FATAL: Should be printed");

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
