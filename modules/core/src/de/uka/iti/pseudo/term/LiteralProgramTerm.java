/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.term.statement.Statement;

/**
 * The class LiteralProgramTerm describes basic formula entities which refer to
 * a particular program pointer position within a particular program.
 * 
 * The referenced program must be defined in the currently valid environment.
 * The index must be a non-negative number, but may exceed the index range of
 * the program.
 */
public final class LiteralProgramTerm extends ProgramTerm {

    // TODO the two following fields look as if they should be replaced by a
    // CodeLocation (TF)
    /**
     * The statement counter index into the program
     */
    private int programIndex;

    /**
     * The program to index into
     */
    private Program program;

    /**
     * Instantiates a new literal program term for an index into a program.
     * 
     * <p>
     * The constructor is not visible. Use the {@code getInst} methods to
     * get/create an object of this Class.
     * 
     * @param programIndex
     *            a non-negative number
     * @param terminating
     *            the termination state of the modality
     * @param program
     *            the referenced program
     * @param formula
     *            the formula to be evaluated in the post states of the program
     *            execution.
     * 
     * @throws TermException
     *             if parameters not well-defined
     */
    private LiteralProgramTerm(int programIndex, boolean terminating,
            @NonNull Program program, @NonNull Term formula) throws TermException {
        super(new Term[] { formula }, terminating);
        this.program = program;
        this.programIndex = programIndex;
        if (programIndex < 0)
            throw new TermException(
                    "Illegally formated literal program index: " + programIndex);
    }

    /**
     * Gets an application term.
     * 
     * If a term with the given parameters already exists in the system, a
     * reference to it is returned instead of a freshly created one. If not, a
     * new instance is created.
     * 
     * @param programIndex
     *            a non-negative number
     * @param terminating
     *            the termination state of the modality
     * @param program
     *            the referenced program
     * @param formula
     *            the formula to be evaluated in the post states of the program
     *            execution.
     * 
     * @throws TermException
     *             if parameters not well-defined
     * @return a term with the given parameters. Not necessarily freshly
     *         created.
     */
    public static @NonNull
    LiteralProgramTerm getInst(int programIndex, boolean terminating,
            @NonNull Program program, @NonNull Term formula) throws TermException {
        return (LiteralProgramTerm) new LiteralProgramTerm(programIndex,
                terminating, program, formula).intern();
    }

    /**
     * Gets an literal program term.
     * 
     * If a term with the given parameters already exists in the system, a
     * reference to it is returned instead of a freshly created one. If not, a
     * new instance is created.
     * 
     * @param index
     *            program index of the new program term
     * @param original
     *            the original program term
     * @throws TermException
     *             if the index is invalid
     * @return a term with the given parameters. Not necessarily freshly
     *         created.
     * 
     */
    public static @NonNull
    LiteralProgramTerm getInst(int programIndex,
            @NonNull LiteralProgramTerm original) throws TermException {
        return (LiteralProgramTerm) new LiteralProgramTerm(programIndex,
                original.isTerminating(), original.getProgram(), 
                original.getSuffixTerm()).intern();
    }

//    /**
//     * create a new literal program term which is a copy of the original program
//     * term with the exception of the programIndex which differs.
//     * 
//     * @param index
//     *            program index of the new program term
//     * @param original
//     *            the original program term
//     * @throws TermException
//     *             if the index is invalid
//     */
//    public LiteralProgramTerm(int index, @NonNull LiteralProgramTerm original)
//            throws TermException {
//        super(original.isTerminating());
//        this.program = original.program;
//        programIndex = index;
//
//        if (programIndex < 0)
//            throw new TermException(
//                    "Illegally formated literal program index: " + index);
//
//    }

    /**
     * {@inheritDoc}
     * 
     * A literal program prints the index as number and the referenced program
     * by its identifier in the modality brackets.
     */
    protected String getContentString(boolean typed) {
        return programIndex + ";" + program;
    }

    /**
     * Two literal terms are equal if and only if they refer to the same program
     * and to the same index within that program and have the same termination
     * status.
     * 
     * This object is not equal to any object which is not a literal program
     * term.
     * 
     * @param object
     *            Object to compare with
     * @return true iff object and this are identical literal program terms.
     */
    public boolean equals(@Nullable Object object) {
        if (object instanceof LiteralProgramTerm) {
            LiteralProgramTerm prog = (LiteralProgramTerm) object;
            return programIndex == prog.programIndex && program == prog.program
                    && isTerminating() == prog.isTerminating();
        }
        return false;
    }

    public void visit(TermVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    /**
     * Gets the referenced index of this term.
     * 
     * @return the program index
     */
    public int getProgramIndex() {
        return programIndex;
    }

    /**
     * Gets the statement to which the index refers in the referenced program.
     * 
     * If the index is outside the bounds of the program, a default statement
     * (for instance "end true") is returned
     * 
     * @return the statement in the referenced program at the referenced index.
     * 
     */
    public Statement getStatement() {
        int programIndex = getProgramIndex();
        return program.getStatement(programIndex);
    }

    /**
     * Delivers the program to which this instance refers.
     * 
     * @return the referenced program
     */
    public Program getProgram() {
        return program;
    }
    
}
