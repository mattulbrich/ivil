package de.uka.iti.pseudo.environment;

import java.io.StringReader;
import java.util.HashMap;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.creation.EnvironmentMaker;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.term.creation.TermMaker;

public class TestRuleAxiomExtractor extends TestCaseWithEnv {

    private Environment testEnv(String string) throws Exception {
        Parser fp = new Parser();
        ASTFile ast = fp.parseFile(new StringReader("include \"$base.p\" " + string), "*test*");
        EnvironmentMaker em = new EnvironmentMaker(fp, ast, "none:test");
        Environment env = em.getEnvironment();
        return env;
    }

    public void testAxiomSchemaTyping() throws Exception {
        try {
            new Axiom("invalid", TermMaker.makeAndTypeTerm("arb as %'a = arb as %'a", new LocalSymbolTable(DEFAULT_ENV)),
                    new HashMap<String, String>(), ASTLocatedElement.CREATED);
            // axiom contains schema type %'a!
            fail("expected EnvironmentException, because the requested axiom is ill-typed");
        } catch (EnvironmentException e) {
            if(VERBOSE) {
                e.printStackTrace();
            }
        }
    }

    public void testAddAxiomExtraction() throws Exception {
        env = testEnv("include \"$int.p\" " +
                "rule R find %a" +
                "  samegoal   replace 2" +
                "  samegoal   add |- %a=2" +
                "  tags asAxiom");

        Axiom ax = env.getAxiom("R");

        assertEquals(makeTerm("(\\forall a; $pattern(a," +
                " !(!a=2 & a=2)))").toString(true), ax.getTerm().toString(true));
    }

    public void testLocatedAxiom() throws Exception {

        env = testEnv("include \"$int.p\" " +
                "rule R find |- %a=2" +
                "  replace %a+1=3" +
                "  tags asAxiom");

        Axiom ax = env.getAxiom("R");

        assertEquals(makeTerm("(\\forall a; $pattern(a=2, a+1=3 -> a=2))"), ax.getTerm());
    }

    public void testAxiomExtraction() throws Exception {

        env = testEnv("include \"$int.p\" " +
                "rule R find 1 replace 2" +
                "tags asAxiom");

        Axiom ax = env.getAxiom("R");

        assertNotNull(ax);
        assertEquals("", ax.getProperty("fromRule"));
        assertEquals(makeTerm("1=2"), ax.getTerm());
    }

    public void testQuantAxiomExtraction() throws Exception {
        env = testEnv("include \"$int.p\" " +
                "rule R find 1+%a replace %a+1" +
                "tags asAxiom");

        Axiom ax = env.getAxiom("R");

        assertEquals(makeTerm("(\\forall a; $pattern(1+a, 1+a=a+1))"), ax.getTerm());
    }

    public void testTypeQuantAxiomExtraction() throws Exception {
        env = testEnv("include \"$int.p\" " +
                "rule R find arb as %'a=arb as %'a replace true " +
                "tags asAxiom");

        Axiom ax = env.getAxiom("R");

        assertEquals(makeTerm("(\\T_all 'ty_a; $pattern(arb as 'ty_a = arb as 'ty_a," +
                "arb as 'ty_a = arb as 'ty_a))"), ax.getTerm());
    }

    public void testNameClash() throws Exception {
        env = testEnv("function bool p('a) " +
                "rule R find p(arb as %'a) replace p(arb as 'ty_a) " +
                "tags asAxiom");

        Axiom ax = env.getAxiom("R");

        assertEquals(makeTerm("(\\T_all 'ty_a1; $pattern(p(arb as 'ty_a1)," +
                "p(arb as 'ty_a1) = p(arb as 'ty_a)))").toString(true), ax.getTerm().toString(true));
    }

    // was a bug in selection sort algo example!
    // very nasty. Z3 could close EVERYTHING!
    // !@*#! gggrrr.
    public void testSchemaTypeAndVar() throws Exception {
        env = testEnv("rule R find %a = %b replace %b = %a " +
                "tags asAxiom");

        Axiom ax = env.getAxiom("R");

        assertEquals(makeTerm("(\\T_all 'ty_b; (\\forall a as 'ty_b; (\\forall b as 'ty_b;" +
        		"$pattern(a = b, (a=b)=(b=a)))))").toString(true), ax.getTerm().toString(true));
    }
}
