package de.uka.iti.pseudo.util;

import junit.framework.TestCase;
import de.uka.iti.pseudo.util.ConcurrentSoftHashCacheImpl.Chain;

public class TestConcurrentSoftHashCache extends TestCase {

    public void testPutGet() {
        
        ConcurrentSoftHashCacheImpl c = new ConcurrentSoftHashCacheImpl(2, 4);
        Integer[] values = new Integer[1000];
        
        for(int i = 0; i < values.length; i++) {
            values[i] = Integer.valueOf(i);
            c.put(values[i]);
        }
        
        assertEquals(1000, c.size());
        
        for(int i = 0; i < 1000; i++) {
            assertSame(values[i], c.get(values[i]));
        }
    }
    
    public void testSegments() {
        
        ConcurrentSoftHashCacheImpl c = new ConcurrentSoftHashCacheImpl(3, 4);
        
        for(int i = 0; i < 16; i++) {
            c.put(Integer.valueOf(i << 3));
        }
        
        assertEquals(16, c.size());
        assertEquals(16, c.segments[0].count);
        
        for (int i = 1; i < 8; i++) {
            assertEquals(0, c.segments[i].count);
        }
        
        Chain[] table = c.segments[0].table;
        for (int i = 1; i < table.length; i++) {
            assertTrue(table[i] == null || table[i].next == null);
        }
        
    }
    
    public void testSize0() throws Exception {
        ConcurrentSoftHashCacheImpl c = new ConcurrentSoftHashCacheImpl(0, 4);
        c.put(1000000);
        c.put(100000);
        assertEquals(1, c.segments.length);
        assertEquals(16, c.segments[0].table.length);
        assertEquals(2, c.size());
    }
    
    private void testStale(int pos) {
        ConcurrentSoftHashCacheImpl c = new ConcurrentSoftHashCacheImpl(0, 5);
        
        for(int i = 0; i < 20; i++) {
            c.put(new Integer(i << 5));
        }

//        assertEquals(32, c.segments[0].table.length);
//        assertEquals(20, c.segments[0].table[0].len());
//        Chain ch = c.segments[0].table[0];
//        for(int i = 0; i < 20; i++) {
//            assertEquals(i << 5, ch.get());
//            ch = ch.next;
//        }
        
        //
        // simulate garbage collection: 1st element.
        Chain ch = c.segments[0].table[0];
        for (int i = 0; i < pos; i++) {
            ch = ch.next;
        }
        
        ch.clear();
        ch.enqueue();
        c.segments[0].removeStale();
        
        assertEquals(32, c.segments[0].table.length);
        assertEquals(19, c.segments[0].table[0].len());
        ch = c.segments[0].table[0];
        for(int i = 0; i < 20; i++) {
            if(i == pos)
                continue;
            
            assertEquals(i << 5, ch.get());
            ch = ch.next;
        }
    }
    
    public void testStaleHead() {
        testStale(0);
    }
    
    public void testStaleSecond() {
        testStale(1);
    }
    
    public void testStaleMiddle() {
        testStale(10);
    }

    public void testStaleLast() {
        testStale(19);
    }
    
    public void testClear() throws Exception {
        ConcurrentSoftHashCacheImpl c = new ConcurrentSoftHashCacheImpl(3, 4);
        
        for(int i = 0; i < 100000; i++) {
            c.put(Integer.valueOf(i));
        }
        
        c.clear();
        
        assertEquals(0, c.size());
        for (int i = 0; i < c.segments.length; i++) {
            assertEquals(0, c.segments[i].count);
            assertEquals(16, c.segments[i].table.length);
            for (int j = 0; j < c.segments[i].table.length; j++) {
                assertNull(c.segments[i].table[j]);
            }
        }
    }
    
    public void testArbitraryHashes() throws Exception {
        ConcurrentSoftHashCacheImpl c =  new ConcurrentSoftHashCacheImpl(3, 4);
        
        for(int i = 0; i < 100000; i++) {
            c.put("This is a string " + i + " : " + i);
        }
    }

}
