package de.uka.iti.pseudo.term;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.Util;

/**
 * Representation of the location of inside a program. This is mostly
 * used to provide useful information for the user.
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

    public CodeLocation(int line, final P program) {
        this.index = line;
        this.program = program;
    }

    /**
     * Checks if two locations are equivalent.
     */
    public boolean sameAs(CodeLocation<P> c) {
        return equals(c);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CodeLocation) {
            CodeLocation<?> loc = (CodeLocation<?>) obj;
            return index == loc.index && Util.equalOrNull(program, loc.program);
        }
        return false;
    }

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
            public void visit(LiteralProgramTerm progTerm) throws TermException {
                progTerms.add(progTerm.getCodeLocation());
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
    
    public static Set<CodeLocation<URL>> findSourceCodeLocations(Collection<CodeLocation<Program>> programLocations) {

        Set<CodeLocation<URL>> result = new HashSet<CodeLocation<URL>>();
        
        for (CodeLocation<Program> loc : programLocations) {
            URL sourceFile = loc.getProgram().getSourceFile();
            if(sourceFile != null) {
                Statement stm = loc.getProgram().getStatement(loc.getIndex());
                int line = stm.getSourceLineNumber();
                result.add(new CodeLocation<URL>(line, sourceFile));
            }
        }

        return result;
    }
    
    @Override
    public String toString() {
        return index + "@" + program;
    }
}
