package de.uka.iti.pseudo.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;
import de.uka.iti.pseudo.TestCaseWithEnv;

public class TestRewindMap extends TestCase {

    public void testPutGet() throws Exception {

        RewindMap<Integer, Integer> m =
                new RewindMap<Integer, Integer>(new HashMap<Integer, Integer>());

        Integer r1 = m.put(0, 0);
        Integer r2 = m.put(1, 2);
        Integer r3 = m.put(0, 1);

        assertEquals(null, r1);
        assertEquals(null, r2);
        assertEquals((Integer) 0, r3);

        assertEquals((Integer) 2, m.get(1));
        assertTrue(m.containsKey(1));
        assertEquals((Integer) 1, m.get(0));
        assertTrue(m.containsKey(0));
        assertEquals(null, m.get(4));
        assertFalse(m.containsKey(4));
    }

    public void testPutRemove() throws Exception {
        RewindMap<Integer, Integer> m =
                new RewindMap<Integer, Integer>(new HashMap<Integer, Integer>());

        m.put(0, 0);
        m.put(1, 2);
        Integer r3 = m.remove(0);
        Integer r4 = m.remove(4);

        assertEquals((Integer) 2, m.get(1));
        assertEquals(null, m.get(0));
        assertEquals(null, m.get(4));

        assertEquals((Integer) 0, r3);
        assertEquals(null, r4);
    }

    public void testRewind() throws Exception {
        RewindMap<Integer, Integer> m =
                new RewindMap<Integer, Integer>(new HashMap<Integer, Integer>());

        m.put(0, 0);
        m.put(1, 2);
        m.put(1, 3);
        m.remove(1);

        assertFalse(m.containsKey(1));
        assertEquals(4, m.getRewindPosition());
        m.rewindTo(3);
        assertEquals((Integer)3, m.get(1));
        assertEquals(3, m.getRewindPosition());
        m.rewindTo(2);
        assertEquals((Integer)2, m.get(1));
        assertEquals(2, m.getRewindPosition());
        m.rewindTo(1);
        assertFalse(m.containsKey(1));
        assertEquals(1, m.getRewindPosition());
    }

    public void testRewindNull() throws Exception {
        RewindMap<Integer, Integer> m =
                new RewindMap<Integer, Integer>(new HashMap<Integer, Integer>());

        m.put(0, null);
        assertTrue(m.containsKey(0));
        assertEquals(null, m.get(0));
        m.rewindTo(0);
        assertFalse(m.containsKey(0));
        assertEquals(null, m.get(0));
    }

    public void testRandom() throws Exception {
        Random r = new Random(0x10003);

        RewindMap<Integer, Integer> m =
                new RewindMap<Integer, Integer>(new HashMap<Integer, Integer>());
        LinkedHashMap<Integer, Integer> n = new LinkedHashMap<Integer, Integer>();

        int x, y;
        Object r1, r2;

        for (int i = 0; i < 10000; i++) {
            TestCaseWithEnv.out(i + ": ");
            assertEquals(n, m);
            switch (r.nextInt(13)) {
            case 0:
            case 1:
            case 2:
                x = r.nextInt(20);
                r1 = m.remove(x);
                r2 = n.remove(x);
                TestCaseWithEnv.out("remove " + x);
                assertEquals(r2, r1);
                break;

            case 3:
                m.clear();
                n.clear();
                TestCaseWithEnv.out("clear");
                break;

            case 4:
                m.rewindTo(0);
                n.clear();
                TestCaseWithEnv.out("rewind");
                break;

            case 5:
                x = r.nextInt(20);
                r1 = m.put(x, null);
                r2 = n.put(x, null);
                TestCaseWithEnv.out("put " + x + ", null");
                assertEquals(r2, r1);
                break;

            default:
                x = r.nextInt(20);
                y = r.nextInt(20);
                r1 = m.put(x, y);
                r2 = n.put(x, y);
                TestCaseWithEnv.out("put " + x + ", " + y);
                assertEquals(r2, r1);
                break;
            }
            TestCaseWithEnv.out(m);
            TestCaseWithEnv.out(m.size());
            TestCaseWithEnv.out(n);
            TestCaseWithEnv.out(n.size());
            iteratorFaithful(m.entrySet());
            assertEquals(m, n);
        }
    }

    private <E> void iteratorFaithful(Set<E> set) {
        int size = set.size();
        Iterator<E> it = set.iterator();
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        assertEquals(size, count);
    }

    //
    // private <K,V> void assertIteratorEquals(Map<K, V> n, Map<K, V> m) {
    //
    // Iterator<Entry<K, V>> it1 = n.entrySet().iterator();
    // Iterator<Entry<K, V>> it2 = m.entrySet().iterator();
    //
    // while(it1.hasNext()) {
    // assertTrue("has also next", it2.hasNext());
    // Entry<K, V> e1 = it1.next();
    // Entry<K, V> e2 = it2.next();
    // assertEquals("both keys equal", e2.getKey(), e1.getKey());
    // assertEquals("both elements equal", e2.getValue(), e1.getValue());
    // }
    //
    // assertFalse("has not more elements", it2.hasNext());
    // }

    // was a bug!
    public void testIterator() throws Exception {
        RewindMap<Integer, Integer> m =
                new RewindMap<Integer, Integer>(new HashMap<Integer, Integer>());
        m.put(0, 0);
        m.put(1, 1);
        m.remove(0);
        assertTrue(m.entrySet().iterator().hasNext());
    }

    public void testRewindSizes() throws Exception {

        Random r = new Random(0x10001);

        RewindMap<Integer, Integer> m =
                new RewindMap<Integer, Integer>(new HashMap<Integer, Integer>());

        int x, y;

        for (int i = 0; i < 10000; i++) {
            switch (r.nextInt(6)) {
            case 0:
                x = r.nextInt(20);
                m.remove(x);
                TestCaseWithEnv.out("remove " + x);
                break;

            case 1:
                m.clear();
                TestCaseWithEnv.out("clear");
                break;

            case 2:
                if(m.getRewindPosition() == 0) {
                    break;
                }
                x = r.nextInt(m.getRewindPosition());
                m.rewindTo(x);
                TestCaseWithEnv.out("rewind " + x);
                break;

            default:
                x = r.nextInt(20);
                y = r.nextInt(20);
                m.put(x, y);
                TestCaseWithEnv.out("put " + x + ", " + y);
                break;
            }
            TestCaseWithEnv.out(i + ": " + m);
            TestCaseWithEnv.out(m.size());
            iteratorFaithful(m.entrySet());
        }
    }

    public void testNoHistory() throws Exception {
        RewindMap<Integer, Integer> m =
                new RewindMap<Integer, Integer>();

        m.rewindTo(0);
    }
}
