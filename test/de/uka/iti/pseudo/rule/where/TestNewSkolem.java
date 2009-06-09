package de.uka.iti.pseudo.rule.where;

import java.util.Properties;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.creation.TermUnification;

public class TestNewSkolem extends TestCaseWithEnv {

    public void testTryNewSkolem() throws Exception {
        NewSkolem newSK = new NewSkolem();
        
        Term[] t = { makeTerm("%i as int") };
        newSK.checkSyntax(t);
        
        try {
            Term[] t2 = { makeTerm("true") };
            newSK.checkSyntax(t2);
            fail("should fail");
        } catch(RuleException ex) {
        }
        
        try {
            Term[] t2 = { makeTerm("%i"), makeTerm("%b") };
            newSK.checkSyntax(t2);
            fail("should fail");
        } catch(RuleException ex) {
        }
    }
    
    RuleApplication mockRuleApp = new RuleApplicationMaker() {
        @Override public String getWhereProperty(String key) {
            return "skolemName(%i)".equals(key) ? "sk100" : null;
        }
    };
    
    public void testNewSkolem() throws Exception {
        NewSkolem newSK = new NewSkolem();
        Term schema = makeTerm("%i as int");
        Term[] t = { schema };
        newSK.checkSyntax(t);
        
        TermUnification mc = new TermUnification();
        WhereClause wc = new WhereClause(newSK, t);
        Properties properties = new Properties();
        
        newSK.applyTo(t, mc, mockRuleApp, null, env, properties, false);
        
        assertEquals(makeTerm("%i as int"), mc.instantiate(schema));
        
        newSK.applyTo(t, mc, mockRuleApp, null, env, properties, true);
        
        assertEquals(makeTerm("sk1 as int"), mc.instantiate(schema));
        assertEquals("sk1", properties.get("skolemName(%i)"));
        assertEquals("int", properties.get("skolemType(%i)"));
        
        loadEnv();
        mc = new TermUnification();
        
        newSK.applyTo(t, mc, mockRuleApp, null, env, null, true);
        assertEquals(makeTerm("sk1 as int"), mc.instantiate(schema));
    }
    
    public void testNewSkolemImportAndVerify() throws Exception {
        NewSkolem newSK = new NewSkolem();
        
        //
        // on import
        
        Properties properties = new Properties();
        properties.put("skolemName(%i)", "sk100");
        properties.put("skolemType(%i)", "int");
        Term[] formal = { makeTerm("%i as int") };
        
        newSK.wasImported(formal, env, properties);
        
        //
        // on verify

        Term[] actual = { makeTerm("sk100") }; 
        newSK.verify(formal, actual, properties);

        try {
            newSK.verify(formal, new Term[] { makeTerm("arb as int") }, properties);
            fail("should have failed");
        } catch (RuleException e) {
            // expected
        }
    }
}
