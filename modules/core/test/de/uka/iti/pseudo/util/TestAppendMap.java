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

import junit.framework.TestCase;
import de.uka.iti.pseudo.TestCaseWithEnv;

public class TestAppendMap extends TestCase {

    public void testAdd() throws Exception {
        AppendMap<String, Integer> map = new AppendMap<String, Integer>();
        map.put("22", 22);
        map.put("33", 33);
        map.put("44", 44);
        map.put("null", null);
        
        assertEquals((Integer)22, map.get("22"));
        assertEquals((Integer)33, map.get("33"));
        assertEquals((Integer)44, map.get("44"));
        assertEquals(null, map.get("null"));
        assertEquals(4, map.size());
        
        try {
            map.put(null, null);
            fail("putting null should fail");
        } catch (NullPointerException e) {
            if(TestCaseWithEnv.VERBOSE)
                e.printStackTrace();
        }
    }
    
    public void testClone() throws Exception {
        AppendMap<String, Integer> map = new AppendMap<String, Integer>();
        map.put("22", 22);
        
        AppendMap<String, Integer> map2 = map.clone();
        map.put("33", 33);
        
        assertEquals((Integer)33, map.get("33"));
        assertEquals(false, map2.containsKey("33"));
        assertEquals(2, map.size());
        assertEquals(1, map2.size());
    }
    
    public void testMultipleAdd() throws Exception {
        
        AppendMap<String, Integer> map = new AppendMap<String, Integer>();
        map.put("22", 22);
        map.put("22", 222);

        assertEquals((Integer)222, map.get("22"));
        assertEquals(1, map.size());
    }
    
    public void testComplicatedCopy() throws Exception {
        AppendMap<String, Integer> map = new AppendMap<String, Integer>();
        map.put("22", 22);
        map.put("33", 33);
        map.put("44", 44);
        
        AppendMap<String, Integer> map2 = map.clone();
        map2.put("44", 444);
        map2.put("22", 222);
        map2.put("55", 55);
        
        assertEquals(3, map.size());
        assertEquals(4, map2.size());
        
        assertEquals((Integer)222, map2.get("22"));
        assertEquals((Integer)444, map2.get("44"));
        assertEquals((Integer)55, map2.get("55"));
        assertEquals((Integer)33, map2.get("33"));
    }
    
    public void testRemove() throws Exception {
        AppendMap<String, Integer> map = new AppendMap<String, Integer>();
        map.put("22", 22);
        map.put("33", 33);
        map.put("44", 44);
        
        assertEquals((Integer)22, map.remove("22"));
        assertEquals(null, map.remove("55"));
        
        assertEquals((Integer)44, map.get("44"));
        assertEquals((Integer)33, map.get("33"));
        assertEquals(null, map.get("22"));
        
        assertEquals(2, map.size());
        
        assertEquals((Integer)44, map.remove("44"));
        assertEquals(null, map.get("44"));
        assertEquals((Integer)33, map.get("33"));
        
        assertEquals(1, map.size());
        
        try {
            map.remove(null);
            fail("Should raise NPE");
        } catch (NullPointerException e) {
            if(TestCaseWithEnv.VERBOSE)
                e.printStackTrace();
        }
    }
    
}
