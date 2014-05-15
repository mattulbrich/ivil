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
import de.uka.iti.pseudo.term.TermException;

public class TestSubtermReplacer extends TestCaseWithEnv {
    
    public void testReplace() throws Exception {
        Term org = makeTerm("{ i1 := 0 } (i1+1)");
        Term two = makeTerm("2");
        
        Term result = SubtermReplacer.replace(org, 4, two);
        Term expected = makeTerm("{ i1 := 2 } (i1+1)");
        assertEquals(expected, result);
        
        result = SubtermReplacer.replace(org, 1, two);
        expected = makeTerm("{ i1 := 0 } 2");
        assertEquals(expected, result);
        
        result = SubtermReplacer.replace(org, 2, two);
        expected = makeTerm("{ i1 := 0 } (2+1)");
        assertEquals(expected, result);
        
        result = SubtermReplacer.replace(org, 3, two);
        expected = makeTerm("{ i1 := 0 } (i1+2)");
        assertEquals(expected, result);
        
        result = SubtermReplacer.replace(org, 0, two);
        assertEquals(two, result);
        
        try {
            result = SubtermReplacer.replace(org, 1000, two);
            fail("Should have failed with TermException");
        } catch(TermException ex) {
        }
    }
    
    // was a bug!
    public void testBinding() throws Exception {
        Term org = makeTerm("(\\forall n; n > 0) -> [ 1; P]true");
        Term two = makeTerm("true");
        
        Term result = SubtermReplacer.replace(org, 5, two);
        Term expected = makeTerm("(\\forall n; n > 0) -> true");
        assertEquals(expected, result);
    }
    
    // from a bug!
    public void testInUpdate() throws Exception {
        Term org = makeTerm("{ i1 := 3 }[ 1;P ]b1");
        Term two = makeTerm("{ b1 := true } [2;P]b1");
        Term result = SubtermReplacer.replace(org, 1, two);
        
        assertEquals(makeTerm("{i1 := 3 }{ b1 := true } [2;P]b1"), result);
    }
    
}
