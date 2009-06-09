package de.uka.iti.pseudo.rule.meta;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.rule.meta.SubstMetaFunction.TermReplacer;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.Variable;

public class TestTermReplacer extends TestCaseWithEnv {

    // due to a bug
    public void testReplace() throws Exception {
        TermReplacer tr = new TermReplacer();
        
        TypeVariable alpha = new TypeVariable("a");
        Term varx = new Variable("x", alpha);
        Term replaceWith = makeTerm("other as 'a");
        
        // replaceIn = "x = arb"
        Term replaceIn = makeTerm("(\\bind x as 'a; x=arb)").getSubterm(0);
        
        Term result = tr.replace(varx, replaceWith, replaceIn);
        
        assertEquals(result, makeTerm("other = arb as 'a"));
    }

}
