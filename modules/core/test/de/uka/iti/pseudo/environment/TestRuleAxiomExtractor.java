package de.uka.iti.pseudo.environment;

import java.io.StringReader;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.creation.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.term.Term;

public class TestRuleAxiomExtractor extends TestCaseWithEnv {

    private Environment testEnv(String string) throws Exception {
        Parser fp = new Parser();
        ASTFile ast = fp.parseFile(new StringReader("include \"$base.p\" " + string), "*test*");
//        de.uka.iti.pseudo.util.protocol.none.Handler.registerNoneHandler();
        EnvironmentMaker em = new EnvironmentMaker(fp, ast, "none:test");
        Environment env = em.getEnvironment();
        // env.dump();
        return env;
    }


    public void testAxiomExtraction() throws Exception {

        env = testEnv("include \"$int.p\" " +
                "rule R find 1 replace 2" +
                "tags asAxiom");

        Axiom ax = env.getAxiom("R");

        assertNotNull(ax);
        assertEquals("", ax.getProperty("fromRule"));
        assertEquals(makeTerm("2=1"), ax.getTerm());
    }

    public void testQuantAxiomExtraction() throws Exception {
        env = testEnv("include \"$int.p\" " +
                "rule R find 1+%a replace %a+1" +
                "tags asAxiom");

        Axiom ax = env.getAxiom("R");

        assertEquals(makeTerm("(\\forall a; a+1=1+a)"), ax.getTerm());
    }

    public void testTypeQuantAxiomExtraction() throws Exception {
        env = testEnv("include \"$int.p\" " +
                "rule R find arb=arb as %'a replace true " +
                "tags asAxiom");

        Axiom ax = env.getAxiom("R");

        assertEquals(makeTerm("(\\T_all 'ty_a; arb as 'ty_a = arb)"), ax.getTerm());
    }

    public void testNameClash() throws Exception {
        env = testEnv("function bool p('a) " +
                "rule R find p(arb as %'a) replace p(arb as 'ty_a) " +
                "tags asAxiom");

        Axiom ax = env.getAxiom("R");

        Term.SHOW_TYPES = true;
        assertEquals(makeTerm("(\\T_all 'ty_a1; p(arb as 'ty_a) = p(arb as 'ty_a1))"), ax.getTerm());
    }
}
