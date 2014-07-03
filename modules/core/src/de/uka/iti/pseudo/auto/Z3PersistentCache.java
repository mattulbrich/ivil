/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.auto.DecisionProcedure.Result;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.settings.Settings;

/**
 * The Class Z3PersistentCache provides a file-backed cache for results of Z3
 * invocations.
 *
 * The implementation maps sha256-fingerprints of challenges to the result that
 * they gave.
 *
 * While a {@link Z3PersistentCache} can be instantiated at any time, there is
 * also a distinct global instance whose backup storage is determined by the key
 * {@value #KEY_PERSISTENT_CACHE} in {@link Settings}.
 *
 * In the file backstore, entries are stored as lines, the hash followed by a
 * colon and then {@link Result#VALID "VALID"} or {@link Result#NOT_VALID
 * "NOT_VALID"}. {@link Result#UNKNOWN} is not considered.
 *
 * <pre>
 * 283ea068183a72666a1f8ba9125605c0a74d4b4fe994bd455085db0bad44eb32:VALID
 * 45c0c0d23f7fb31894a30202c0a0dd190cc3296c545984ab832c152a70d2ffc0:NOT_VALID
 * </pre>
 *
 * Since the first line of a challenge is a comment containing the date, it is
 * not included in the sha hash sum.
 */
public class Z3PersistentCache {

    /**
     * The key in the settings pointing to the global persistent cache.
     */
    public static final String KEY_PERSISTENT_CACHE = "pseudo.z3.persistentCache";

    /**
     * The hexadecimal digits needed for output.
     */
    private static final String HEXCHARS = "0123456789abcdef";

    /**
     * The global instance. May be null if not yet initialised or if the
     * according key is not set in the settings or if an exception occurred
     * during initialisation.
     */
    private static @Nullable Z3PersistentCache globalInstance;

    /**
     * Flag whether the global instance has already been checked for.
     *
     * This is true as soon as {@link #getGlobalInstance()} has been called.
     */
    private static boolean checkedForGlobalInstance = false;

    /**
     * The file in which the cache resides.
     * May be null if the cache is not backed by a file.
     */
    private @Nullable final File cacheFile;

    /**
     * The message digester used to compute the hash values.
     */
    private @Nullable final MessageDigest messageDigest;

    /**
     * The actual cache mapping.
     */
    private @Nullable final Map<String, Result> cache;

    /**
     * Instantiates a new z3 persistent cache.
     *
     * @param persistentCacheFileName
     *            the file name of persistent cache file, <code>null</code> if
     *            no file-backup is required.
     * @throws IOException
     *             Signals that an I/O exception has occurred during initialisation.
     * @throws NoSuchAlgorithmException
     *             Signals that the message digest cannot be instantiated.
     */
    public Z3PersistentCache(String persistentCacheFileName) throws IOException, NoSuchAlgorithmException {
        this.cacheFile = persistentCacheFileName == null ? null :
            new File(persistentCacheFileName);
        this.cache = new TreeMap<String, Result>();
        this.messageDigest = MessageDigest.getInstance("SHA-256");
        rereadFile();
    }

