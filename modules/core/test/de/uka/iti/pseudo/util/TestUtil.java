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
package de.uka.iti.pseudo.util;

import java.util.Arrays;

import junit.framework.TestCase;

public class TestUtil extends TestCase {

    public void testJoin() throws Exception {
        assertEquals("a.1.c", Util.join(new Object[] { "a", 1, "c" }, "."));
        assertEquals("a.(null).b", Util.join(new Object[] { "a", null, "b" }, "."));
        assertEquals("a.b", Util.join(Arrays.asList("a", null, "b"), ".", true));
    }

    // stripQuotes and addQuotes belong together

    public void testQuotes() throws Exception {
        String string = "abc \" \\";
        String quoted = Util.addQuotes(string);
        String unquoted = Util.stripQuotes(quoted);

        assertEquals("\"abc \\\" \\\\\"", quoted);
        assertEquals(string, unquoted);
    }

    public void testArrayLists() throws Exception {

        Integer array[] = { 0, 1, 2, 3, 4, 5 };

        assertEquals(Arrays.asList(0,1,2,3,4,5), Util.readOnlyArrayList(array));
        assertEquals(Arrays.asList(1,2,3,4,5), Util.readOnlyArrayList(array, 1, 6));
        assertEquals(Arrays.asList(2), Util.readOnlyArrayList(array, 2, 3));

        int counter = 1;
        for (Integer val : Util.readOnlyArrayList(array, 1, 5)) {
            assertEquals((Integer)counter, val);
            counter ++;
        }
        assertEquals(5, counter);
    }

}
