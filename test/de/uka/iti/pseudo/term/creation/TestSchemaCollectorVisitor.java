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
        assertTrue(schemaVariables.contains(new SchemaVariable("%i", Environment.getIntType())));
        assertTrue(schemaVariables.contains(new SchemaVariable("%a", Environment.getBoolType())));
        assertTrue(schemaVariables.contains(new SchemaVariable("%n", Environment.getIntType())));
        assertTrue(schemaVariables.contains(new SchemaVariable("%k", Environment.getIntType())));
        
    }
    
    public void testProgramInUpdate() throws Exception {
        
        SchemaCollectorVisitor scv = new SchemaCollectorVisitor();
        
        scv.collect(makeTerm("{ b1 := [%a]}true"));
        assertTrue(scv.getSchemaVariables().contains(new SchemaVariable("%a", Environment.getBoolType())));
    }
    
    public void testSchemaAssignment() throws Exception {
        SchemaCollectorVisitor scv = new SchemaCollectorVisitor();
        
        scv.collect(makeTerm("{ %i := 0 }true"));
        scv.collect(makeTerm("[ %a : %j := 0 ]"));
        assertTrue(scv.getSchemaVariables().contains(new SchemaVariable("%i", Environment.getIntType())));
        assertTrue(scv.getSchemaVariables().contains(new SchemaVariable("%j", Environment.getIntType())));
    }
}
