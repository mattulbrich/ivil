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

import java.util.HashSet;
import java.util.Set;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.util.Util;

public class TestSchemaCollectorVisitor extends TestCaseWithEnv {

    // found a bug
    public void testCollectInModality() throws Exception {
        SchemaCollectorVisitor scv = new SchemaCollectorVisitor();
        
        scv.collect(makeTerm("{ i1 := %i }[ %a : goto %n, %k]%phi & [0;P]%psi"));
        Set<SchemaVariable> schemaVariables = scv.getSchemaVariables();
        SchemaVariable[] expected = {
                SchemaVariable.getInst("%i", Environment.getIntType()),
                SchemaVariable.getInst("%a", Environment.getBoolType()),
                SchemaVariable.getInst("%n", Environment.getIntType()),
                SchemaVariable.getInst("%k", Environment.getIntType()),
                SchemaVariable.getInst("%phi", Environment.getBoolType()),
                SchemaVariable.getInst("%psi", Environment.getBoolType())};
        
        assertEquals(Util.readOnlyArraySet(expected), schemaVariables);
    }
    
    public void testProgramInUpdate() throws Exception {
        
        SchemaCollectorVisitor scv = new SchemaCollectorVisitor();
        
        scv.collect(makeTerm("{ b1 := [%a]%phi}true"));
        SchemaVariable[] expected = {
                SchemaVariable.getInst("%a", Environment.getBoolType()),
                SchemaVariable.getInst("%phi", Environment.getBoolType())};
        
        assertEquals(Util.readOnlyArraySet(expected), scv.getSchemaVariables());
    }
    
    public void testSchemaAssignment() throws Exception {
        SchemaCollectorVisitor scv = new SchemaCollectorVisitor();
        
        scv.collect(makeTerm("{ %i := 0 }true"));
        scv.collect(makeTerm("[ %a : %j := 0 ]true"));
        assertTrue(scv.getSchemaVariables().contains(SchemaVariable.getInst("%i", Environment.getIntType())));
        assertTrue(scv.getSchemaVariables().contains(SchemaVariable.getInst("%j", Environment.getIntType())));
    }
}
