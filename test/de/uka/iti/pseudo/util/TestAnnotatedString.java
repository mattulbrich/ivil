package de.uka.iti.pseudo.util;

import junit.framework.TestCase;

public class TestAnnotatedString extends TestCase {

    public TestAnnotatedString(String name) {
        super(name);
    }

    public void testOne() {
        AnnotatedString<Integer> as = new AnnotatedString<Integer>();
        as.append("0123").begin(1).append("456").begin(2).append("789").end()
                .end();
        assertEquals(4, as.getBegin(5));
        assertEquals(10, as.getEnd(5));
        assertEquals(0, as.getBegin(0));
        assertEquals(10, as.getEnd(0));
        assertNull(as.getAttributeAt(0));
        assertEquals((Integer) 2, as.getAttributeAt(8));
    }

    public void testHasEmptyStack() {
        AnnotatedString<Integer> as = new AnnotatedString<Integer>();
        as.append("0123").begin(1).append("456").begin(2).append("789").end();
        assertFalse(as.hasEmptyStack());
        as.end();
        assertTrue(as.hasEmptyStack());
    }

    public void testExampleInDoc() throws Exception {
        AnnotatedString<String> as = new AnnotatedString<String>();
        as.append("without Block ");
        as.begin("value1");
        as.append("text in 1 and ");
        as.begin("value2");
        as.append("this in nested block");
        as.end();
        as.end();
        
        assertEquals(null, as.getAttributeAt(0));
        assertEquals(-1, as.getAttributeIndexAt(0));
        assertEquals(null, as.getAttributeAt(13));
        assertEquals(-1, as.getAttributeIndexAt(13));
        assertEquals("value1", as.getAttributeAt(14));
        assertEquals(0, as.getAttributeIndexAt(14));
        assertEquals("value1", as.getAttributeAt(27));
        assertEquals(0, as.getAttributeIndexAt(27));
        assertEquals("value2", as.getAttributeAt(28));
        assertEquals(1, as.getAttributeIndexAt(28));
        assertEquals("value2", as.getAttributeAt(47));
        assertEquals(1, as.getAttributeIndexAt(47));
        assertEquals(null, as.getAttributeAt(48));
        assertEquals(-1, as.getAttributeIndexAt(48));
    }
}
