package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.TestCaseWithEnv;

public class TestUpdates extends TestCaseWithEnv {

    public void testCreation() throws Exception {
        assertEquals(makeTerm("{i1 := 3}3"), makeTerm("{i1 := 3}3"));
    }
    
}
