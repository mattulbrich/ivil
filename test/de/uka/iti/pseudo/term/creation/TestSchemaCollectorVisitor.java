package de.uka.iti.pseudo.term.creation;

import java.util.Set;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.SchemaModality;
import de.uka.iti.pseudo.term.SchemaVariable;

public class TestSchemaCollectorVisitor extends TestCaseWithEnv {

    public void testCollectInModality() throws Exception {
        SchemaCollectorVisitor scv = new SchemaCollectorVisitor();
        
        scv.collect(makeTerm("[ i1 := %i ; while %b do if %b1 then skip end end]true"));
        Set<SchemaVariable> schemaVariables = scv.getSchemaVariables();
        assertTrue(schemaVariables.contains(new SchemaVariable("%i", Environment.getIntType())));
        assertTrue(schemaVariables.contains(new SchemaVariable("%b", Environment.getBoolType())));
        assertTrue(schemaVariables.contains(new SchemaVariable("%b1", Environment.getBoolType())));
        
    }
    
    public void testModalityInModality() throws Exception {
        
        SchemaCollectorVisitor scv = new SchemaCollectorVisitor();
        
        scv.collect(makeTerm("[ i1 := [ &a ]i1 ]true"));
        assertTrue(scv.getSchemaModalities().contains(new SchemaModality("&a")));
    }
}
