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
        
        newSK.applyTo(wc, mc, mockRuleApp, null, env, properties);
        
        assertEquals(makeTerm("sk1 as int"), mc.instantiate(schema));
        assertEquals("sk1", properties.get("skolemName(%i)"));
        
        loadEnv();
        mc = new TermUnification();
        
        newSK.applyTo(wc, mc, mockRuleApp, null, env, null);
        assertEquals(makeTerm("sk1 as int"), mc.instantiate(schema));
    }
    
    public void testNewSkolemImport() throws Exception {
        NewSkolem newSK = new NewSkolem();
        Term schema = makeTerm("%i as int");
        Term[] t = { schema };
        newSK.checkSyntax(t);
        
        TermUnification mc = new TermUnification();
        WhereClause wc = new WhereClause(newSK, t);
        Properties properties = new Properties();
        
        // after import the following might be set:
        env.addFunction(new Function("sk100", Environment.getIntType(), new Type[0], false, false, ASTLocatedElement.BUILTIN));
        mc.addInstantiation((SchemaVariable) schema, makeTerm("sk100"));
        
        newSK.applyTo(wc, mc, mockRuleApp, null, env, properties);
        
        assertEquals(makeTerm("sk100 as int"), mc.instantiate(schema));
    }
}
