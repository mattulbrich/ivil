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
package de.uka.iti.pseudo.term.creation;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.term.Term;

public class TestSubtermReplacer extends TestCaseWithEnv {
    
    public void testReplace() throws Exception {
        Term org = TermMaker.makeAndTypeTerm("{ i1 := 0 } (i1+1)", env);
        Term two = TermMaker.makeAndTypeTerm("2", env);
        
        Term result = SubtermReplacer.replace(org, 4, two);
        Term expected = TermMaker.makeAndTypeTerm("{ i1 := 2 } (i1+1)", env);
        assertEquals(expected, result);
        
        result = SubtermReplacer.replace(org, 1, two);
        expected = TermMaker.makeAndTypeTerm("{ i1 := 0 } 2", env);
        assertEquals(expected, result);
        
        result = SubtermReplacer.replace(org, 2, two);
        expected = TermMaker.makeAndTypeTerm("{ i1 := 0 } (2+1)", env);
        assertEquals(expected, result);
        
        result = SubtermReplacer.replace(org, 3, two);
        expected = TermMaker.makeAndTypeTerm("{ i1 := 0 } (i1+2)", env);
        assertEquals(expected, result);
        
        result = SubtermReplacer.replace(org, 0, two);
        assertEquals(two, result);
        
        result = SubtermReplacer.replace(org, 1000, two);
        assertNull(result);
    }
    
    // was a bug!
    public void testBinding() throws Exception {
        Term org = makeTerm("(\\forall n; n > 0) -> [ 1; P]");
        Term two = TermMaker.makeAndTypeTerm("true", env);
        
        Term result = SubtermReplacer.replace(org, 5, two);
        Term expected = makeTerm("(\\forall n; n > 0) -> true");
        assertEquals(expected, result);
    }
    
    // from a bug!
    public void testInUpdate() throws Exception {
        Term org = makeTerm("{ i1 := 3 }[ 1; P ]");
        Term two = makeTerm("{ b1 := true } [2;P]");
        Term result = SubtermReplacer.replace(org, 1, two);
        
        assertEquals(makeTerm("{i1 := 3 }{ b1 := true } [2;P]"), result);
    }
    
}
