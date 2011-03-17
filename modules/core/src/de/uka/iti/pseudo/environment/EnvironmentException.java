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

package de.uka.iti.pseudo.environment;

import checkers.nullness.quals.AssertNonNullIfTrue;
import nonnull.Nullable;

/**
 * Exceptions of class EnvironmentException are mainly thrown 
 * by classes in the package de.uka.iti.pseudo.environment to 
 * indicate that something related to environment creation or
 * usage has gone wrong.
 */
@SuppressWarnings("serial")
public class EnvironmentException extends Exception {

    private @Nullable String resource;
    private int beginLine = -1;
    private int endLine = -1;
    private int beginColumn = -1;
    private int endColumn = -1;

    public EnvironmentException(String message) {
        super(message);
    }

    public EnvironmentException() {
        super();
    }

    public EnvironmentException(String message, Throwable cause) {
        super(message, cause);
    }

    // do not include the cause's classname
    public EnvironmentException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    /**
     * @return the resource
     */
    public @Nullable String getResource() {
        return resource;
    }

    /**
     * @param resource the resource to set
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * @return the beginLine
     */
    public int getBeginLine() {
        return beginLine;
    }

    /**
     * @param beginLine the beginLine to set
     */
    public void setBeginLine(int beginLine) {
        this.beginLine = beginLine;
    }

    /**
     * @return the endLine
     */
    public int getEndLine() {
        return endLine;
    }

    /**
     * @param endLine the endLine to set
     */
    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    /**
     * @return the beginColumn
     */
    public int getBeginColumn() {
        return beginColumn;
    }

    /**
     * @param beginColumn the beginColumn to set
     */
    public void setBeginColumn(int beginColumn) {
        this.beginColumn = beginColumn;
    }

    /**
     * @return the endColumn
     */
    public int getEndColumn() {
        return endColumn;
    }

    /**
     * @param endColumn the endColumn to set
     */
    public void setEndColumn(int endColumn) {
        this.endColumn = endColumn;
    }

    @AssertNonNullIfTrue("getResource()")
    public boolean hasErrorInformation() {
        return beginColumn != -1 && beginLine != -1 &&
               endLine != -1 && endColumn != -1 &&
               resource != null;
    }

}
