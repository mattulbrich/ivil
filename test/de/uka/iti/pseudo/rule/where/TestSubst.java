package de.uka.iti.pseudo.rule.where;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermUnification;

public class TestSubst extends TestCaseWithEnv {

    public void testTrySubst() throws Exception {
        Subst subst = new Subst();

        Term[] t = { makeTerm("%i"), makeTerm("true"), makeTerm("false"), makeTerm("4") };
        subst.checkSyntax(t);

        try {
            t[0] = t[1];
            subst.checkSyntax(t);
            fail("should fail");
        } catch(RuleException ex) {
        }

        try {
            subst.checkSyntax(new Term[] { makeTerm("%i"), null, null});
            fail("should fail");
        } catch(RuleException ex) {
        }
    }

    public void testSubst() throws Exception {
        Subst subst = new Subst();

        Term[] t = { makeTerm("%i"), makeTerm("3"), makeTerm("4"), makeTerm("4+3") };
        WhereClause wc = new WhereClause(subst, t);
        TermUnification mc = new TermUnification();
        
        subst.applyTo(wc, mc);
        assertEquals(makeTerm("4+4"), mc.instantiate(makeTerm("%i")));
        
        // assert I can apply a second time
        subst.applyTo(wc, mc);
    }
    
    public void testIncompatibleSubst() throws Exception {
        Subst subst = new Subst();

        Term[] t = { makeTerm("%i"), makeTerm("3"), makeTerm("4"), makeTerm("4+3") };
        WhereClause wc = new WhereClause(subst, t);
        TermUnification mc = new TermUnification();
        mc.addInstantiation((SchemaVariable) t[0], makeTerm("55"));

        try {
            subst.applyTo(wc, mc);
            fail("shoulld fail");
        } catch(RuleException ex) {
        }

    }
}