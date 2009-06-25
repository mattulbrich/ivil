package de.uka.iti.pseudo.util;

import junit.framework.TestCase;

public class TestUtil extends TestCase {

    public void testJoin() throws Exception {
        
        assertEquals("a.1.c", Util.join(new Object[] { "a", 1, "c" }, "."));
        assertEquals("a.null", Util.join(new Object[] { "a", null }, "."));
    }
    
}
