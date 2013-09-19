package de.uka.iti.pseudo.algo;

import java.io.StringReader;

import junit.framework.TestCase;

public class TestAlgoVisitor extends TestCase {

    public void testComplexAbbrev() throws Exception {
        AlgoParser p = new AlgoParser(new StringReader(
                "abbreviation @test := a + b " +
                "algo x do assume @test*c > 0 " +
                "end"));
        ASTStart algo = p.Start();

        Translation translation = new Translation(p);
        TermVisitor termVisit = new TermVisitor(translation);
        algo.jjtAccept(new TranslationVisitor(translation, false), null);

        assertEquals("(a + b)", translation.getAbbreviatedTerm("@test"));

        Node assumeExpr = algo.jjtGetChild(1).jjtGetChild(1).jjtGetChild(0).jjtGetChild(0);
        assertEquals("(((a + b) * c) > 0)", assumeExpr.jjtAccept(termVisit, null));
    }

    public void testUnknownAbbrev() throws Exception {
        AlgoParser p = new AlgoParser(new StringReader(
                "algo x do assume @unknown end"));
        ASTStart algo = p.Start();

        Translation translation = new Translation(p);
        try {
            String result = algo.jjtAccept(new TranslationVisitor(translation, false), null);
            fail("Should have failed, but returned " + result);
        } catch(IllegalStateException ex) {
            assertEquals("Abbreviation @unknown not defined", ex.getMessage());
        }
    }
}
