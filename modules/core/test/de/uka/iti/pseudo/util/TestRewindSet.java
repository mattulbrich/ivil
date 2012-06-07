package de.uka.iti.pseudo.util;

import java.util.Arrays;

import junit.framework.TestCase;

public class TestRewindSet extends TestCase {

    public void testRewind() throws Exception {
        RewindSet<Integer> s = new RewindSet<Integer>();

        s.add(0);
        s.addAll(Arrays.asList(1,2,3));
        s.remove(3);

        assertEquals(3, s.size());
        assertFalse(s.contains(3));
        assertEquals(5, s.getRewindPosition());
        s.rewindTo(4);
        assertEquals(4, s.size());
        assertTrue(s.contains(3));
        assertEquals(4, s.getRewindPosition());
    }

    public void testRewindNull() throws Exception {
        RewindSet<Integer> s = new RewindSet<Integer>();

        s.add(null);
        assertTrue(s.contains(null));
        s.rewindTo(0);
        assertFalse(s.contains(null));
    }
}
