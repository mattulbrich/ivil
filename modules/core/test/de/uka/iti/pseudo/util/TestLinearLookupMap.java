package de.uka.iti.pseudo.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.uka.iti.pseudo.TestCaseWithEnv;

public class TestLinearLookupMap extends TestCaseWithEnv {
    
    static HashMap<String, String> HASH_MAP = new HashMap<String,String>();
    static {
        HASH_MAP.put("Hello", "World");
        HASH_MAP.put("Null", null);
        HASH_MAP.put("something", "else");
    }
    
    private LinearLookupMap<String, String> makeMap() {
        return new LinearLookupMap<String, String>(HASH_MAP);
    }

    public void testUnmodifiable() throws Exception {

        Map<String, String> map = new LinearLookupMap<String, String>(
                Collections.<String, String> emptyMap());
        
        try {
            map.clear();
            fail("clear should fail");
        } catch(UnsupportedOperationException e) {
            if(VERBOSE)
                e.printStackTrace();
        }        
        
        try {
            map.put("Hello", "World");
            fail("put should fail");
        } catch(UnsupportedOperationException e) {
            if(VERBOSE)
                e.printStackTrace();
        }
        
        try {
            map.putAll(Collections.<String,String>emptyMap());
            fail("putAll should fail");
        } catch(UnsupportedOperationException e) {
            if(VERBOSE)
                e.printStackTrace();
        }
        
        try {
            map.remove("Test");
            fail("remove should fail");
        } catch(UnsupportedOperationException e) {
            if(VERBOSE)
                e.printStackTrace();
        }
    }
    
    public void testGetter() throws Exception {
        LinearLookupMap<String, String> map = makeMap();
        Map<String, String> emptyMap = new LinearLookupMap<String, String>(
                Collections.<String, String> emptyMap());
        
        assertEquals("World", map.get("Hello"));
        assertNull(map.get("Null"));
        assertNull(map.get("unknown"));
        
        assertEquals(3, map.size());
        assertEquals(0, emptyMap.size());
        
        assertTrue(emptyMap.isEmpty());
        assertFalse(map.isEmpty());
        
        assertTrue(map.containsKey("Hello"));
        assertFalse(map.containsKey("unknown"));
        
        // found a NPE :)
        assertTrue(map.containsValue("World"));
        assertTrue(map.containsValue(null));
        assertFalse(map.containsValue("unknown"));
        
        assertEquals(HASH_MAP.entrySet(), map.entrySet());
        assertEquals(HASH_MAP.keySet(), map.keySet());
//wont work        assertEquals(HASH_MAP.values(), map.values());
    }
    
    public void testIterator() throws Exception {
        LinearLookupMap<String, String> map = makeMap();
        Set<Entry<String, String>> entries = map.entrySet();
        try {
            entries.iterator().remove();
            fail("remove should fail");
        } catch (UnsupportedOperationException e) {
            if(VERBOSE)
                e.printStackTrace();
        }
    }
    
    public void testToString() {
        assertEquals(HASH_MAP.toString(), makeMap().toString());
    }

}
