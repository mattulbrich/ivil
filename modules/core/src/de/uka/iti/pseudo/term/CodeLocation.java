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
    public CodeLocation(@NonNull P program, int index) {
        this.index = index;
        this.program = program;
    }

    // TODO DOC
    public static CodeLocation<Program> fromTerm(LiteralProgramTerm progTerm) {
        int index = progTerm.getProgramIndex();
        Program program = progTerm.getProgram();
        return new CodeLocation<Program>(program, index);
    }

    /**
     * Checks if two locations are equivalent.
     */
    public boolean equals(CodeLocation<?> c) {
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
        return program.hashCode() * 47 + index;
    };

    public P getProgram() {
        return program;
    }

    public int getIndex() {
        return index;
    }
    
    /*
     * Calculates both native and source code locations.
     */
    public static Set<CodeLocation<Program>> findCodeLocations(Sequent s) {

        final Set<CodeLocation<Program>> progTerms = new HashSet<CodeLocation<Program>>();

        TermVisitor programFindVisitor = new DefaultTermVisitor.DepthTermVisitor() {
            @Override
            public void visit(LiteralProgramTerm progTerm) throws TermException {
                progTerms.add(new CodeLocation<Program>(progTerm.getProgram(), progTerm.getProgramIndex()));
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
    
    public static Set<CodeLocation<URL>> findSourceCodeLocations(Sequent sequent) {
        return findSourceCodeLocations(findCodeLocations(sequent));
    }
    
    public static Set<CodeLocation<URL>> findSourceCodeLocations(Collection<CodeLocation<Program>> programLocations) {

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
