/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule.meta;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.statement.DefaultStatementVisitor;
import de.uka.iti.pseudo.term.statement.SkipStatement;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.TermUtil;

class RefinementMarkInfoCollector extends DefaultStatementVisitor {

    public static class MarkInfo {
        public final int index;
        public final int literal;
        public final Term couplingInvariant;
        public final Term couplingVar;

        public MarkInfo(int index, int literal, Term couplingInvariant, Term couplingVar) {
            this.index = index;
            this.literal = literal;
            this.couplingInvariant = couplingInvariant;
            this.couplingVar = null; // TODO couplingVar;
        }
    }

    private int cur;
    private final Map<Integer, MarkInfo> collectedInfo =
            new TreeMap<Integer, MarkInfo>();
    private final String skipMarkIndicator;

    private RefinementMarkInfoCollector(@NonNull String skipMarkIndicator) {
        this.skipMarkIndicator = skipMarkIndicator;
    }

    public static Map<Integer, MarkInfo> collectMarkAssignments(Program program,
            String skipMarkerIndicator) throws TermException {
        RefinementMarkInfoCollector collector =
                new RefinementMarkInfoCollector(skipMarkerIndicator);
        program.visitStatements(collector);
        return collector.collectInfo();
    }

    @Override
    protected void visitDefault(Statement statement) {
        cur ++;
    }

    @Override
    public void visit(SkipStatement skipStatement) throws TermException {
        String marker = "";
        List<Term> skipArgs = skipStatement.getSubterms();
        if(skipStatement.countSubterms() > 0) {
            marker = skipArgs.get(0).toString(false);
        }

        if(skipMarkIndicator.equals(marker)) {

            if(skipArgs.size() < 2) {
                throw new TermException("a skip refinement marker needs at least 2 arguments");
            }

            Term literalTerm = skipArgs.get(1);
            int literal = TermUtil.getIntLiteral(literalTerm);
            Term inv = null;
            Term var = null;

            if(skipArgs.size() >= 3) {
                inv = skipArgs.get(2);
                if(!inv.getType().equals(Environment.getBoolType())) {
                    throw new TermException("refinement marker needs boolean invariant as argument");
                }
            }

            if(skipArgs.size() >= 4) {
                var = skipArgs.get(3);
            }

            collectedInfo.put(literal, new MarkInfo(cur, literal, inv, var));

        }
        super.visit(skipStatement);
    }

    private Map<Integer, MarkInfo> collectInfo() throws TermException {

        return collectedInfo;
    }
}
