/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */

package de.uka.iti.pseudo.environment;

import nonnull.Nullable;
import checkers.nullness.quals.AssertNonNullIfTrue;

/**
 * Exceptions of class EnvironmentException are mainly thrown
 * by classes in the package de.uka.iti.pseudo.environment to
 * indicate that something related to environment creation or
 * usage has gone wrong.
 */
@SuppressWarnings("serial")
public class EnvironmentException extends Exception {

    // Checkstyle: OFF MutableException
    // - This exception is build poco a poco

    private @Nullable String resource;
    private int beginLine = -1;
    private int endLine = -1;
    private int beginColumn = -1;
    private int endColumn = -1;

    /**
     * Instantiates a new environment exception with a message.
     *
     * <p> The location fields of this exception remain unassigned.
     *
     * @param message
     *            the message
     */
    public EnvironmentException(String message) {
        super(message);
    }

    /**
     * Instantiates a new environment exception.
     *
     * <p> The location fields of this exception remain unassigned.
     */
    public EnvironmentException() {
        super();
    }

    /**
     * Instantiates a new environment exception.
     *
     * <p> The location fields of this exception remain unassigned.
     *
     * @param message
     *            the message
     * @param cause
     *            the cause of this exception
     */
    public EnvironmentException(String message, Throwable cause) {
        super(message, cause);
    }

    // do not include the cause's classname
    /**
     * Instantiates a new environment exception.
     *
     * <p>
     * The location fields of this exception remain unassigned.
     *
     * <p>
     * The message of the causing exception is used as message, dropping the
     * classname
     *
     * @param cause
     *            the cause
     */
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

    /**
     * Checks for error information.
     *
     * Information present means that there is detailled report on the location
     * of the exception (all parameters set).
     *
     * @return true, if this exception has detailled information
     */
    @AssertNonNullIfTrue("getResource()")
    public boolean hasErrorInformation() {
        return beginColumn != -1 && beginLine != -1 &&
               endLine != -1 && endColumn != -1 &&
               resource != null;
    }

}
