package de.uka.iti.pseudo.util.settings;

import java.util.NoSuchElementException;

import de.uka.iti.pseudo.gui.Main;

import junit.framework.TestCase;

public class TestSettings extends TestCase {
    
    Settings s;
    
    @Override protected void setUp() throws Exception {
        s = Settings.getInstance();
        s.clear();
    }
    
    public void testSysDir() throws Exception {
        String dir = s.getExpandedProperty(Main.SYSTEM_DIRECTORY_KEY);
        assertEquals("./sys", dir);
    }
    
    public void testGetExpandedString() throws Exception {
        
        s.put("abc", "value");
        s.put("def", "embeds ${abc}");
        s.put("rec", "${rec}");
        
        assertEquals("value", s.getExpandedProperty("abc"));
        assertEquals("embeds value", s.getExpandedProperty("def"));
        
        // no recursion
        assertEquals("${rec}", s.getExpandedProperty("rec"));
    }
    
    public void testGetPropertyString() {
        
        s.put("abc", "value");
        
        assertEquals("value", s.getProperty("abc"));
        
        try {
            s.getProperty("certainly_undefined_key");
            fail("Undefined key: should have failed");
        } catch(NoSuchElementException ex) {
        }
    }
    
    
    public void testGetInteger() {
     
        s.put("i1", "123");
        s.put("i2", "0x42");
        s.put("i3", "no number");
        
        assertEquals(123, s.getInteger("i1"));
        assertEquals(0x42, s.getInteger("i2"));
        
        try {
            assertEquals(0, s.getInteger("i3"));  
            fail("No number: should have failed");
        } catch(NumberFormatException ex) {
        }
    }
}
