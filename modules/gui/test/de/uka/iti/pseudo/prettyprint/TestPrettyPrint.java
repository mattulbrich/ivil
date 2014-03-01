/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.prettyprint;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.prettyprint.AnnotatedString;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.creation.TermMaker;

public class TestPrettyPrint extends TestCaseWithEnv {

    PrettyPrint pp = new PrettyPrint(env);

    private void testTerm(String term, String expected) throws Exception {
        Term t = makeTerm(term);

        assertEquals(expected, pp.print(t).toString());
    }

    public void testPrettyPrint() throws Exception {
        pp.setTyped(false);
        testTerm("i1+(i2+i3)", "i1 + (i2 + i3)");
        testTerm("(i1+i2)+i3", "i1 + i2 + i3");
        testTerm("i1+i2+i3", "i1 + i2 + i3");
        // this found a bug:
        testTerm("(i1+i2)*i3", "(i1 + i2) * i3");
        testTerm("[ 6; P ]true", "[ 6; P ]true");
        testTerm("{b1 := true}{i1:=0}i2", "{ b1 := true }{ i1 := 0 }i2");
        testTerm("[[%a : assert %b]]%phi", "[[ %a : assert %b ]]%phi");
        testTerm("! !b1", "! !b1");
        testTerm("-5 = -3", "-5 = -3");
        testTerm("! (-5 = -3)", "! -5 = -3");
        testTerm("!((!b1) = true)", "!(!b1) = true");
        testTerm("!(1=1)", "!1 = 1");
        testTerm("(\\forall x; x = 5)", "(\\forall x; x = 5)");
        testTerm("(\\forall x; x > 5)", "(\\forall x; x > 5)");
        testTerm("((\\T_all 'a; (true)))", "(\\T_all 'a; true)");
        testTerm("f(1+2)", "f(1 + 2)");
    }

    // was a bug
    public void testParens() throws Exception {
        testTerm("{ i1 := 1 }(i1 + i1)", "{ i1 := 1 }(i1 + i1)");
        testTerm("({ i1 := 1 }i1) + i1", "{ i1 := 1 }i1 + i1");

        testTerm("[0; P](b1 & b1)", "[ 0; P ](b1 & b1)");
        testTerm("([0; P]b1) & b1", "[ 0; P ]b1 & b1");
    }

    public void testTyped() throws Exception {
        pp.setTyped(true);
        testTerm("1 + 2", "(1 as int + 2 as int) as int");
        testTerm("1", "1 as int");
        testTerm("-1", "(-1 as int) as int");
        // is this correct: ?
        testTerm("! !b1", "(!((!b1 as bool) as bool)) as bool");
    }

    public void testAnnotations1() throws Exception {
        Term t = TermMaker.makeAndTypeTerm("i1 + i2 + i3", env);
        AnnotatedString as = PrettyPrint.print(env, t);
        assertEquals("i1 + i2 + i3", as.toString());
        assertEquals("[Element[begin=0;end=7;attr=0], " +
                "Element[begin=0;end=2;attr=0.0], " +
                "Element[begin=5;end=7;attr=0.1], " +
                "Element[begin=10;end=12;attr=1]]", as.describeAllElements());
    }

    public void testAnnotations2() throws Exception {
        Term t = TermMaker.makeAndTypeTerm("{ i1 := i2 + i3 }(i1 = i3)", env);
        AnnotatedString as = PrettyPrint.print(env, t);
        assertEquals("{ i1 := i2 + i3 }(i1 = i3)", as.toString());
        assertEquals("[Element[begin=8;end=15;attr=1], " +
                "Element[begin=8;end=10;attr=1.0], " +
                "Element[begin=13;end=15;attr=1.1], " +
                "Element[begin=17;end=26;attr=0], " +
                "Element[begin=18;end=20;attr=0.0], " +
                "Element[begin=23;end=25;attr=0.1]]", as.describeAllElements());

    }

    public void testAnnotations3() throws Exception {
        Term t = TermMaker.makeAndTypeTerm("1 + (2 + 3)", env);
        // 01234567890
        AnnotatedString as = PrettyPrint.print(env, t);
        assertEquals("1 + (2 + 3)", as.toString());
        assertEquals("[Element[begin=0;end=1;attr=0], " +
                "Element[begin=4;end=11;attr=1], " +
                "Element[begin=5;end=6;attr=1.0], " +
                "Element[begin=9;end=10;attr=1.1]]", as.describeAllElements());
    }

    // this needs to be the last test!
    public void testPPPlugin() throws Exception {
        env.getPluginManager().register(null, PrettyPrint.SERVICE_NAME, "de.uka.iti.pseudo.gui.MockPrettyPrintPlugin");
        pp = new PrettyPrint(env);

        testTerm("f(3)", "{-3-}");
        testTerm("g(3,4)", "g[4, 3]");
        testTerm("(\\forall x; x >0)", "ALL x ; x > 0");
        // No update rewriting at the moment, may come back later
        // testTerm("{i1:=0}i2", "{ i1 <-- 0 }i2");

    }

    // these need to be the last test!
    public void testNameReplacing() throws Exception {

        env = new Environment("none:generated", env);
        env.getPluginManager().register(null, PrettyPrint.SERVICE_NAME, "de.uka.iti.pseudo.gui.MockPrettyPrintPlugin");
        env.addFunction(new Function("Xtest", Environment.getBoolType(),
                new Type[0], false, false, ASTLocatedElement.CREATED));

        pp = new PrettyPrint(env);

        testTerm("Xtest", "test");
        testTerm("(\\forall Xx; Xx >0)", "ALL x ; x > 0");
    }

    public void testIndention() throws Exception {
        Term t = TermMaker.makeAndTypeTerm("111111 + (222222222 + 3333333)", env);
        pp = new PrettyPrint(env);

        assertEquals("  111111\n" +
                     "+ (  222222222\n" +
                     "   + 3333333)", pp.print(t, 10).toString());
    }

}
