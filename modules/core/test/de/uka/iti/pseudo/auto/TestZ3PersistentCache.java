package de.uka.iti.pseudo.auto;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import junit.framework.TestCase;
import de.uka.iti.pseudo.auto.DecisionProcedure.Result;
import de.uka.iti.pseudo.util.Util;
import de.uka.iti.pseudo.util.settings.Settings;

public class TestZ3PersistentCache extends TestCase {

    private static final String CHALLENGE_PLAIN =
            "This is the date\nThis is the challenge\n";

    // Computed elsewhere
    private static final String CHALLENGE_SHA =
            "283ea068183a72666a1f8ba9125605c0a74d4b4fe994bd455085db0bad44eb32";

    private File createTemp() throws IOException {
        File tempFile = File.createTempFile("ivil-z3cache-", ".tmp");
        tempFile.deleteOnExit();
        return tempFile;
    }

    public void testGlobalInstance() throws Exception {
        File tempFile = createTemp();
        Settings.getInstance().put(Z3PersistentCache.KEY_PERSISTENT_CACHE,
                tempFile.getAbsolutePath());

        Z3PersistentCache gi = Z3PersistentCache.getGlobalInstance();
        assertNotNull(gi);
    }

    public void testInformAndRetrieve() throws Exception {

        String c1 = "Date A\nThis is the challenge";
        String c2 = "Date BBB\nThis is the challenge";
        String c3 = "Date A\nThis has changed";

        Z3PersistentCache cache = new Z3PersistentCache(null);

        cache.put(c1, Result.VALID);

        assertEquals(Result.VALID, cache.lookup(c2));
        assertNull(cache.lookup(c3));
    }

    public void testWriteBack() throws Exception {
        File tempFile = createTemp();

        Z3PersistentCache cache = new Z3PersistentCache(tempFile.getAbsolutePath());
        cache.put(CHALLENGE_PLAIN, Result.VALID);
        cache.writeBack();

        String content = Util.readFileAsString(tempFile);
        assertEquals(CHALLENGE_SHA + ":VALID", content.trim());
    }

    public void testRead() throws Exception {
        File tempFile = createTemp();

        FileWriter fw = new FileWriter(tempFile);
        fw.write(CHALLENGE_SHA + ":VALID\n");
        fw.close();

        Z3PersistentCache cache = new Z3PersistentCache(tempFile.getAbsolutePath());
        assertEquals(Result.VALID, cache.lookup(CHALLENGE_PLAIN));
    }

    public void testChange() throws Exception {
        File tempFile = createTemp();

        Z3PersistentCache cache = new Z3PersistentCache(tempFile.getAbsolutePath());
        assertNull(cache.lookup(CHALLENGE_PLAIN));
        cache.put(CHALLENGE_PLAIN, Result.VALID);
        assertEquals(Result.VALID, cache.lookup(CHALLENGE_PLAIN));

        String pseudoLine1 = Util.duplicate("1", 64) + ":VALID";
        String pseudoLine9 = Util.duplicate("9", 64) + ":VALID";

        // in the meantime, the file changes
        FileWriter fw = new FileWriter(tempFile);
        fw.write(pseudoLine1 + "\n");
        fw.write(pseudoLine9 + "\n");
        fw.close();

        cache.writeBack();
        String[] lines = Util.readFileAsString(tempFile).split("\n");
        assertEquals(pseudoLine1, lines[0]);
        assertEquals(CHALLENGE_SHA + ":VALID", lines[1]);
        assertEquals(pseudoLine9, lines[2]);
    }
}
