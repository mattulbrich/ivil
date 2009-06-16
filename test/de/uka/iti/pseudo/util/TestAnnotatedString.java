package de.uka.iti.pseudo.util;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.gui.PrettyPrint;
import de.uka.iti.pseudo.term.Term;

public class TestAnnotatedString extends TestCaseWithEnv {

    public TestAnnotatedString() {
        super();
    }

    public void testOne() {
        AnnotatedString<Integer> as = new AnnotatedString<Integer>();
        as.append("0123").begin(1).append("456").begin(2).append("789").end()
                .end();
        assertEquals(4, as.getBeginAt(5));
        assertEquals(10, as.getEndAt(5));
        assertEquals(0, as.getBeginAt(0));
        assertEquals(10, as.getEndAt(0));
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
    
    // from a bug
    public void testTerm() throws Exception {
        Term t = makeTerm("b1 & b2 -> b2");
        AnnotatedString<Term> as = PrettyPrint.print(env, t);
        assertEquals(0, as.getAttributeIndexAt(8));
    }
    
    // example from the javadoc
    public void testStyled() throws Exception {
        AnnotatedStringWithStyles<String> as = 
            new AnnotatedStringWithStyles<String>();
        
        as.append("Hello ");
        as.setStyle("A");
        as.append("world ");
        as.setStyle("B");
        as.append("again ");
        as.resetPreviousStyle();
        as.append("and again");
        as.resetPreviousStyle();
        
        for (int i = 0; i < as.length(); i++) {
            System.out.println(as.charAt(i) + " " + as.getStyleAt(i));
        }
        
        assertEquals("", as.getStyleAt(0));
        assertEquals("", as.getStyleAt(5));
        assertEquals("A", as.getStyleAt(6));
        assertEquals("A B", as.getStyleAt(12));
        assertEquals("A", as.getStyleAt(18));
        assertEquals("", as.getStyleAt(27));
    }
    
    // from a bug
    public void testVariableTerm() throws Exception {
        Term t = makeTerm("(\\exists x; x>0)");
        PrettyPrint pp = new PrettyPrint(env, false);
        AnnotatedStringWithStyles<Term> as = pp.print(t);
        
        for (int i = 0; i < as.length(); i++) {
            System.out.println(as.charAt(i) + " " + as.getStyleAt(i));
        }
        
        assertEquals("", as.getStyleAt(0));
        assertEquals("variable", as.getStyleAt(9));
        assertEquals("", as.getStyleAt(10));
        assertEquals("variable", as.getStyleAt(12));
        assertEquals("", as.getStyleAt(13));
        
    }
    
    // miraculously the second "A" becomes " A"
    public void testStyles2() throws Exception {
        AnnotatedStringWithStyles<String> as = 
            new AnnotatedStringWithStyles<String>();
        
        as.append("1");
        as.setStyle("A");
        as.append("2");
        as.resetPreviousStyle();
        as.append("3");
        as.setStyle("A");
        as.append("4");
        as.resetPreviousStyle();

        assertEquals("", as.getStyleAt(0));
        assertEquals("A", as.getStyleAt(1));
        assertEquals("", as.getStyleAt(2));
        assertEquals("A", as.getStyleAt(3));

    }
}
