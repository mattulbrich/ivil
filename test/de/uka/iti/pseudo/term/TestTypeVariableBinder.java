package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.TestCaseWithEnv;

public class TestTypeVariableBinder extends TestCaseWithEnv {

    // due to a bug
    public void testEquality() throws Exception {
       Term term = makeTerm("$impl((\\T_all ''a;(\\forall x as ''a;$eq(x as ''a,x as ''a) as bool) as bool) as bool,false as bool) as bool");
       assertEquals(term, term);
    }
}
