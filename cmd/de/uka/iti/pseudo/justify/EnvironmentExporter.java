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
package de.uka.iti.pseudo.justify;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.rule.GoalAction.Kind;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.Util;

public class EnvironmentExporter {

	private PrintWriter pw;

	public EnvironmentExporter(File out) throws IOException {
		pw = new PrintWriter(out);
		printHeader(pw);
	}

	private void printHeader(PrintWriter pw2) {
		pw.println("# Created by RuleJustification");
		pw.println("# " + new Date());
		pw.println();
	}

	public void exportIncludes(List<String> includes) {
	    if(includes.size() > 0) {
	        pw.println("include");
	        for (String string : includes) {
                pw.print("  \"");
                pw.print(string);
                pw.println("\"");
            }
	        pw.println();
	    }
	}

	public void exportDefinitionsFrom(Environment env) {
		exportSortsFrom(env);
		exportFunctionsFrom(env);
	}

	private void exportFunctionsFrom(Environment env) {
		Collection<Function> functions = env.getLocalFunctions();
		if(!functions.isEmpty()) {
			for (Function fct : functions) {
				exportFunction(fct);
			}
		}
		
		pw.println();
	}

	private void exportFunction(Function fct) {
		pw.print("function " + fct.getResultType() + " " + fct.getName());
		if(fct.getArity() > 0) {
			pw.print("(" + Util.join(fct.getArgumentTypes(), ", ") + ")");
		}

		if(fct.isAssignable())
			pw.print(" assignable");
		else if(fct.isUnique())
			pw.print(" unique");
			
		pw.println();
	}

	private void exportSortsFrom(Environment env) {
		Collection<Sort> sorts = env.getLocalSorts();
		if(!sorts.isEmpty()) {
			for (Sort sort : sorts) {
				exportSort(sort);
			}
		}	
		
		pw.println();
	}

	private void exportSort(Sort sort) {
		pw.print("sort " + sort.getName());
		if(sort.getArity() > 0) {
			pw.print("('t0");
			for (int i = 1; i < sort.getArity(); i++) {
				pw.print(", t" + i);
			}
			pw.print(")");
		}
		pw.println();
	}

	public void exportRule(Rule r) {
		pw.println("rule " + r.getName());
        
		LocatedTerm findClause = r.getFindClause();
        if(findClause != null) {
            pw.println("  find " + findClause);
        }
        
        List<LocatedTerm> assumptions = r.getAssumptions();
        for (LocatedTerm lt : assumptions) {
            pw.println("  assume " + lt);
        }
        
        List<WhereClause> whereClauses = r.getWhereClauses();
		for (WhereClause wc : whereClauses) {
            pw.println("  where " + wc);
        }
		
        List<GoalAction> goalActions = r.getGoalActions();
		for (GoalAction ga : goalActions) {
            exportGoalAction(ga);
        }
		
		Collection<String> properties = r.getDefinedProperties();
		if(!properties.isEmpty()) {
			pw.println("  tags");
			for (String prop : properties) {
				String value = r.getProperty(prop);
				assert value != null;
				pw.println("    " + prop + " " + value);
			}
		}
		
		pw.println();
	}

	private void exportGoalAction(GoalAction ga) {
		
		Kind kind = ga.getKind();
		switch (kind) {
		case CLOSE:
			pw.println("  closegoal");
			break;
		case COPY:
			pw.println("  samegoal");
			break;
		case NEW:
			pw.println("  newgoal");
			break;
		default:
			throw new Error("type distinction incomplete: Kind");
		}
		
		if(ga.isRemoveOriginalTerm())
			pw.println("    remove");
		
		Term replaceWith = ga.getReplaceWith();
		if(replaceWith != null)
            pw.println("    replace " + replaceWith);

        for (Term t : ga.getAddAntecedent()) {
            pw.println("    add " + t + " |-");
        }
        
        for (Term t : ga.getAddSuccedent()) {
            pw.println("    add |- " + t);
        }
	}

	public void exportProblem(Term formula) throws TermException {
		if(!Environment.getBoolType().equals(formula.getType()))
			throw new TermException("Only boolean formulas can be exported as problems");
		
		pw.println("problem");
		pw.println("  " + formula);
	}

    public void close() {
        pw.close();
    }

}