package de.uka.iti.pseudo.justify;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Term;

public class TestRuleProblemExtractor extends TestCaseWithEnv {

    private static Environment loadEnv() {
        try {
            Parser fp = new Parser(TestRuleProblemExtractor.class.getResourceAsStream("extractor.p"));
            ASTFile f = fp.File();
            f.setFilename("*test_internal*");
            EnvironmentMaker em = new EnvironmentMaker(fp, f, "*test_internal*");
            Environment environment = em.getEnvironment();
            environment.setFixed();
            return environment;
        } catch (Exception e) {
            throw new Error(e);
        }
    }
    
    public void testRuleExtracts() throws Exception {
        
        Environment startEnv = loadEnv();
        
        for (Rule rule : startEnv.getAllRules()) {
            String extract = rule.getProperty("extract");
            
            if(extract == null)
                continue;
            
            env = new Environment("wrap", startEnv);
            RuleProblemExtractor rpe = new RuleProblemExtractor(rule, env);
            Term result = rpe.extractProblem();
            
            Term extractTerm;
            try {
                extractTerm = makeTerm(extract);
            } catch(Exception ex) {
                env.dump();
                System.out.println(result);
                throw ex;
            }
            
            if(!result.equals(extractTerm)) {
                rule.dump();
                
                System.out.println(extractTerm);
                System.out.println(result);
                
                PrettyPrint pp = new PrettyPrint(env);
                System.out.println(pp.print(extractTerm));
                System.out.println(pp.print(result));
                
                pp.setTyped(true);               
                System.out.println(pp.print(extractTerm));
                System.out.println(pp.print(result));
            }
            
            assertEquals(result, extractTerm);
        }
        
    }
}
