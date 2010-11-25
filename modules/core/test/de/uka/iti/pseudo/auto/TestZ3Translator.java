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
package de.uka.iti.pseudo.auto;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.term.Term;

public class TestZ3Translator extends TestCaseWithEnv {

    public void testZ3() throws Exception {
        
        Z3Translator trans = new Z3Translator(env);
        
        Term t = makeTerm("(\\forall x; x > 0)");
        t.visit(trans);
        
    }
    
    
    public void testBoolean() throws Exception {
        Z3Translator trans = new Z3Translator(env);
        
        Term t = makeTerm("true");
        t.visit(trans);
        assertEquals("App c1 true", trans.translation.get(0));
        
        t = makeTerm("b1");
        t.visit(trans);
        assertEquals("Const c2 x.b1.bool bool", trans.translation.get(1));
    }

    
}
