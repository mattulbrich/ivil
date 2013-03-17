///*
// * This file is part of
// *    ivil - Interactive Verification on Intermediate Language
// *
// * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
// *    written by Mattias Ulbrich
// *
// * The system is protected by the GNU General Public License.
// * See LICENSE.TXT (distributed with this file) for details.
// */
//package de.uka.iti.pseudo.prettyprint;
//
//import de.uka.iti.pseudo.TestCaseWithEnv;
//import de.uka.iti.pseudo.term.Term;
//import de.uka.iti.pseudo.term.TermException;
//
//public class TestTermTag extends TestCaseWithEnv {
//
//    // from a bug
//    public void testSubtermCalc() throws Exception {
//
//        Term t = makeTerm("g(4+2,3)");
//
//        TermTag tag = new TermTag(t);
//        TermTag tag2 = tag.derive(1);
//
//        assertEquals(4, tag2.getTotalPos());
//        assertEquals(1, tag2.getSubTermNo());
//
//        try {
//            tag.derive(2);
//            fail("should fail");
//        } catch(TermException ex) {}
//
//    }
//
//    public void testCountAllSubterms() throws Exception {
//
//        Term t = makeTerm("g(f(f(g(3,4))), f(f(3)))");
//        assertEquals(9, t.countAllSubterms());
//    }
//
//}
