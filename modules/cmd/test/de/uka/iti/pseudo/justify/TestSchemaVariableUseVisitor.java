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
package de.uka.iti.pseudo.justify;

import java.util.Map;
import java.util.SortedSet;

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
				"| (\\exists %e; true)");
		
		SchemaVariableUseVisitor svtuv = new SchemaVariableUseVisitor();
		t.visit(svtuv);
		
		boolean oldShow = Term.SHOW_TYPES;
		Term.SHOW_TYPES = false;
		
		// Schema vars to seen bindables
		Map<SchemaVariable, SortedSet<BindableIdentifier>> map = svtuv.getSeenBindablesMap();
		// System.out.println(map);
		assertEquals(5, map.size());
		assertEquals("[\\var c]"    , map.get(sv("%a")).toString());
		assertEquals("[%x, \\var c]", map.get(sv("%b")).toString());
		assertEquals("[]",            map.get(sv("%c")).toString());
		assertEquals("[]",            map.get(sv("%d")).toString());
		assertEquals("[%x, \\var c]", map.get(svInt("%x")).toString());
		
		// collect all bound variables
		assertEquals("[%e, %x, \\var c]", svtuv.getBoundIdentifiers().toString());
		
		Term.SHOW_TYPES = oldShow;
	}
	
	private SchemaVariable sv(String string) throws TermException {
		return new SchemaVariable(string, Environment.getBoolType());
	}
	
	private SchemaVariable svInt(String string) throws TermException {
		return new SchemaVariable(string, Environment.getIntType());
	}
}
