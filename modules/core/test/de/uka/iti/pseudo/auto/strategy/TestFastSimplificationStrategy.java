package de.uka.iti.pseudo.auto.strategy;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Pair;

public class TestFastSimplificationStrategy extends TestCaseWithEnv {

    @Override
    protected void setUp() throws Exception {

    }

    public void testLevels() throws Exception {
        env = makeEnv(getClass().getResource("fastSimpLevels.p.txt"));
        Proof p = new Proof(makeTerm("true"));

        FastSimplificationStrategy fss = new FastSimplificationStrategy();
        fss.init(p, env, null);
        Map<Rule, Integer> map = fss.getRuleLevelMap();
        assertEquals(4, map.size());
        for (int i = 4; i < 4; i++) {
            assertEquals("R" + i, (Integer)i, map.get("R" + i));
        }
    }

    // uncovered a bug
    public void testFindAll() throws Exception {
        env = makeEnv(getClass().getResource("fastSimpLevels.p.txt"));
        Term[] ante = { makeTerm("q(p(a1,a4))"), makeTerm("q(a2)"), makeTerm("q(a3)") };
        Term[] succ = { makeTerm("q(p(a2,a2))"), makeTerm("q(a1)"), makeTerm("q(a4)") };
        Sequent s = new Sequent(ante, succ);

        Proof p = new Proof(s);

        FastSimplificationStrategy fss = new FastSimplificationStrategy();
        fss.init(p, env, null);
        List<Pair<TermSelector, Rule>> result = fss.findAllMatches(s);
        assertEquals(8, result.size());
        System.out.println(result);
        Collections.sort(result, fss.getMatchSorter());
        System.out.println(result);
        assertEquals("Pair[A.0.0.0,Rule[R1]]", result.get(0).toString());
        assertEquals("Pair[S.1.0,Rule[R1]]", result.get(1).toString());
        assertEquals("Pair[A.1.0,Rule[R2]]", result.get(2).toString());
        assertEquals("Pair[S.0.0.0,Rule[R2]]", result.get(3).toString());
        assertEquals("Pair[S.0.0.1,Rule[R2]]", result.get(4).toString());
        assertEquals("Pair[A.2.0,Rule[R3]]", result.get(5).toString());
        assertEquals("Pair[A.0.0.1,Rule[R4]]", result.get(6).toString());
        assertEquals("Pair[S.2.0,Rule[R4]]", result.get(7).toString());
    }
}
