package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.TestCaseWithEnv;

public class TestProgramTerm extends TestCaseWithEnv {
    
    // due to a bug
    public void testProgramEqualities() throws Exception {
        assertEquals(makeTerm("[0;P]"), makeTerm("[0;P]"));
        assertFalse(makeTerm("[0;P]").equals(makeTerm("[[0;P]]")));
        assertFalse(makeTerm("[0;Q]").equals(makeTerm("[0;P]")));
    }
    
}
