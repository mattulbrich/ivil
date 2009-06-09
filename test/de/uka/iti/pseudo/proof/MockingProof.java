package de.uka.iti.pseudo.proof;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.term.TestTermParser;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMaker;

public class MockingProof extends Proof {
    
    private Environment env;
    private Term trueTerm;

    public MockingProof() throws Exception {
        env = TestTermParser.loadEnv();
        Term[] trueTermArr = { TermMaker.makeTerm("true as bool", env) };
        Sequent trueSequent = new Sequent(trueTermArr, trueTermArr);
        root = new ProofNode(this, null, trueSequent);
        ProofNode c1 = new ProofNode(this, root, trueSequent);
        {
            ProofNode c11 = new ProofNode(this, c1, trueSequent);
            openGoals.add(c11);
            ProofNode c12 = new ProofNode(this, c1, trueSequent);
            openGoals.add(c12);
            ProofNode c13 = new ProofNode(this, c1, trueSequent);
            openGoals.add(c13);
            c1.setChildren(new ProofNode[] { c11, c12, c13 });
        }
        ProofNode c2 = new ProofNode(this, root, trueSequent);
        openGoals.add(c2);
        ProofNode c3 = new ProofNode(this, root, trueSequent);
        openGoals.add(c3);
        ProofNode c4 = new ProofNode(this, root, trueSequent);
        openGoals.add(c4);
        root.setChildren(new ProofNode[] { c1, c2, c3, c4 });
    }
}
