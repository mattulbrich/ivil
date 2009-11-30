package de.uka.iti.pseudo.justify;

import java.util.Map;
import java.util.Set;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.BindableIdentifier;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

public class TestSchemaVariableUseVisitor extends TestCaseWithEnv {

    // see extractor.p
	public void testCollection() throws Exception {
		
		Term t = makeTerm("(\\forall c; (\\forall %x; %x>0 & %a & %b & %d) & %a) & %c & %d " +
				"| (\\exists e; true)");
		
		SchemaVariableUseVisitor svtuv = new SchemaVariableUseVisitor();
		t.visit(svtuv);
		
		// Schema vars used as bindings: [%x]
		Set<BindableIdentifier> binds = svtuv.getBoundIdentifiers();
		assertEquals(svInt("%x"), binds.iterator().next());
		assertEquals(1, binds.size());

		// Schema vars to seen bindables
		Map<SchemaVariable, Set<BindableIdentifier>> map = svtuv.getSeenBindablesMap();
		System.out.println(map);
		assertEquals(5, map.size());
		assertEquals("[c]",     map.get(sv("%a")).toString());
		assertEquals("[%x, c]", map.get(sv("%b")).toString());
		assertEquals("[]",      map.get(sv("%c")).toString());
		assertEquals("[]",      map.get(sv("%d")).toString());
		assertEquals("[%x, c]", map.get(svInt("%x")).toString());
		
		// collect all bound variables
		assertEquals("[%x, c, e]", svtuv.getBoundIdentifiers().toString());
	}

	private SchemaVariable sv(String string) throws TermException {
		return new SchemaVariable(string, Environment.getBoolType());
	}
	
	private SchemaVariable svInt(String string) throws TermException {
		return new SchemaVariable(string, Environment.getIntType());
	}
}
