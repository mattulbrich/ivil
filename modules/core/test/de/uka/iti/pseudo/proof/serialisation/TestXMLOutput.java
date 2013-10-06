package de.uka.iti.pseudo.proof.serialisation;

import java.io.StringWriter;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.term.Term;

public class TestXMLOutput extends TestCaseWithEnv {

    public void testGetPath() throws Exception {

        Term t = makeTerm("b1 -> (b1 & b2)");
        Proof proof = new Proof(t);

        {
            RuleApplicationMaker ra = new RuleApplicationMaker(env);
            ra.setFindSelector(new TermSelector("S.0"));
            ra.setRule(env.getRule("impl_right"));
            ra.setProofNode(proof.getRoot());
            ra.matchInstantiations();
            proof.apply(ra, env);
        }

        {
            RuleApplicationMaker ra = new RuleApplicationMaker(env);
            ra.setFindSelector(new TermSelector("S.0"));
            ra.setRule(env.getRule("and_right"));
            ra.setProofNode(proof.getGoalByNumber(2));
            ra.matchInstantiations();
            proof.apply(ra, env);
        }

        ProofNode root = proof.getRoot();
        ProofNode child = root.getChildren().get(0);
        ProofNode left = child.getChildren().get(0);
        ProofNode right = child.getChildren().get(1);

        assertEquals("", XMLOutput.getPath(root));
        assertEquals("", XMLOutput.getPath(child));
        assertEquals("0", XMLOutput.getPath(left));
        assertEquals("1", XMLOutput.getPath(right));
    }

    public void testTypeVars() throws Exception {

        Term t = makeTerm("arb as 'a = arb");
        Proof proof = new Proof(t);

        RuleApplicationMaker ra = new RuleApplicationMaker(env);
        ra.setFindSelector(new TermSelector("S.0"));
        ra.setRule(env.getRule("equality_refl"));
        ra.setProofNode(proof.getRoot());
        ra.matchInstantiations();
        proof.apply(ra, env);

        StringWriter writer = new StringWriter();
        XMLOutput output = new XMLOutput(writer);
        output.export(proof);

        String[] lines = writer.toString().split(" *\n *");
        assertEquals("<?xml version=\"1.0\"?>", lines[0]);
        assertEquals("<problem> |- $eq(arb,arb)</problem>", lines[4]);
        assertEquals("<schemavariable name=\"%t\">arb as 'a</schemavariable>", lines[11]);
        assertEquals("<typevariable name=\"t\">'a</typevariable>", lines[12]);
    }


}
