/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term.statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nonnull.Nullable;
import checkers.nullness.quals.AssertNonNullIfFalse;
import checkers.nullness.quals.AssertNonNullIfTrue;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.Util;

/**
 * Captures a parallel assignments <code>v_1:=t_1 || ... || v_k := t_k</code> as
 * it can appear in assignment statements in programs.
 *
 * <p>
 * The parts of the parallel assignments are stored as a list of
 * {@link Assignment} objects.
 *
 * @see de.uka.iti.pseudo.term.Update
 * @see Assignment
 */
public final class AssignmentStatement extends Statement {

    private final @Nullable Assignment[] assignments;
    private @Nullable final String schemaIdentifier;

    //@ invariant assignments == null <==> schemaIdentifier != null

    /**
     * Create a new concrete assignment statement.
     *
     * It takes a list of assignments which are embedded in the statement.
     *
     * @param sourceLineNumber the line number in the corresponding sources
     * @param assignments the assignments which are embedded
     * @throws TermException not thrown.
     */
    public AssignmentStatement(int sourceLineNumber, List<Assignment> assignments)
            throws TermException {
        super(sourceLineNumber, toTermArray(assignments));
        this.schemaIdentifier = null;
        this.assignments = new Assignment[assignments.size()];
        assignments.toArray(this.assignments);
    }

    /**
     * Create a new schematic assignment statement.
     *
     * It takes a schematic identifier as argument.
     *
     * @param sourceLineNumber the line number in the corresponding sources
     * @param identifier the schematic identifier for the assignments
     */
    public AssignmentStatement(int sourceLineNumber, String identifier) {
        super(sourceLineNumber);
        this.assignments = null;
        this.schemaIdentifier = identifier;
    }

    /**
     * Convenience constructor for a single assignment.
     *
     * @param sourceLineNumber
     *            the line in the sources at which the statement appears.
     * @param target
     *            the left hand side of the assignment
     * @param value
     *            the right hand side of the assignment
     * @throws TermException
     *             if the assignment cannot be checked.
     */
    public AssignmentStatement(int sourceLineNumber, Term target, Term value) throws TermException {
        this(sourceLineNumber, Arrays.asList(new Assignment(target, value)));

    }

    /*
     * create a term array (as needed by the super constructor) from a list of
     * assignments.
     */
    private static Term[] toTermArray(List<Assignment> assignments) {
        Term[] result = new Term[assignments.size() * 2];
        int i = 0;
        for (Assignment assignment : assignments) {
            result[i++] = assignment.getTarget();
            result[i++] = assignment.getValue();
        }
        return result;
    }


    /**
     * Check whether this statement is a schematic assignment or not.
     *
     * @return <code>true</code> iff this statement is schematic or concrete.
     */
    @AssertNonNullIfTrue("schemaIdentifier")
    @AssertNonNullIfFalse("assignments")
    public boolean isSchematic() {
        return schemaIdentifier != null;
    }

    @Override
    public String toString(boolean typed) {
        if(isSchematic()) {
            return schemaIdentifier;
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < assignments.length; i++) {
                if (i != 0) {
                    sb.append(" || ").append(assignments[i].toString(typed));
                } else {
                    sb.append(assignments[i].toString(typed));
                }
            }

            return sb.toString();
        }
    }

    @Override
    public void accept(StatementVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    /**
     * Gets the assignments embedded in this statement.
     *
     * Call only if {@link #isSchematic()} returns <code>false</code>.
     *
     * @return an immutable view to the assignments of this object
     */
    public List<Assignment> getAssignments() {
        assert !isSchematic();
        return Util.readOnlyArrayList(assignments);
    }



    /**
     * Gets the list of assigned program variables.
     *
     * Call only if {@link #isSchematic()} returns <code>false</code>.
     *
     * @return a freshly created list of program variables
     */
    public List<Function> getAssignedVars() {

        assert !isSchematic() :
            "nullness: This method must not be called on schematic assignment statements";

        List<Function> result = new ArrayList<Function>();
        for (Assignment ass : assignments) {
            Term target = ass.getTarget();
            if(target instanceof Application) {
                Application app = (Application) target;
                result.add(app.getFunction());
            } else {
                Log.log(Log.WARNING, "There should only be application assignments here.");
            }
        }
        return result;
    }

    /**
     * Gets the schema identifier of this schematic assignment statement.
     *
     * Call only if {@link #isSchematic()} returns <code>true</code>.
     *
     * @return the schema identifier
     */
    public String getSchemaIdentifier() {
        assert isSchematic();
        return schemaIdentifier;
    }

//    @Override @Deprecated
//    public Statement getWithReplacedSubterms(Term[] newSubterms) throws TermException {
//        if (newSubterms.length != getSubterms().size()) {
//            throw new TermException("It is required to supply the same amount of subterms; was: "
//                    +getSubterms().size() + " is: "+newSubterms.length);
//        }
//
//        int i = 0;
//        while (newSubterms[i].equals(getSubterms().get(i))) {
//            i++;
//            if (i == newSubterms.length) {
//                return this;
//            }
//        }
//
//        return new AssignmentStatement(getSourceLineNumber(), fromTermArray(newSubterms));
//    }
//
//    private static List<Assignment> fromTermArray(Term[] assignments) throws TermException {
//        assert assignments.length % 2 == 0 : "requirement: assignments must have even length";
//
//        ArrayList<Assignment> result = new ArrayList<Assignment>(assignments.length / 2);
//
//        for (int i = 0; i < assignments.length; i += 2) {
//            result.add(new Assignment(assignments[i], assignments[i + 1]));
//        }
//
//        return result;
//    }

}