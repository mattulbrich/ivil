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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nonnull.NonNull;

// TODO: Auto-generated Javadoc
/**
 * This data class models the result of an automatic run if ivil.
 *
 * It contains the filename, problem name, messages and a boolean success flag.
 *
 * @author mattias ulbrich
 */
public class Result {

    /**
     * The file which has been visited.
     */
    private final File file;


    /**
     * The name of the probilem which has been visited.
     */
    private final String name;

    /**
     * The a non-null list of messages.
     */
    private final List<String> messages = new ArrayList<String>();

    /**
     * The success flag. <code>true</code> indicates success.
     */
    boolean success;

    /**
     * Instantiates a new result.
     *
     * @param result
     *            the result flag (<code>true</code> is successful)
     * @param file
     *            the visited file
     * @param name
     *            Name of this proof obligation
     * @param messages
     *            the messages
     */
    public Result(boolean result, File file, String name, String... messages) {
       this(result, file, name, Arrays.<String>asList(messages));
    }

    /**
     * Instantiates a new result.
     *
     * @param result
     *            the result flag (<code>true</code> is successful)
     * @param file
     *            the visited file
     * @param name
     *            Name of this proof obligation
     * @param messages
     *            the messages
     */
    public Result(boolean result, File file, String name, List<String> messages) {
        this.success = result;
        this.file = file;
        this.name = name;
        this.messages.addAll(messages);
    }

    /**
     * Gets the success flag.
     *
     * @return the success flag
     */
    public boolean getSuccess() {
        return success;
    }

    /**
     * Gets the file.
     *
     * @return the file
     */
    public String getFile() {
        return file.toString();
    }

    /**
     * Prints this object to a print stream.
     *
     * Prints the file name, followed by "#" and the name of the problem (if not ""), followed
     * by the messages, each on a new line.
     *
     * @param err
     *            the stream to print to.
     */
    public void print(@NonNull PrintStream err) {
        err.print(file);
        if(name.length() > 0) {
            err.println("#" + name);
        }
        err.println(" :");
        for (String m : messages) {
            err.println(m);
        }
    }

}