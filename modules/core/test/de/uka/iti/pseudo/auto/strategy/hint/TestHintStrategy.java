package de.uka.iti.pseudo.auto.strategy.hint;

import java.io.InputStreamReader;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.auto.strategy.HintStrategy;
import de.uka.iti.pseudo.environment.creation.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.term.Sequent;

public class TestHintStrategy extends TestCaseWithEnv {

    private Sequent problem;

    @Override
    protected void setUp() throws Exception {
        Parser fp = new Parser();
        ASTFile ast = fp.parseFile(new InputStreamReader(getClass().getResourceAsStream("hinttest.p")), "*test*");
        EnvironmentMaker em = new EnvironmentMaker(fp, ast, "none:test");
        env = em.getEnvironment();
        problem = em.getProblemSequent();
    }
    
    
    public void testHintStrategy() throws Exception {
        // make first rule application
        Proof proof = new Proof(problem);
        RuleApplicationMaker ram = new RuleApplicationMaker(env);
        ProofNode root = proof.getRoot();
        ram.setProofNode(root);
        ram.setFindSelector(new TermSelector("S.0"));
        ram.setRule(env.getRule("assertion"));
        ram.matchInstantiations();
        proof.apply(ram, env);

        // create HintStrategy and inform it
        HintStrategy hs = new HintStrategy();
        hs.init(proof, env, null);
        hs.notifyRuleApplication(ram);

        // now the mock rule app finder are installed
        RuleApplication ra = hs.findRuleApplication(root.getChildren().get(0));
        assertEquals("oops", ra.getRule().getName());
        
        ra = hs.findRuleApplication(root.getChildren().get(1));
        assertEquals("oops", ra.getRule().getName());
        
        ra = hs.findRuleApplication(root.getChildren().get(2));
        assertNull(ra);
    }
}
