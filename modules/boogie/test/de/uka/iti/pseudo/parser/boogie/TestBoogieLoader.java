package de.uka.iti.pseudo.parser.boogie;

import java.io.File;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.gui.Main;

public class TestBoogieLoader extends TestCaseWithEnv {

    public void test_SimpleGuiLoader() throws Exception {
        String path = "examples/boogie/test/closable/tfe_test/simple.bpl";
        
        Main.openProver(new File(path));
    }
}
