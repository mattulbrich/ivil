package de.uka.iti.pseudo.algo;

import java.io.InputStream;

import junit.framework.TestCase;

public class TestParser extends TestCase {

    public void testParseFile() throws Exception {
        InputStream is = getClass().getResourceAsStream("parserTest.algo.txt");
        AlgoParser parser = new AlgoParser(is);
        ASTStart n = parser.Start();
        n.dump("");
        n.jjtAccept(new TranslationVisitor(new Translation(parser), false), null);
    }

}
