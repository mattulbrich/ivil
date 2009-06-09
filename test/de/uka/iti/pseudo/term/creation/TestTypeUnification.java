package de.uka.iti.pseudo.term.creation;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.UnificationException;

public class TestTypeUnification extends TestCaseWithEnv {

    // due to a bug
    public void testBoolInt() throws Exception {
        TypeUnification tu = new TypeUnification();
        
        try {
            tu.leftUnify(Environment.getBoolType(), Environment.getIntType());
            fail("Should fail");
        } catch (UnificationException e) {
            // should fail
        }
    }
    
}
