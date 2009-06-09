package de.uka.iti.pseudo.proof;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.term.TestTermParser;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMaker;

public class MockingProof extends Proof {
    
    private Environment env;
    private Term[] trueTermArr;

    public MockingProof() throws Exception {
        int no = 0;
        env = TestTermParser.loadEnv();
        trueTermArr = new Term[] { TermMaker.makeTerm("true as bool", env) };
        Sequent trueSequent = new Sequent(trueTermArr, trueTermArr);
        root = new ProofNode(this, null, trueSequent);
        ProofNode c1 = new ProofNode(this, root, mkSeq(no++));
        {
            ProofNode c11 = new ProofNode(this, c1, mkSeq(no++));
            openGoals.add(c11);
            ProofNode c12 = new ProofNode(this, c1, mkSeq(no++));
            openGoals.add(c12);
            ProofNode c13 = new ProofNode(this, c1, mkSeq(no++));
            openGoals.add(c13);
            c1.setChildren(new ProofNode[] { c11, c12, c13 });
        }
        ProofNode c2 = new ProofNode(this, root, mkSeq(no++));
        openGoals.add(c2);
        ProofNode c3 = new ProofNode(this, root, mkSeq(no++));
        openGoals.add(c3);
        ProofNode c4 = new ProofNode(this, root, mkSeq(no++));
        openGoals.add(c4);
        root.setChildren(new ProofNode[] { c1, c2, c3, c4 });
    }
    
    private Sequent mkSeq(int i) throws Exception {
        Term t = TermMaker.makeTerm(i + "=" + i, env);
        Term[] arr = { t };
        return new Sequent(trueTermArr, arr);
    }
}
