package de.uka.iti.pseudo.term;

import java.io.File;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.parser.Parser;

public class TestTermComparator extends TestCaseWithEnv {

    private TermComparator tc;

    @Override
    protected void setUp() throws Exception {
        Parser fp = new Parser();
        EnvironmentMaker em = new EnvironmentMaker(fp, new File(
                "test/de/uka/iti/pseudo/term/testTermComparator.p"));
        env = em.getEnvironment();
        env.addFunction(new Function("sk", Environment.getBoolType(),
                new Type[0], false, false, ASTLocatedElement.CREATED));
        env.setFixed();
        tc = new TermComparator(env);
    }
    
    public void testCompare() throws Exception {
        assertTrue(tc.compare(makeTerm("a"), makeTerm("b")) < 0);
        assertTrue(tc.compare(makeTerm("c"), makeTerm("b")) > 0);
        assertTrue(tc.compare(makeTerm("a"), makeTerm("a")) == 0);
        
        assertTrue(tc.compare(makeTerm("1"), makeTerm("0")) == 0);
        assertTrue(tc.compare(makeTerm("a"), makeTerm("0")) < 0);
        assertTrue(tc.compare(makeTerm("z"), makeTerm("0")) > 0);
        assertTrue(tc.compare(makeTerm("a"), makeTerm("sk")) > 0);
    }
    
    public void testCompareDepth() throws Exception {
        assertTrue(tc.compare(makeTerm("f(a)"), makeTerm("g(a)")) == 0);
        assertTrue(tc.compare(makeTerm("f(a)"), makeTerm("f(b)")) < 0);
        assertTrue(tc.compare(makeTerm("g(a)"), makeTerm("f(b)")) < 0);
        assertTrue(tc.compare(makeTerm("g(z)"), makeTerm("h(a,b)")) > 0);
    }
    
    public void testCompareUpdate() throws Exception {
        assertTrue(tc.compare(makeTerm("{ass:=f(ass)}b"), makeTerm("a")) > 0);
        assertTrue(tc.compare(makeTerm("{ass:=f(ass)}b"), makeTerm("z")) < 0);
        assertTrue(tc.compare(makeTerm("{ass:=f(ass)}b"), makeTerm("{ass:=f(ass)}c")) < 0);
    }

}
