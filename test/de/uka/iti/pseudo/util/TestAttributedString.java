package de.uka.iti.pseudo.util;

import junit.framework.TestCase;

public class TestAttributedString extends TestCase {

    public TestAttributedString(String name) {
        super(name);
    }


    public void testOne() {
        AttributedString<Integer> as = new AttributedString<Integer>();
        as.append("0123").begin(1).append("456").begin(2).append("789").end().end();
        assertEquals(4, as.getBegin(5));
        assertEquals(10, as.getEnd(5));
        assertEquals(0, as.getBegin(0));
        assertEquals(10, as.getEnd(0));
        assertNull(as.getAttributeAt(0));
        assertEquals((Integer)2, as.getAttributeAt(8));
    }
    
    public void testHasEmptyStack() {
        AttributedString<Integer> as = new AttributedString<Integer>();
        as.append("0123").begin(1).append("456").begin(2).append("789").end();
        assertFalse(as.hasEmptyStack());
        as.end();
        assertTrue(as.hasEmptyStack());
    }
}
