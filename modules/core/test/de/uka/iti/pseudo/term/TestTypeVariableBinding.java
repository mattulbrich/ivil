package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;

public class TestTypeVariableBinding extends TestCaseWithEnv {

    // due to a bug
    public void testEquality() throws Exception {
       Term term = makeTerm("$impl((\\T_all 'a;(\\forall x as 'a;$eq(x as 'a,x as 'a) as bool) as bool) as bool,false as bool) as bool");
       assertEquals(term, term);
    }

    public void testNonVariable() throws Exception {

        // try to bind "int"
        try {
            TypeVariableBinding tvb = TypeVariableBinding.getInst(
                    TypeVariableBinding.Kind.ALL,
                    Environment.getBoolType(),
                    Environment.getTrue());
            fail("");
        } catch (TermException e) {
            if(VERBOSE) {
                e.printStackTrace();
            }
        }
    }
}
