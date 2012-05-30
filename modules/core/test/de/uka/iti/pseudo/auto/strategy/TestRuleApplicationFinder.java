package de.uka.iti.pseudo.auto.strategy;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationFinder;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Dump;

public class TestRuleApplicationFinder extends TestCaseWithEnv {

    // found also a bug
    public void testFindWithSimpleAssumption() throws Exception {

        env = makeEnv(getClass().getResource("ruleApplicationFinder.p.txt"));
        Term[] ante = { makeTerm("p(1,2)"), makeTerm("p(1,2)") };
        Term[] succ = { makeTerm("p(2,1)") };
        Sequent s = new Sequent(ante, succ);
        Proof p = new Proof(s);

        RuleApplicationFinder ruleAppFinder = new RuleApplicationFinder(p.getRoot(), env);
        Rule rule = env.getRule("noFreeAssumption");
        List<RuleApplication> rams = ruleAppFinder.findAll(new TermSelector("S.0"),
                Collections.singletonList(rule));

        Dump.dumpRuleApplication(rams.get(0));
        Dump.dumpRuleApplication(rams.get(1));

        assertEquals(2, rams.size());
        {
            RuleApplication ram = rams.get(0);
            assertEquals("A.0", ram.getAssumeSelectors().get(0).toString());
        }
        {
            RuleApplication ram = rams.get(1);
            assertEquals("A.1", ram.getAssumeSelectors().get(0).toString());
        }


    }

    public void testFindWithSchematicAssumption() throws Exception {

        env = makeEnv(getClass().getResource("ruleApplicationFinder.p.txt"));
        Term[] ante = { makeTerm("p(1,4)"), makeTerm("p(1,2)") };
        Term[] succ = { makeTerm("p(2,1)") };
        Sequent s = new Sequent(ante, succ);
        Proof p = new Proof(s);

        RuleApplicationFinder ruleAppFinder = new RuleApplicationFinder(p.getRoot(), env);
        Rule rule = env.getRule("freeAssumption");

        List<RuleApplication> rams = ruleAppFinder.findAll(new TermSelector("S.0"),
                Collections.singletonList(rule));

        Dump.dumpRuleApplication(rams.get(0));
        Dump.dumpRuleApplication(rams.get(1));

        assertEquals(2, rams.size());
        {
            RuleApplication ram = rams.get(0);
            assertEquals("A.0", ram.getAssumeSelectors().get(0).toString());
        }
        {
            RuleApplication ram = rams.get(1);
            assertEquals("A.1", ram.getAssumeSelectors().get(0).toString());
        }

    }

}
