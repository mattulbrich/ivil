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

import java.util.Set;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.SchemaVariable;

public class TestSchemaCollectorVisitor extends TestCaseWithEnv {

    public void testCollectInModality() throws Exception {
        SchemaCollectorVisitor scv = new SchemaCollectorVisitor();
        
        scv.collect(makeTerm("{ i1 := %i }[ %a : goto %n, %k]"));
        Set<SchemaVariable> schemaVariables = scv.getSchemaVariables();
        //System.out.println(schemaVariables);
        assertTrue(schemaVariables.contains(SchemaVariable.getInst("%i", Environment.getIntType())));
        assertTrue(schemaVariables.contains(SchemaVariable.getInst("%a", Environment.getBoolType())));
        assertTrue(schemaVariables.contains(SchemaVariable.getInst("%n", Environment.getIntType())));
        assertTrue(schemaVariables.contains(SchemaVariable.getInst("%k", Environment.getIntType())));
        
    }
    
    public void testProgramInUpdate() throws Exception {
        
        SchemaCollectorVisitor scv = new SchemaCollectorVisitor();
        
        scv.collect(makeTerm("{ b1 := [%a]}true"));
        assertTrue(scv.getSchemaVariables().contains(SchemaVariable.getInst("%a", Environment.getBoolType())));
    }
    
    public void testSchemaAssignment() throws Exception {
        SchemaCollectorVisitor scv = new SchemaCollectorVisitor();
        
        scv.collect(makeTerm("{ %i := 0 }true"));
        scv.collect(makeTerm("[ %a : %j := 0 ]"));
        assertTrue(scv.getSchemaVariables().contains(SchemaVariable.getInst("%i", Environment.getIntType())));
        assertTrue(scv.getSchemaVariables().contains(SchemaVariable.getInst("%j", Environment.getIntType())));
    }
}
