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

import junit.framework.TestCase;
import de.uka.iti.pseudo.algo.data.ParsedAlgorithm;
import de.uka.iti.pseudo.algo.data.ParsedData;

public class TestAlgoVisitor extends TestCase {

    public void testComplexAbbrev() throws Exception {
        AlgoParser p = new AlgoParser(new StringReader(
                "abbreviation @test := a + b " +
                "algo x do assume @test*c > 0 " +
                "end"));
        ASTStart algo = p.Start();

        ParsedData pd = new ParsedData();
        TermVisitor termVisit = new TermVisitor(pd);
        algo.jjtAccept(new AlgoDeclarationVisitor(pd), null);

        assertEquals("(a + b)", pd.getAbbreviation("@test"));

        Node assumeExpr = algo.jjtGetChild(1).jjtGetChild(1).jjtGetChild(0).jjtGetChild(0);
        assertEquals("(((a + b) * c) > 0)", assumeExpr.jjtAccept(termVisit, null));
    }

    public void testUnknownAbbrev() throws Exception {
        AlgoParser p = new AlgoParser(new StringReader(
                "algo x do assume @unknown end"));
        ASTStart algo = p.Start();

        ParsedData pd = new ParsedData();
        algo.jjtAccept(new AlgoDeclarationVisitor(pd), null);
        try {
            algo.jjtAccept(new AlgoDeclarationVisitor(pd), null);
            ParsedAlgorithm x = pd.getAlgorithms().get("x");
            AlgoVisitor v = new AlgoVisitor(pd, x, false);
            v.extractProgram();

            fail("Should have failed");
        } catch(IllegalStateException ex) {
            assertEquals("Abbreviation @unknown not defined", ex.getMessage());
        }
    }
}
