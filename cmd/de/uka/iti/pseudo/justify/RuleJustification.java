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
import java.util.Collection;
import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.RuleTagConstants;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.creation.TermFactory;
import de.uka.iti.pseudo.util.CommandLine;
import de.uka.iti.pseudo.util.CommandLineException;

/**
 * For a ivil file containing rules create a proof obligation for every rule
 * which has been tagged as derived.
 */
public class RuleJustification {

	private static final String CMDLINE_HELP = "-help";
	private static final String CMDLINE_TARGET = "-d";
	private static final String CMDLINE_RULE = "-rule";
	
	private String rulenameToFind;
	private File file;
	private String targetDir;
    private Environment env;
    private TermFactory tf;
    private List<String> imports;

	public RuleJustification(String file, String rule, String targetDir) {
		this.rulenameToFind = rule;
		this.file = new File(file);
		this.targetDir = targetDir;
	}
	
	public void generateObligations() throws ParseException, ASTVisitException, IOException, TermException, EnvironmentException, RuleException {
	    EnvironmentMaker em = new EnvironmentMaker(new Parser(), file);
	    imports = em.getImportedFilenames();
        env = em.getEnvironment();
        tf = new TermFactory(env);
		
		for (Rule rule : env.getLocalRules()) {
			if(rulenameToFind == null || rulenameToFind.equals(rule.getName())) {
			    if(rule.getProperty(RuleTagConstants.KEY_DERIVED_RULE) != null) {
			        makeProofObligation(rule);
			    }
			}
		}
	}

	private void makeProofObligation(Rule rule)
            throws IOException, TermException, EnvironmentException, RuleException {
        File in = new File(env.getResourceName());
        File out = new File(targetDir, in.getName() + "_" + rule.getName() + ".p");

        EnvironmentExporter eex = new EnvironmentExporter(out);
        eex.exportIncludes(imports);
        eex.exportDefinitionsFrom(env);

        for (Rule r : env.getLocalRules()) {
            if (r == rule)
                break;

            eex.exportRule(r);
        }

        env.setFixed();
        Environment wrapEnv = new Environment("wrapping_for_justification", env);
        RuleProblemExtractor rpe = new RuleProblemExtractor(rule, wrapEnv);
        Term instantiatedProblem = rpe.extractProblem();

        eex.exportDefinitionsFrom(wrapEnv);
        
        eex.exportProblem(instantiatedProblem);
        eex.close();
    }

//		LocatedTerm findClause = rule.getFindClause();
//		
//        if (findClause == null || findClause.getMatchingLocation() != MatchingLocation.BOTH) {
//            throw new EnvironmentException("We only support rewrite rules at the moment");
//        }
//        
//        if (!rule.getAssumptions().isEmpty()) {
//            throw new EnvironmentException("We only support rewrite rules at the moment");
//        }
//        
//        List<GoalAction> goals = rule.getGoalActions();
//        if (goals.size() != 1) {
//            throw new EnvironmentException("We only support rewrite rules at the moment");
//        }
//        
//        GoalAction goal = goals.get(0);
//        if(goal.getKind() != Kind.COPY) {
//            throw new EnvironmentException("We only support rewrite rules at the moment");
//        }
//        
//        if(!goal.getAddAntecedent().isEmpty() || !goal.getAddSuccedent().isEmpty() || goal.getReplaceWith() == null) {
//            throw new EnvironmentException("We only support rewrite rules at the moment");
//        }
//        
//        Term find = findClause.getTerm();
//        Term replaceWith = goal.getReplaceWith();
//            
//        SchemaVarToVarVisitor svv = new SchemaVarToVarVisitor();
//        find.visit(svv);
//        Term findVar = svv.getResultingTerm();
//        
//        replaceWith.visit(svv);
//        Term replaceWithVar = svv.getResultingTerm();
//
//        return universalClosure(svv.getVariables(), tf.eq(findVar, replaceWithVar));
//        
//	}

	private Term universalClosure(Collection<Variable> variables, Term term) throws TermException {
	    for (Variable variable : variables) {
            term = tf.forall(variable, term);
        }
	    return term;
    }

    /**
	 * @param args
	 * @throws CommandLineException
	 * @throws IOException 
	 * @throws ASTVisitException 
	 * @throws ParseException 
	 * @throws TermException 
	 * @throws EnvironmentException 
     * @throws RuleException 
	 */
	public static void main(String[] args) throws CommandLineException, ParseException, ASTVisitException, IOException, TermException, EnvironmentException, RuleException {
		CommandLine commandLine = makeCommandLine();
		commandLine.parse(args);

		if (commandLine.isSet(CMDLINE_HELP) || args.length == 0) {
			commandLine.printUsage(System.out);
			System.out.println("<files>  .p file to scan");
			System.exit(0);
		}
		
		String rule = commandLine.getString(CMDLINE_RULE, null); 
		String targetDir = commandLine.getString(CMDLINE_TARGET, ".");
		
		for (String arg : commandLine.getArguments()) {
			RuleJustification rj = new RuleJustification(arg, rule, targetDir);
			rj.generateObligations();
		}
		
	}

	

	private static CommandLine makeCommandLine() {
		CommandLine cl = new CommandLine();
		cl.addOption(CMDLINE_HELP, null, "Print usage");
		cl.addOption(CMDLINE_RULE, "rule",
				"Generate justification obligation for only that rule");
		cl.addOption(CMDLINE_TARGET, "dir", "Target directory");
		return cl;
	}

}