    /*
     * At the writeback routine as a shutdown hook to the system.
     */
    private void writebackAtExit() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    writeBack();
                } catch (IOException e) {
                    Log.log(Log.ERROR, "Cannot write back the z3 persistent cache");
                    Log.stacktrace(Log.ERROR, e);
                }
            }});
    }

    /**
     * Gets the global instance.
     *
     * This returns the global instance if it exists. This is only the case if
     * the key {@value #KEY_PERSISTENT_CACHE} has been set in the
     * {@link Settings} and if the initialisation does not fail.
     *
     * @return the global instance, or <code>null</code> if not available
     */
    public static Z3PersistentCache getGlobalInstance() {
        if(!checkedForGlobalInstance) {
            synchronized (Z3PersistentCache.class) {
                Settings settings = Settings.getInstance();
                String cacheFileName = settings.getExpandedProperty(KEY_PERSISTENT_CACHE, null);
                // check again ... we are now synchronised
                if(!checkedForGlobalInstance
                        && cacheFileName != null && cacheFileName.length() > 0) {
                    try {
                        Log.log(Log.DEBUG, "Creating global z3 persistent cache instance");
                        globalInstance = new Z3PersistentCache(cacheFileName);
                        globalInstance.writebackAtExit();
                    } catch (Exception e) {
                        Log.log(Log.ERROR, "Cannot load persistent z3 cache from " + cacheFileName);
                        Log.stacktrace(Log.ERROR, e);
                        globalInstance = null;
                    }
                }
                checkedForGlobalInstance = true;
            }
        }
        return globalInstance;
    }

    /**
     * Reread cache entries from the backstore file.
     *
     * This may result in new entries in the cache map if the file content has
     * changed externally.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void rereadFile() throws IOException {
        if(cacheFile != null && cacheFile.exists()) {
            Log.log(Log.DEBUG, "(Re)reading persistent cache");
            BufferedReader r = new BufferedReader(new FileReader(cacheFile));
            String line;
            while((line=r.readLine()) != null) {
                String[] parts = line.split(":");
                try {
                    cache.put(parts[0], Result.valueOf(parts[1]));
                } catch (Exception e) {
                    Log.log(Log.ERROR, "An error in the persistent cache file");
                    Log.stacktrace(Log.ERROR, e);
                }
            }
        }
    }

    /**
     * Write the cache back to the backstore file.
     *
     * To ensure that no entries are lost, the file is
     * <ol>
     * <li>Locked
     * <li>reread ({@link #rereadFile()})
     * <li>and only then written
     * <li>finally unlocked.
     * </ol>
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void writeBack() throws IOException {
        FileLock lock = null;
        FileChannel channel = null;

        try {
            channel = new RandomAccessFile(cacheFile, "rw").getChannel();
            lock = channel.tryLock();

            // add anything not already present!
            rereadFile();

            FileWriter w = new FileWriter(cacheFile);
            Log.log(Log.DEBUG, "Writing persistent cache");
            for (Map.Entry<String, Result> en : cache.entrySet()) {
                w.write(en.getKey() + ":" + en.getValue() + "\n");
            }
            w.close();

        } finally {
            if(lock != null) {
                lock.release();
            }
        }
    }

    /**
     * Lookup whether a challenge has a cached result.
     *
     * @param challenge
     *            the challenge to lookup in the cache.
     *
     * @return the cached result
     */
    public Result lookup(String challenge) {
        if (cache != null) {
            String hash = challengeToHash(challenge);
            Result cached = cache.get(hash);
            Log.log(Log.DEBUG, "Looked up %s in persistent cache: %s", hash, cached);
            return cached;
        }
        return null;
    }

    /**
     * sha256 hash for a challenge.
     *
     * The first line is dropped as it contains a volatile date.
     * The result is converted to a string.
     *
     * @param challenge
     *            the challenge to hash
     * @return the hash code as string
     */
    private String challengeToHash(String challenge) {
        messageDigest.reset();
        // ignore first line (with volatile date!)
        challenge = challenge.substring(challenge.indexOf('\n')+1);
        byte[] digest = messageDigest.digest(challenge.getBytes());
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < digest.length; i++) {
            sb.append(HEXCHARS.charAt((digest[i] >> 4) & 0xf));
            sb.append(HEXCHARS.charAt(digest[i] & 0xf));
        }
        return sb.toString();
    }

    /**
     * Add an entry to the cache.
     *
     * @param challenge
     *            the challenge to store
     * @param result
     *            the result to associate with the challenge.
     */
    public void put(@NonNull String challenge, @NonNull Result result) {
        if(result == Result.NOT_VALID || result == Result.VALID) {
            String hash = challengeToHash(challenge);
            cache.put(hash, result);
        }
    }

}
