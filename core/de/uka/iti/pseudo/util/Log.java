/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.util;

import java.io.PrintStream;

import de.uka.iti.pseudo.util.settings.Settings;


/**
 * Log provides static methods for a <b>very simple</b> logging mechanism.
 * 
 * Better use log4j or java.util.logging if more comfort is desired. A change
 * should be easily done.
 * 
 * @author mattias ulbrich
 */
public class Log {
    
    /**
     * ERROR is a message level indicating a serious failure.
     * <p>
     * In general ERROR messages should describe events that are
     * of considerable importance and which will prevent normal
     * program execution.   They should be reasonably intelligible
     * to end users and to system administrators.
     * This level is initialized to <CODE>50</CODE>.
     */
    public static final int ERROR = 50;

    /**
     * WARNING is a message level indicating a potential problem.
     * <p>
     * In general WARNING messages should describe events that will
     * be of interest to end users or system managers, or which
     * indicate potential problems.
     * This level is initialized to <CODE>40</CODE>.
     */
    public static final int WARNING = 40;
    
    /**
     * INFO is a message level for informational messages.
     * <p>
     * Typically INFO messages will be written to the console
     * or its equivalent.  So the INFO level should only be 
     * used for reasonably significant messages that will
     * make sense to developers and system admins.
     * This level is initialized to <CODE>30</CODE>.
     */
    public static final int DEBUG = 30;
    
    /**
     * VERBOSE is a message level providing more deep information.
     * <p>
     * In general the VERBOSE level should be used for information
     * that will be broadly interesting to developers who do not have
     * a specialized interest in the specific subsystem.
     * <p>
     * VERBOSE messages might include things like minor (recoverable)
     * failures.  Issues indicating potential performance problems
     * are also worth logging as VERBOSE.
     * This level is initialized to <CODE>20</CODE>.
     */
    public static final int VERBOSE = 20;
    
    /**
     * FINEST indicates a highly detailed tracing message.
     * This level is initialized to <CODE>10</CODE>. 
     */
    public static final int TRACE = 10;
    
    /**
     * ALL indicates all messages.
     * This level is initialized to <code>0</code>.
     */
    public static final int ALL = 0;
    
    /**
     * The minimum level of a log message to be displayed.
     * 
     * We cannot use Settings here because that leads to a loop -
     * Settings uses Log and always prints a message.
     */
    private static int minLevel = Integer.getInteger("pseudo.log", ERROR);

    
    private Log() {
        // only static methods
    }
    
    public static int getMinLevel() {
        return minLevel;
    }


    public static void setMinLevel(int level) {
        assert level >= 0;
        minLevel = level;
    }
    
    
    /**
     * Log an message (using {@link Object#toString()}) to {@link System#err}.
     * 
     * Logging level {@link #DEBUG} is used.
     * 
     * @see PrintStream#println(Object)
     * @param s the message to log
     */
    public static void println(Object s) {
        dbgPrint(DEBUG, s.toString());
    }
    
    /**
     * Log an message (using {@link Object#toString()}) to {@link System#err}.
     * A line break is automatically amended.
     * 
     * Logging level {@link #DEBUG} is used.
     * 
     * @see PrintStream#printf(String, Object...)
     * @param format the <code>printf</code> format string
     * @param args arguments to be formatted
     */
    public static void printf(String format, Object... args) {
        dbgPrint(DEBUG, String.format(format, args));
    }
    
    /**
     * Log an message (using {@link Object#toString()}) to {@link System#err}.
     * A line break is automatically amended.
     * 
     * Logging level {@link #DEBUG} is used.
     * 
     * @see PrintStream#printf(String, Object...)
     * @param format the <code>printf</code> format string
     * @param args arguments to be formatted
     */
    public static void log(String format, Object... args) {
        dbgPrint(DEBUG, String.format(format, args));
    }
    
