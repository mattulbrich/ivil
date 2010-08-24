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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class Result.
 * @author mattias ulbrich
 */
public class Result {
    
    /**
     * The file.
     */
    private File file;
    
    /**
     * The messages.
     */
    private List<String> messages = new ArrayList<String>();
    
    /**
     * The success.
     */
    boolean success;
    
    /**
     * Instantiates a new result.
     * 
     * @param result
     *            the result
     * @param file
     *            the file
     * @param messages
     *            the messages
     */
    public Result(boolean result, File file, String... messages) {
        this.success = result;
        this.file = file;
        this.messages.addAll(Arrays.asList(messages));
    }

    /**
     * Instantiates a new result.
     * 
     * @param result
     *            the result
     * @param file
     *            the file
     * @param messages
     *            the messages
     */
    public Result(boolean result, File file, ArrayList<String> messages) {
        this.success = result;
        this.file = file;
        this.messages.addAll(messages);
    }

    /**
     * Gets the success.
     * 
     * @return the success
     */
    public boolean getSuccess() {
        // TODO Implement Result.getSuccess
        return false;
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
     * Prints the.
     * 
     * @param err
     *            the err
     */
    public void print(PrintStream err) {
        err.println(file + " :");
        for (String m : messages) {
            err.println(m);
        }
    }
    
}