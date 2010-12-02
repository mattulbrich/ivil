package de.uka.iti.pseudo.util;

import de.uka.iti.pseudo.TestCaseWithEnv;

public class TestAppendSet extends TestCaseWithEnv {
    
    public void testAdd() throws Exception {
        AppendSet<Integer> map = new AppendSet<Integer>();
        map.add(22);
        map.add(33);
        map.add(44);
        map.add(null);

        assertTrue(map.contains(44));
        assertTrue(map.contains(null));
        assertFalse(map.contains(55));
    }
    
    public void testSize() throws Exception {
        AppendSet<Integer> map = new AppendSet<Integer>();
        map.add(22);
        map.add(44);
        map.add(22);
        map.add(null);

        assertEquals(3, map.size());
    }
    
    public void testRemove() throws Exception {
        AppendSet<Integer> map = new AppendSet<Integer>();
        map.add(22);
        try {
            map.remove(22);
            fail("Remove not implemented");
        } catch (Exception e) {
            if(VERBOSE)
                e.printStackTrace();
        }
    }
    
    public void testClone() throws Exception {
        AppendSet<Integer> map = new AppendSet<Integer>();
        map.add(22);
        AppendSet<Integer> map2 = map.clone();
        map.add(44);
        map2.add(55);

        assertTrue(map.contains(22));
        assertTrue(map2.contains(22));
        assertTrue(map.contains(44));
        assertFalse(map2.contains(44));
        assertFalse(map.contains(55));
        assertTrue(map2.contains(55));
        
        assertEquals(2, map.size());
        assertEquals(2, map2.size());
    }

}