    /**
     * Log an message (using {@link Object#toString()}) to {@link System#err}.
     * A line break is automatically amended.
     * 
     * Logging level <code>level</code> ist used.
     * 
     * @see PrintStream#printf(String, Object...)
     * @param level the level of the log message.
     * @param format the <code>printf</code> format string
     * @param args arguments to be formatted
     */
    public static void log(int level, String format, Object... args) {
        assert level >= 0;
        dbgPrint(level, String.format(format, args));
    }
    
    /**
     * Log an message (using {@link Object#toString()}) to {@link System#err}.
     * A line break is automatically amended.
     * 
     * Logging level <code>level</code> ist used.
     * 
     * @param message the message to log
     */
    public static void log(int level, Object message) {
        assert level >= 0;
        dbgPrint(level, message.toString());
    }
    
    /**
     * Log a method entry to {@link System#err}.
     * 
     * Logging level {@link #TRACE} is used.
     * 
     * @param args arguments to the method should be supplied
     */
    public static void enter(Object... args) {
        if(TRACE >= minLevel) {
            String prefix = getClassAndMethod(2);

            System.err.println("> Enter " + prefix);
            System.err.println("  Arguments: " + Util.join(args, ", "));
        }
    }
    
    /**
     * Log a method leave to {@link System#err}.
     * 
     * Logging level {@link #TRACE} is used.
     */
    public static void leave() {
        if(TRACE >= minLevel) {
            String prefix = getClassAndMethod(2);
            System.err.println("> Leave " + prefix);
        }
    }
    
    /**
     * print a string to stdout, prefixed by the execution context of the caller
     * of the calling function.
     * 
     * If {@link #showOnlyPrefixes} is defined, the output is only written, if
     * the caller prefix begins with one of the specified strings
     * 
     * @param string
     *            string to be printed out
     */
    private static final void dbgPrint(int level, String string) {
        if(level >= minLevel) {
            String prefix = getClassAndMethod(3);
            String threadName = Thread.currentThread().getName();
            System.err.println("> " + prefix + " [" + threadName + 
                    "] - Level " + levelToString(level) + "\n  " + string);
        }
    }

    private static String levelToString(int level) {
        switch(level) {
        case 50: return "ERROR";
        case 40: return "WARNING";
        case 30: return "DEBUG";
        case 20: return "VERBOSE";
        case 10: return "TRACE";
        }
        return Integer.toString(level);
    }

    /**
     * return information about some execution context. The context of interest
     * may have appeared several levels higher.
     * 
     * @author MU
     * @param level
     *            to go up in the context hierarchy
     * @return a String giving information about the stack of the calling
     *         function.
     */
    private static String getClassAndMethod(int level) {
        StackTraceElement[] trace = new Exception().getStackTrace();
        if (trace.length > level) {
            return trace[level].toString();
        }
        return "";
    }


    public static void stacktrace(Throwable e) {
        if(DEBUG >= minLevel) {
            String prefix = getClassAndMethod(2);
            System.err.println("> Exc in " + prefix + ":");
            e.printStackTrace(System.err);
        }
    }
    
    public static void stacktrace(int level, Throwable e) {
        if(level >= minLevel) {
            String prefix = getClassAndMethod(2);
            System.err.println("> Exc in " + prefix + ":");
            e.printStackTrace(System.err);
        }
    }

    
    
    public static void main(String[] args) {
        Log.setMinLevel(TRACE);
        enter((Object)args);
        
        Log.setMinLevel(DEBUG);
        // should not do anything
        leave();
        Log.log(VERBOSE, "VERBOSE: Should not be printed");
        Log.log(DEBUG, "DEBUG: Should be printed");
        Log.log(WARNING, "WARNING: Should be printed");
        Log.log(ERROR, "ERROR: Should be printed");
        Log.log(88, "88: Should be printed");
        
        try {
            throw new Exception("Hello World");
        } catch (Exception e) {
            stacktrace(e);
        }
        
        Log.setMinLevel(TRACE);
        leave();
    }
}
