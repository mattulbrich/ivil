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
package de.uka.iti.pseudo.term;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import nonnull.DeepNonNull;
import nonnull.NonNull;
import checkers.nullness.quals.Nullable;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.term.creation.ToplevelCheckVisitor;
import de.uka.iti.pseudo.util.Util;

/**
 * The Class Sequent is used to model immutable logic sequents. A sequent consists of lists of terms. Any term
 * in these lists needs to be of boolean type.
 */
public class Sequent {

    private static final CodeLocation[] NO_CODE_LOCATIONS = new CodeLocation[0];

    /**
     * The antecedent.
     */
    private Term[] antecedent;

    /**
     * The succedent.
     */
    private Term[] succedent;

    /**
     * Native code locations, i.e. ivil byte code oder BoogiePL code
     */
    private CodeLocation /*@Nullable*/ [] nativeCodeLocations = null;

    /**
     * Source code locations, i.e. any code that was used to generate the
     * corresponding native code. Source code will be empty if no corresponding
     * source code can be found.
     */
    private CodeLocation /*@Nullable*/ [] sourceCodeLocations = null;

    /**
     * Instantiates a new sequent.
     *
     * The given arrays are not stored in the sequent themselves but are
     * (shallow-) copied first. You can savely change them after the constructor
     * call.
     *
     * @param antecedent
     *            the terms in the antecedent
     * @param succedent
     *            the terms in the succedent
     *
     * @throws TermException
     *             if a term is not suitable for toplevel usage.
     */
    public Sequent(@NonNull Term[] antecedent, @NonNull Term[] succedent) throws TermException {
        this(Arrays.asList(antecedent), Arrays.asList(succedent));
    }

    /**
     * Instantiates a new sequent.
     *
     * The given lists are not stored in the sequent themselves but are (shallow-) copied first. You can savely change them
     * after the constructor call.
     *
     * @param antecedent
     *            the terms in the antecedent
     * @param succedent
     *            the terms in the succedent
     *
     * @throws TermException
     *             if a term is not suitable for toplevel usage.
     */
    public Sequent(List<Term> antecedent, List<Term> succedent) throws TermException {
        this.antecedent = Util.listToArray(antecedent, Term.class);
        this.succedent = Util.listToArray(succedent, Term.class);
        check();
    }

    /*
     * Calculates both native and source code locations.
     */
    private void calculateCodeLocations() {
        assert nativeCodeLocations == null && sourceCodeLocations == null;

        final List<LiteralProgramTerm> progTerms = new LinkedList<LiteralProgramTerm>();

        TermVisitor programFindVisitor = new DefaultTermVisitor.DepthTermVisitor() {
            public void visit(LiteralProgramTerm progTerm) throws TermException {
                progTerms.add(progTerm);
            }
        };

        try {
            for (Term t : antecedent) {
                t.visit(programFindVisitor);
            }

            for (Term t : succedent) {
                t.visit(programFindVisitor);
            }
        } catch (TermException e) {
            // never thrown
            throw new Error(e);
        }

        if (progTerms.isEmpty()) {
            nativeCodeLocations = sourceCodeLocations = NO_CODE_LOCATIONS;
        } else {
            Set<CodeLocation> nativ = new HashSet<CodeLocation>();
            Set<CodeLocation> source = new HashSet<CodeLocation>();

            for (LiteralProgramTerm t : progTerms) {
                nativ.add(new CodeLocation(t.getProgramIndex(), t.getProgram()));
                URL sourceFile = t.getProgram().getSourceFile();
                if (null != sourceFile)
                    source.add(new CodeLocation(t.getStatement().getSourceLineNumber(), sourceFile));
            }

            nativeCodeLocations = new CodeLocation[nativ.size()];
            nativ.toArray(nativeCodeLocations);

            sourceCodeLocations = new CodeLocation[source.size()];
            source.toArray(sourceCodeLocations);
        }
    }

    /*
     * Check whether all terms are usable as formulas: No schema variables, boolean type, etc.
     * See TopLevelVisitor for details.
     */
    private void check() throws TermException {
        ToplevelCheckVisitor checker = new ToplevelCheckVisitor();
        for (Term t : antecedent) {
            t.visit(checker);
        }
        for (Term t : succedent) {
            t.visit(checker);
        }
    }

    /**
     * Gets an immutable view on the terms in the antecedent.
     *
     * @return the terms in the antecedent
     */
    public @NonNull List<Term> getAntecedent() {
        return Util.readOnlyArrayList(antecedent);
    }

    /**
     * Gets an immutable view on the terms in the succedent.
     *
     * @return the terms in the succedent
     */
    public @NonNull List<Term> getSuccedent() {
        return Util.readOnlyArrayList(succedent);
    }

    /**
     * Gets an immutable view on the code locations appearing in this sequent.
     *
     * The code locations are calculated lazily upon demand and then cached.
     *
     * @return an immutable list of code locations
     */
    public @DeepNonNull List<CodeLocation> getNativeCodeLocations() {
        if (null == nativeCodeLocations)
            calculateCodeLocations();

        assert nativeCodeLocations != null : "nullness";
        
        return Util.readOnlyArrayList(nativeCodeLocations);
    }

    /**
     * Gets an immutable view on the source code locations for the program
     * formulas appearing in this sequent.
     *
     * The code locations are calculated lazily upon demand and then cached.
     *
     * The result of this method might be empty even if
     * {@link #getNativeCodeLocations()} returns a non-empty collection.
     *
     * @return an immutable list of code locations
     */
    public @DeepNonNull List<CodeLocation> getSourceCodeLocations() {
        if (null == sourceCodeLocations)
            calculateCodeLocations();

        assert sourceCodeLocations != null : "nullness";
        
        return Util.readOnlyArrayList(sourceCodeLocations);
    }

    /**
     * A sequent is represented as a string as two comma separated lists of term
     * strings separated by <code>|-</code>. The antecedent appears on the
     * left of the separator and the succedent on the right, like in
     *
     * <pre>
     *    ante1, ante2, ..., anteN |- succ1, succ2, ..., succM
     * </pre>
     *
     * Both, antecedent and succedent may be the empty list and the string
     * therefore empty.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Term t : antecedent) {
            sb.append(t).append(" ");
        }
        sb.append("|-");
        for (Term t : succedent) {
            sb.append(" ").append(t);
        }
        return sb.toString();
    }

    /**
     * A sequent is equal to another object if it is a sequent, too, and the
     * terms in antecedent and succedent are pairwise equal.
     */
    @Override
    public boolean equals(@Nullable Object obj) {

        if (obj instanceof Sequent) {
            Sequent other = (Sequent) obj;
            return Arrays.equals(antecedent, other.antecedent) &&
            Arrays.equals(succedent, other.succedent);
        }
        return false;
    }

    /**
     * The hash code of a sequent is calculated by the hash code of
     * the terms in antecedent and succedent.
     */
    @Override public int hashCode() {
        return Arrays.hashCode(antecedent) + 31 * Arrays.hashCode(succedent);
    }

}
