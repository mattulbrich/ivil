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

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.term.Type;

/*
 * Test cases are for functions only since binders, sorts, programs
 * follow precisely the same pattern.
 */
public class TestLocalSymbolTable extends TestCaseWithEnv {

    private Function f, f2, g;
    private SymbolTable lst;

    @Override
    protected void setUp() throws Exception {
        f = createFunction("lf");
        g = createFunction("lg");

        // same name for f and f2!
        f2 = createFunction("lf");

        lst = new SymbolTable(env);
        lst.addFunction(f);
        lst.addFunction(g);
    }

    private static Function createFunction(String name) throws EnvironmentException {
        return new Function(name, Environment.getIntType(),
                new Type[0], false, true, ASTLocatedElement.BUILTIN);
    }

    public void testFixed() throws Exception {

        assertFalse(lst.isFixed());

        lst.setFixed();
        assertTrue(lst.isFixed());

        try {
            lst.addFunction(g);
            fail("should have complained that already fixed");
        } catch (EnvironmentException e) {
            if(VERBOSE) {
                e.printStackTrace();
            }
        }
    }

    public void testEnsureOpenTable() throws Exception {

        assertTrue(lst == lst.ensureOpenTable());

        lst.setFixed();

        SymbolTable result = lst.ensureOpenTable();
        assertTrue(lst != result);
        assertEquals(lst, result);
        assertTrue(!result.isFixed());
    }

    public void testGetFunctions() {

        Function[] arr = { g, f };

        int i = 0;
        for (Function function : lst.getFunctions()) {
            assertTrue(arr[i] == function);
            i++;
        }
    }

    public void testGetFunction() {
        assertEquals(f, lst.getFunction("lf"));
        assertEquals(g, lst.getFunction("lg"));

        assertNotNull(lst.getFunction("i1"));
        assertNull(lst.getLocalFunction("i1"));

        assertNull(lst.getFunction("unknown"));
    }

    public void testAddFunction() throws EnvironmentException {

        Function h = createFunction("lh");
        lst.addFunction(h);

        Function[] arr = { h, g, f };

        int i = 0;
        for (Function function : lst.getFunctions()) {
            assertTrue(arr[i] == function);
            i++;
        }
    }

    public void testDoubleAddFunction() throws EnvironmentException {

        try {
            lst.addFunction(f);
            fail("f is already registered");
        } catch (EnvironmentException e) {
            if(VERBOSE) {
                e.printStackTrace();
            }
        }

        try {
            lst.addFunction(f2);
            fail("Name f is already registered");
        } catch (EnvironmentException e) {
            if(VERBOSE) {
                e.printStackTrace();
            }
        }

    }


    public void testAddAndCopy() throws Exception {

        int i;
        for (i = 0; i < 20; i++) {
            lst.addFunction(createFunction("tmp" + i));
            if(i % 5 == 0) {
                lst = new SymbolTable(lst);
            }
        }

        for (Function fun : lst.getFunctions()) {
            i--;
            if(i >= 0) {
                assertEquals("tmp" + i, fun.getName());
            }
        }

        assertEquals(-2, i);
    }

}
