/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.algo;

import java.io.StringReader;

import de.uka.iti.pseudo.algo.data.ParsedData;

import junit.framework.TestCase;

public class TestTermVisitor extends TestCase {

    public TermVisitor termVisit;
    private ParsedData parsedData;

    @Override
    protected void setUp() throws Exception {
        parsedData = new ParsedData();
        termVisit = new TermVisitor(parsedData);
    }

    public void testAbbrevIdentifier() throws Exception {
        parsedData.putAbbreviation("@test", "TEST");
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
