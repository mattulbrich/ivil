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
package de.uka.iti.pseudo.environment;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;

public class TestTypeVariableCollector extends TestCaseWithEnv {

    private static final SchemaType SCHEMA_ALPHA = new SchemaType("a");

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
    
    public void testCollectSchemaTerm() throws Exception {
        Term t = makeTerm("arb as %'a");
        Set<SchemaType> result = TypeVariableCollector.collectSchema(t);
        assertEquals(Collections.singleton(t.getType()), result);
        
        t = makeTerm("(\\forall x as %'a; x=x)");
        result = TypeVariableCollector.collectSchema(t);
        assertEquals(Collections.singleton(SCHEMA_ALPHA), result);
        
        t = makeTerm("(\\forall x as %'a; true)");
        result = TypeVariableCollector.collectSchema(t);
        assertEquals(Collections.singleton(SCHEMA_ALPHA), result);
    }

    public void testCollectType() throws Exception {
        Type t = makeTerm("arb as poly(poly('a, 'b), poly(set('a), int))").getType();
        Set<TypeVariable> result = TypeVariableCollector.collect(t);
        
        HashSet<TypeVariable> expected = new HashSet<TypeVariable>();
        expected.add(TypeVariable.ALPHA);
        expected.add(TypeVariable.BETA);
        
        assertEquals(expected, result);
    }
    
    // was a bug
    public void testCollectInTypeBinding() throws Exception {
        Term t = makeTerm("(\\T_all 'a; true)");
        Set<TypeVariable> result = TypeVariableCollector.collect(t);
        assertEquals(Collections.singleton(TypeVariable.ALPHA), result); 
        
        t = makeTerm("(\\T_all %'a; true)");
        Set<SchemaType> schemaresult = TypeVariableCollector.collectSchema(t);
        assertEquals(Collections.singleton(SCHEMA_ALPHA), schemaresult);
    }

}
