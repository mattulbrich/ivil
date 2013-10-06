package de.uka.iti.pseudo.algo;

import java.io.StringReader;

import junit.framework.TestCase;

public class TestTermVisitor extends TestCase {

    public TermVisitor termVisit;
    private Translation translation;

    @Override
    protected void setUp() throws Exception {
        translation = new Translation((String)null);
        termVisit = new TermVisitor(translation);
    }

    public void testAbbrevIdentifier() throws Exception {
        translation.putAbbreviation("@test", "TEST");
        check("@test", "TEST");
    }

    private void check(String in, String out) throws Exception {
        AlgoParser p = new AlgoParser(new StringReader("algo x do assume " + in + " end"));
        ASTStart algo = p.Start();

        Node assume = algo.jjtGetChild(0).jjtGetChild(1).jjtGetChild(0);
        String result = assume.jjtGetChild(0).jjtAccept(termVisit, null);

        assertEquals(out, result);
    }

}
