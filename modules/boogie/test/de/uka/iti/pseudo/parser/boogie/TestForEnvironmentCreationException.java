package de.uka.iti.pseudo.parser.boogie;
import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.boogie.EnvironmentCreationException;
import de.uka.iti.pseudo.environment.boogie.TypeSystemException;
public class TestForEnvironmentCreationException extends TestCaseWithEnv {

// generated test for modules/boogie/test/data/EnvironmentCreationException/BadLabels2.bpl
public void testBoogieParsemodules_boogie_test_data_EnvironmentCreationException_BadLabels2() throws Exception {
try{
BPLParser.main(new String[] { "modules/boogie/test/data/EnvironmentCreationException/BadLabels2.bpl"});
} catch(EnvironmentCreationException ex){
return;
}
fail("expected EnvironmentCreationException to be trown");
}

// generated test for modules/boogie/test/data/EnvironmentCreationException/BadLabels1.bpl
public void testBoogieParsemodules_boogie_test_data_EnvironmentCreationException_BadLabels1() throws Exception {
try{
BPLParser.main(new String[] { "modules/boogie/test/data/EnvironmentCreationException/BadLabels1.bpl"});
} catch(EnvironmentCreationException ex){
return;
}
fail("expected EnvironmentCreationException to be trown");
}

// generated test for modules/boogie/test/data/EnvironmentCreationException/BadLabels3.bpl
public void testBoogieParsemodules_boogie_test_data_EnvironmentCreationException_BadLabels3() throws Exception {
try{
BPLParser.main(new String[] { "modules/boogie/test/data/EnvironmentCreationException/BadLabels3.bpl"});
} catch(EnvironmentCreationException ex){
return;
}
fail("expected EnvironmentCreationException to be trown");
}

}