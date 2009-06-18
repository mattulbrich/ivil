package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.TestCaseWithEnv;

public class TestProgramTerm extends TestCaseWithEnv {
    
    // due to a bug
    public void testProgramEqualities() throws Exception {
        assertEquals(makeTerm("[0]"), makeTerm("[0]"));
        assertFalse(makeTerm("[0]").equals(makeTerm("[[0]]")));
    }
    
}
