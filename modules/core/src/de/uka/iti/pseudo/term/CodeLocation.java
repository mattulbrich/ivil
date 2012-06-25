/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import nonnull.NonNull;

import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.Util;

/**
 * Representation of the location of inside a program. This is mostly
 * used to provide useful information for the user.
 *
 * The index may be a sourceline or a statement number.
 *
 * @param <P>
 *            type of the program reference in the location (typically
 *            {@link Program} or {@link URL}
 *
 * @author timm.felden@felden.com
 */
public final class CodeLocation<P> {

    /**
     * The index of this location within the program.
     */
    private final int index;

    /**
     * This object is used as representation of the program.
     */
    private final P program;

    /**
     * Instantiates a new code location.
     *
     * @param index
     *            the index in the program object
     * @param program
     *            the program object to use
     */
    private CodeLocation(@NonNull P program, int index) {
        this.index = index;
        this.program = program;
    }

    /**
     * Create a new code location from a {@link LiteralProgramTerm}.
     *
     * The result has the same index as the argument and refers to the same
     * program.
     *
     * @param progTerm
     *            the program term to take information from
     *
     * @return a freshly created code location
     */
    public static CodeLocation<Program> fromTerm(@NonNull LiteralProgramTerm progTerm) {
        int index = progTerm.getProgramIndex();
        Program program = progTerm.getProgram();
        return new CodeLocation<Program>(program, index);
    }

    /**
     * Checks if two locations are equivalent. This is the case if they point to
     * the same index and resource.
     *
     * @param c
     *            other code location to compare with
     * @return <code>true</code> iff both code locations point to the same spot.
     */
    public boolean equals(@NonNull CodeLocation<?> c) {
        return index == c.index && Util.equalOrNull(program, c.program);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CodeLocation) {
            return equals((CodeLocation<?>)obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return program.hashCode() * 31 + index;
    };

    public P getProgram() {
        return program;
    }

    public int getIndex() {
        return index;
    }

    /**
     * Calculates both native locations in a sequent.
     *
     * The result will contain exactlye those code locations which appear
     * literally in a term in the sequent. The ones embedded in the program are
     * not considered.
     *
     * @param s
     *            the sequent to scan
     *
     * @return a possibly immutable, possibly empty set of program code
     *         locations.
     */
    public static Set<CodeLocation<Program>> findCodeLocations(@NonNull Sequent s) {

        final Set<CodeLocation<Program>> progTerms = new HashSet<CodeLocation<Program>>();

        TermVisitor programFindVisitor = new DefaultTermVisitor.DepthTermVisitor() {
            @Override
            public void visit(LiteralProgramTerm progTerm) throws TermException {
                progTerms.add(new CodeLocation<Program>(progTerm.getProgram(),
                        progTerm.getProgramIndex()));
            }
        };

        try {
            for (Term t : s.getAntecedent()) {
                t.visit(programFindVisitor);
            }

            for (Term t : s.getSuccedent()) {
                t.visit(programFindVisitor);
            }
        } catch (TermException e) {
            // never thrown
            throw new Error(e);
        }

        if (progTerms.isEmpty()) {
            return Collections.emptySet();
        } else {
            return progTerms;
        }
    }

    /**
     * Calculates source code locations in a sequent.
     *
     * The result will contain exactlye those source code locations which appear
     * literally in a term in the sequent. The ones embedded in the program are
     * not considered.
     *
     * If a program term does not have a source line number attributed to it,
     * the location is ignored.
     *
     * @param sequent
     *            the sequent to scan
     *
     * @return a possibly immutable, possibly empty set of URL code
     *         locations.
     */
    public static Set<CodeLocation<URL>> findSourceCodeLocations(Sequent sequent) {
        return findSourceCodeLocations(findCodeLocations(sequent));
    }

    /*
     * extract the set of URL locations from a set of Program locations
     * by querying the Program for linenumbers.
     */
    private static Set<CodeLocation<URL>> findSourceCodeLocations(
            Collection<CodeLocation<Program>> programLocations) {

        Set<CodeLocation<URL>> result = new HashSet<CodeLocation<URL>>();

        for (CodeLocation<Program> loc : programLocations) {
            URL sourceFile = loc.getProgram().getSourceFile();
            if(sourceFile != null) {
                Statement stm = loc.getProgram().getStatement(loc.getIndex());
                int line = stm.getSourceLineNumber();
                if(line != -1) {
                    result.add(new CodeLocation<URL>(sourceFile, line));
                }
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return index + "@" + program;
    }
}
