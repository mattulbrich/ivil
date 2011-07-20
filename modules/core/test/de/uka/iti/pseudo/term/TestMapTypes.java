package de.uka.iti.pseudo.term;

import java.io.StringReader;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.file.ASTFile;

public class TestMapTypes extends TestCaseWithEnv {

    // was a bug - could not be parsed
    public void testMapApplicationOnTerm() throws Exception {
        makeEnv("sort S as [int]int\n" +
        		"function S map assignable\n" +
        		"problem ({map := map}map)[5] = 5");
    }
    
    public void testMapDeclarations() throws Exception {
        makeEnv("sort S T as [S]S");
        try {
            makeEnv("sort S as [S]S");
            fail("Expected: Circularity error");
        } catch(ASTVisitException ex) {
            out(ex);
        }
        
        makeEnv("sort R as {'a}['a]'b");
        
        try {
            makeEnv("sort S sort T as [%'a]S");
            fail("Should not be allowed");
        } catch (ASTVisitException ex) {
            out(ex);
            assertNull("Must be a cause-less exception", ex.getCause());
        }
        
    }
    
    private ASTFile parseFile(String s) throws ParseException {
        Parser fp = new Parser();
        return fp.parseFile(new StringReader(s), "*test*");   
    }
    
    public void testParserCases() throws Exception {
        try {
            parseFile("sort S sort T as {%'a}[S]S");
            fail("Should not be allowed");
        } catch (ParseException e) {
            out(e);
        }
        
    }
}
