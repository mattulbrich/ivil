package de.uka.iti.pseudo.auto.strategy.hint;

import java.util.Arrays;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;

public class TestHints extends TestCaseWithEnv {

    private Proof makeProofAndApplyHint(Sequent s,
            ProofHint proofHint, String... arguments) throws Exception {

        Proof proof = new Proof(s);
        HintRuleAppFinder finder = proofHint.createRuleAppFinder(env, Arrays.asList(arguments));
        ProofNode root = proof.getRoot();
        ProofNode node = root;
        RuleApplication ruleApp = finder.findRuleApplication(node, root);
        while(ruleApp != null) {
            proof.apply(ruleApp, env);
            // we continue execution only on first child
            node = node.getChildren().get(0);
            ruleApp = finder.findRuleApplication(node, root);
        }
        return proof;
    }

    // make term "bf(i)" ...
    private Term t(int i) throws Exception {
        return makeTerm("bf(" + i + ")");
    }

    public void testFocusHint1() throws Exception {
        Term[] ante = { t(1), t(2) };
        Term[] succ = { t(3), t(4) };
        Sequent s = new Sequent(ante, succ);

        Proof p = makeProofAndApplyHint(s, new FocusProofHint(), "focus");
        assertEquals(1, p.getOpenGoals().size());
        ProofNode goal = p.getOpenGoals().get(0);

        Term[] expAnte = {};
        Term[] expSucc = { t(3) };

        assertEquals(new Sequent(expAnte, expSucc), goal.getSequent());
    }

    public void testFocusHint2() throws Exception {
        Term[] ante = { t(1), t(2) };
        Term[] succ = { t(3), t(4) };
        Sequent s = new Sequent(ante, succ);

        Proof p = makeProofAndApplyHint(s, new FocusProofHint(), "focus", "A.1");
        assertEquals(1, p.getOpenGoals().size());
        ProofNode goal = p.getOpenGoals().get(0);

        Term[] expAnte = { t(2) };
        Term[] expSucc = { };

        assertEquals(new Sequent(expAnte, expSucc), goal.getSequent());
    }

    public void testPickHint1() throws Exception {
        Term[] ante = { t(0), t(1), t(2) };
        Term[] succ = { t(3), t(4), t(5) };
        Sequent s = new Sequent(ante, succ);

        Proof p = makeProofAndApplyHint(s, new PickProofHint(), "pick", "S.1", "S.0", "A.1");
        assertEquals(1, p.getOpenGoals().size());
        ProofNode goal = p.getOpenGoals().get(0);

        Term[] expAnte = { t(1) };
        Term[] expSucc = { t(3), t(4) };

        assertEquals(new Sequent(expAnte, expSucc), goal.getSequent());
    }

    public void testRuleHint() throws Exception {
        Term[] ante = { t(0), t(1) };
        Term[] succ = { makeTerm("b1 | b2"), t(3), t(4) };
        Sequent s = new Sequent(ante, succ);

        Proof p = makeProofAndApplyHint(s, new RuleProofHint(), "rule", "or_right");
        assertEquals(1, p.getOpenGoals().size());
        ProofNode goal = p.getOpenGoals().get(0);

        Term[] expAnte = { t(0), t(1) };
        Term[] expSucc = { makeTerm("b1"), t(3), t(4), makeTerm("b2") };

        assertEquals(new Sequent(expAnte, expSucc), goal.getSequent());
    }

    public void testCutHint() throws Exception {
        Term[] ante = { t(0), t(1) };
        Term[] succ = { t(3), t(4) };
        Sequent s = new Sequent(ante, succ);

        Proof p = makeProofAndApplyHint(s, new CutProofHint(), "cut", " true ");
        assertEquals(2, p.getOpenGoals().size());
        {
            ProofNode goal = p.getOpenGoals().get(0);
            Term[] expAnte = { t(0), t(1), makeTerm("true"), };
            Term[] expSucc = { t(3), t(4) };
            assertEquals(new Sequent(expAnte, expSucc), goal.getSequent());
        }
        {
            ProofNode goal = p.getOpenGoals().get(1);
            Term[] expAnte = { t(0), t(1) };
            Term[] expSucc = { t(3), t(4), makeTerm("true") };
            assertEquals(new Sequent(expAnte, expSucc), goal.getSequent());
        }
    }

    // factorial as example
    public void testExpandHint1() throws Exception {
        env = makeEnv("include \"$int.p\" " +
                "function int f(int) " +
                "rule f_def find f(%a) replace %a*f(%a-1)");

        Term[] empty = {  };
        Term[] succ = { makeTerm("f(3) = 3*f(2)") };
        Sequent s = new Sequent(empty, succ);

        Proof p = makeProofAndApplyHint(s, new ExpandProofHint(), "expand", "f_def");
        assertEquals(1, p.getOpenGoals().size());
        ProofNode goal = p.getOpenGoals().get(0);

        Term[] expSucc = { makeTerm("3*f(3-1) = 3*(2*f(2-1))") };

        assertEquals(new Sequent(empty, expSucc), goal.getSequent());
    }

 // factorial as example
    public void testExpandHint2() throws Exception {
        env = makeEnv("include \"$int.p\" " +
                "function int f(int) " +
                "rule f_def find f(%a) replace %a*f(%a-1)");

        Term[] empty = {  };
        Term[] succ = { makeTerm("f(3) = 3*f(2)") };
        Sequent s = new Sequent(empty, succ);

        Proof p = makeProofAndApplyHint(s, new ExpandProofHint(), "expand", "f_def", "2");
        assertEquals(1, p.getOpenGoals().size());
        ProofNode goal = p.getOpenGoals().get(0);

        Term[] expSucc = { makeTerm("3*((3-1)*f((3-1)-1)) = 3*(2*((2-1)*f((2-1)-1)))") };

        assertEquals(new Sequent(empty, expSucc), goal.getSequent());
    }
}
