package de.uka.iti.pseudo.auto.strategy;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.term.Term;

public class TestRuleMatchTreeCollection extends TestCaseWithEnv {

    @Override
    public void setUp() throws Exception {
        env = makeEnv(getClass().getResource("ruleMatchTreeCollection.p.txt"));
    }

    public void testTree() throws Exception {
        Term t = makeTerm("5+4 > 4+5");
        RuleMatchTreeCollection rc = new RuleMatchTreeCollection(env.getLocalRules());
        RuleMatchTree tree = rc.getRuleMatchTree(t);

        //Dump.dumpRuleMatchTree(tree);
        {
            assertEquals("[Rule[B]]", tree.getMatchingRules().toString());
            assertEquals(2, tree.getBranches().size());
            {
                RuleMatchTree t1 = tree.getBranches().get(0);
                assertEquals("[Rule[A]]", t1.getMatchingRules().toString());
                assertEquals(2, t1.getBranches().size());
                {
                    RuleMatchTree t11 = t1.getBranches().get(0);
                    assertEquals("[Rule[C1], Rule[C2]]", t11.getMatchingRules().toString());
                    assertEquals(0, t11.getBranches().size());
                }
                {
                    RuleMatchTree t12 = t1.getBranches().get(1);
                    assertEquals("[]", t12.getMatchingRules().toString());
                    assertEquals(0, t12.getBranches().size());
                }
            }
            {
                RuleMatchTree t2 = tree.getBranches().get(1);
                assertEquals("[Rule[A]]", t2.getMatchingRules().toString());
                assertEquals(2, t2.getBranches().size());
                {
                    RuleMatchTree t21 = t2.getBranches().get(0);
                    assertEquals("[]", t21.getMatchingRules().toString());
                    assertEquals(0, t21.getBranches().size());
                }
                {
                    RuleMatchTree t22 = t2.getBranches().get(1);
                    assertEquals("[Rule[C1], Rule[C2]]", t22.getMatchingRules().toString());
                    assertEquals(0, t22.getBranches().size());
                }
            }
        }
    }

}

/* Content of the text file

include "$int.p"

rule A
 find %a + %b
 replace 0

rule B
 find %a > %b
 replace true

rule C1
 find 5
 replace 0

 rule C2
 find 5
 replace 0
*/