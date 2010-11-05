package de.uka.iti.pseudo.parser.boogie;

import de.uka.iti.pseudo.TestCaseWithEnv;

public class TestBoogieParser extends TestCaseWithEnv {
    
    public void testBoogieLexer() throws Exception {
        Parser.main(new String[] { "examples/boogie/test/test1/Arrays.bpl" });
    }

}
