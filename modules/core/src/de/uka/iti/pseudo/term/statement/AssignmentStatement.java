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
package de.uka.iti.pseudo.term.statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Update;
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
 * @see Update
 * @see Assignment
 */
public class AssignmentStatement extends Statement {
    
    private Assignment[] assignments;
    private @Nullable String schemaIdentifier; 
    
    //@ invariant assignments.length == 0 <==> schemaIdentifier != null 

    // TODO DOC
    public AssignmentStatement(int sourceLineNumber, List<Assignment> assignments) throws TermException {
        super(sourceLineNumber, toTermArray(assignments));
        this.schemaIdentifier = null;
        this.assignments = new Assignment[assignments.size()];
        assignments.toArray(this.assignments);
    }

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

    public boolean isSchematic() {
        return schemaIdentifier != null;
    }
    
    public String toString(boolean typed) {
        if(isSchematic()) {
            return schemaIdentifier;
        } else {
            return Util.join(assignments, " || ");
        }
    }
    
    public void visit(StatementVisitor visitor) throws TermException {
        visitor.visit(this);
    }
    
    public List<Assignment> getAssignments() {
        assert !isSchematic();
        return Util.readOnlyArrayList(assignments);
    }

    private static Term[] toTermArray(List<Assignment> assignments) {
        Term[] result = new Term[assignments.size() * 2];
        int i = 0;
        for (Assignment assignment : assignments) {
            result[i++] = assignment.getTarget();
            result[i++] = assignment.getValue();
        }
        return result;
    }

    public List<Function> getAssignedVars() {
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

    public String getSchemaIdentifier() {
        assert isSchematic();
        return schemaIdentifier;
    }
    
}