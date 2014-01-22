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

import java.io.InputStream;

import de.uka.iti.pseudo.algo.data.ParsedData;

import junit.framework.TestCase;

public class TestParser extends TestCase {

    public void testParseFile() throws Exception {
        InputStream is = getClass().getResourceAsStream("parserTest.algo.txt");
        AlgoParser parser = new AlgoParser(is);
        ASTStart n = parser.Start();
        n.dump("");
        n.jjtAccept(new AlgoDeclarationVisitor(new ParsedData()), null);
    }

}
