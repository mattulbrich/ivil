/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.environment;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;

public class TestTypeVariableCollector extends TestCaseWithEnv {

    public void testCollectTerm() throws Exception {
        Term t = makeTerm("arb as 'a");
        Set<TypeVariable> result = TypeVariableCollector.collect(t);
        assertEquals(Collections.singleton(t.getType()), result);
        
        t = makeTerm("(\\forall x as 'a; x=x)");
        result = TypeVariableCollector.collect(t);
        assertEquals(Collections.singleton(TypeVariable.ALPHA), result);
        
        t = makeTerm("(\\forall x as 'a; true)");
        result = TypeVariableCollector.collect(t);
        assertEquals(Collections.singleton(TypeVariable.ALPHA), result);
    }

    public void testCollectType() throws Exception {
        Type t = makeTerm("arb as poly(poly('a, 'b), poly(set('a), int))").getType();
        Set<TypeVariable> result = TypeVariableCollector.collect(t);
        
        HashSet<TypeVariable> expected = new HashSet<TypeVariable>();
        expected.add(TypeVariable.ALPHA);
        expected.add(TypeVariable.BETA);
        
        assertEquals(expected, result);
    }

}
