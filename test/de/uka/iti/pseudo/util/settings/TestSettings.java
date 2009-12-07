package de.uka.iti.pseudo.util.settings;

import java.awt.Color;

import junit.framework.TestCase;
import de.uka.iti.pseudo.gui.Main;

public class TestSettings extends TestCase {
    
    Settings s;
    
    @Override protected void setUp() throws Exception {
        s = Settings.getInstance();
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
    
    public void testGetPropertyString() throws Exception {
        
        s.put("abc", "value");
        
        assertEquals("value", s.getProperty("abc"));
        
        try {
            s.getProperty("certainly_undefined_key");
            fail("Undefined key: should have failed");
        } catch(SettingsException ex) {
        }
    }
    
    public void testGetInteger() throws Exception {
     
        s.put("i1", "123");
        s.put("i2", "0x42");
        s.put("i3", "no number");
        
        assertEquals(123, s.getInteger("i1"));
        assertEquals(0x42, s.getInteger("i2"));
        
        try {
            s.getInteger("i3");  
            fail("No number: should have failed");
        } catch(SettingsException ex) {
        }
        
    }
    
    public void testGetColor() throws Exception {
        
        s.put("c1", "#010203");
        s.put("c2", "sandy brown");
        s.put("c3", "certainly_undefined_color_name");
        
        assertEquals(new Color(0x010203), s.getColor("c1"));
        assertEquals(new Color(244, 164, 96), s.getColor("c2"));
        
        try {
            s.getColor("c3");  
            fail("No color: should have failed");
        } catch(SettingsException ex) {
        }
    }
}
