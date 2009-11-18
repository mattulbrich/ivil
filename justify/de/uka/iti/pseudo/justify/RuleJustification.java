package de.uka.iti.pseudo.justify;

import java.io.File;
import java.io.IOException;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.SchemaCollectorVisitor;
import de.uka.iti.pseudo.util.CommandLine;
import de.uka.iti.pseudo.util.CommandLineException;

/**
 * For a pseudo file containing rules create a proof obligation for every rule
 * which has been tagged as derived.
 */
public class RuleJustification {

	private static final String CMDLINE_HELP = "-help";
	private static final String CMDLINE_TARGET = "-d";
	private static final String CMDLINE_RULE = "-rule";
	
	private String rulenameToFind;
	private File file;
	private String targetDir;

	public RuleJustification(String file, String rule, String targetDir) {
		this.rulenameToFind = rule;
		this.file = new File(file);
		this.targetDir = targetDir;
	}
	
	private void generateObligations() throws ParseException, ASTVisitException, IOException, TermException {
		EnvironmentMaker em = new EnvironmentMaker(new Parser(), file);
		Environment env = em.getEnvironment();
		
		for (Rule rule : env.getLocalRules()) {
			if(rulenameToFind == null || rulenameToFind.equals(rule.getName())) {
				makeProofObligation(env, rule);
			}
		}
	}

	private void makeProofObligation(Environment env, Rule rule) throws IOException, TermException {
		File in = new File(env.getResourceName());
		File out = new File(targetDir, in.getName() + "_" + rule.getName() + ".p");
		
		EnvironmentExporter eex = new EnvironmentExporter(out);
		eex.exportIncludesFrom(env);
		eex.exportDefinitionsFrom(env);
		
		for (Rule r : env.getLocalRules()) {
			if(r == rule)
				break;
			
			eex.exportRule(r);
		}
		
		eex.exportProblem(extractProblemFrom(rule));
		
	}

	// We only support rewrite at the moment.
	// no where clauses are supported
	private Term extractProblemFrom(Rule rule) {
		SchemaCollectorVisitor scv = new SchemaCollectorVisitor();
		scv.collect(rule);
		scv.getSchemaVariables();
		// XXX XXX XXX
		return null;
	}

	/**
	 * @param args
	 * @throws CommandLineException
	 * @throws IOException 
	 * @throws ASTVisitException 
	 * @throws ParseException 
	 * @throws TermException 
	 */
	public static void main(String[] args) throws CommandLineException, ParseException, ASTVisitException, IOException, TermException {
		CommandLine commandLine = makeCommandLine();
		commandLine.parse(args);

		if (commandLine.isSet(CMDLINE_HELP)) {
			commandLine.printUsage(System.out);
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
