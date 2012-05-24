package de.uka.iti.pseudo.algo;

import java.io.StringReader;

import junit.framework.TestCase;

public class TestChainedRelationVisitor extends TestCase {

    public static final TermVisitor TERMVISIT = new TermVisitor();

    private void testChaining(String in, String out) throws Exception {
        AlgoParser p = new AlgoParser(new StringReader("algo x do assume " + in + " end"));
        ASTStart algo = p.Start();

        Node assume = algo.jjtGetChild(0).jjtGetChild(1).jjtGetChild(0);
        ASTBinaryExpression binex =
                (ASTBinaryExpression) assume.jjtGetChild(0);

        ChainedRelationVisitor crv = new ChainedRelationVisitor();
        binex.jjtAccept(crv, null);

        assertEquals(out, binex.jjtAccept(TERMVISIT, null));
    }

    public void testChainingLTE() throws Exception {
        testChaining("a <= b <= c", "((a <= b) & (b <= c))");
    }

    public void testChainingLT() throws Exception {
        testChaining("a < b < c", "((a < b) & (b < c))");
    }

    public void testChainingMIXED() throws Exception {
        testChaining("0 <= b < c", "((0 <= b) & (b < c))");
    }

    public void testChainingDeep() throws Exception {
        testChaining("0 <= b < c < d", "((0 <= b) & ((b < c) & (c < d)))");
    }

}
