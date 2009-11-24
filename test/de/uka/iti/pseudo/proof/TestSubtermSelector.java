package de.uka.iti.pseudo.proof;

import java.util.Arrays;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.term.Term;

public class TestSubtermSelector extends TestCaseWithEnv {

    public void testHashCode() {
        SubtermSelector t1 = new SubtermSelector(4,3,2,1,2);
        SubtermSelector t2 = new SubtermSelector(4,3,2,1,2);
        SubtermSelector t3 = new SubtermSelector(4,3,1,2,2);
        
        assertEquals(t1.hashCode(), t2.hashCode());
        assertFalse(t1.hashCode() == t3.hashCode());
    }

    public void testSubtermSelectorIntArray() {
        SubtermSelector t1 = new SubtermSelector(4,3,2,1,2);
        
        assertEquals(5, t1.getDepth());
        
        int i = 0;
        assertEquals(4, t1.getSubtermNumber(i++));
        assertEquals(3, t1.getSubtermNumber(i++));
        assertEquals(2, t1.getSubtermNumber(i++));
        assertEquals(1, t1.getSubtermNumber(i++));
        assertEquals(2, t1.getSubtermNumber(i++));
    }
    
    public void testSubtermSelectorEmpty() throws Exception {
        SubtermSelector t1 = new SubtermSelector();
        assertEquals(0, t1.getDepth());
    }

    public void testSubtermSelectorSubtermSelectorInt() {
        SubtermSelector t1 = new SubtermSelector(4,3);
        SubtermSelector t2 = new SubtermSelector(t1, 5);
        SubtermSelector t3 = new SubtermSelector(4, 3, 5);
        
        assertEquals(t3, t2);
    }

    public void testSubtermSelectorString() throws FormatException {
        assertEquals(new SubtermSelector(), new SubtermSelector(""));
        assertEquals(new SubtermSelector(4), new SubtermSelector("4"));
        assertEquals(new SubtermSelector(4,3,2), new SubtermSelector("4.3.2"));
        
        try {
            new SubtermSelector("4.");
            fail("Should fail");
        } catch (FormatException e) {
        }
        
        try {
            new SubtermSelector(".4");
            fail("Should fail");
        } catch (FormatException e) {
        }
        
        try {
            new SubtermSelector(".");
            fail("Should fail");
        } catch (FormatException e) {
        }
        
        try {
            new SubtermSelector("4 . 4");
            fail("Should fail");
        } catch (FormatException e) {
        }
        
        try {
            new SubtermSelector(" ");
            fail("Should fail");
        } catch (FormatException e) {
        }

    }

    public void testToString() throws FormatException {
        assertEquals("", new SubtermSelector().toString());
        assertEquals("4", new SubtermSelector(4).toString());
        assertEquals("4.3.2", new SubtermSelector("4.3.2").toString());
    }

    public void testGetDepth() throws FormatException {
        assertEquals(0, new SubtermSelector().getDepth());
        assertEquals(1, new SubtermSelector(4).getDepth());
        assertEquals(3, new SubtermSelector("4.3.2").getDepth());
    }

    public void testEqualsObject() {
        SubtermSelector t1 = new SubtermSelector(4,3,2,1,2);
        SubtermSelector t2 = new SubtermSelector(4,3,2,1,2);
        SubtermSelector t3 = new SubtermSelector(4,3,1,2,2);
        
        assertEquals(t1, t2);
        assertFalse(t1.equals(t3));
    }

    public void testGetPath() {
        SubtermSelector t1 = new SubtermSelector(4, 5, 2);
        assertEquals(Arrays.asList(4,5,2), t1.getPath());
    }

    public void testGetLinearIndex() throws Exception {
        Term t = makeTerm("g(g(i1,i2),f(i3))");
        
        // t
        SubtermSelector t1 = new SubtermSelector();
        
        // f(i3)
        SubtermSelector t2 = new SubtermSelector(1);
        
        // i2
        SubtermSelector t3 = new SubtermSelector(0, 1);
        
        assertEquals(0, t1.getLinearIndex(t));
        assertEquals(4, t2.getLinearIndex(t));
        assertEquals(3, t3.getLinearIndex(t));
    }

    public void testSelectSubtermTerm() throws Exception {
        Term t = makeTerm("g(g(i1,i2),f(i3))");
        
        // t
        SubtermSelector t1 = new SubtermSelector();
        
        // f(i3)
        SubtermSelector t2 = new SubtermSelector(1);
        
        // i2
        SubtermSelector t3 = new SubtermSelector(0, 1);
        
        assertEquals(t, t1.selectSubterm(t));
        assertEquals(t.getSubterm(1), t2.selectSubterm(t));
        assertEquals(t.getSubterm(0).getSubterm(1), t3.selectSubterm(t));
    }

}
