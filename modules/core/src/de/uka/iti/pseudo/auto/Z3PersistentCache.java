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
import java.util.Map;
import java.util.TreeMap;

import nonnull.Nullable;
import de.uka.iti.pseudo.auto.DecisionProcedure.Result;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.Pair;
import de.uka.iti.pseudo.util.settings.Settings;

// TODO DOC
public class Z3PersistentCache {

    private static final String HEXCHARS = "0123456789abcdef";

    private static Z3PersistentCache instance;

    private @Nullable File cacheFile;
    private @Nullable MessageDigest messageDigest;
    private @Nullable Map<String, Result> cache = null;

    private final Thread shutdownHook = new Thread() {
        @Override
        public void run() {
            FileLock lock = null;
            FileChannel channel = null;
            try {
                channel = new RandomAccessFile(cacheFile, "rw").getChannel();
                lock = channel.tryLock();

                rereadCache();

                FileWriter w = new FileWriter(cacheFile);
                Log.log(Log.DEBUG, "Writing persistent cache");
                for (Map.Entry<String, Result> en : cache.entrySet()) {
//                    System.err.println(en.getKey() + ":" + en.getValue());
                    w.write(en.getKey() + ":" + en.getValue() + "\n");
                }
                w.close();

            } catch (Exception e) {
                Log.log(Log.WARNING, "Cannot save persistent cache to " + cacheFile);
                Log.stacktrace(Log.WARNING, e);
            } finally {
                try {
                    if(lock != null) {
                        lock.release();
                    }
                } catch (IOException e) {
                    Log.log(Log.WARNING, "Cannot save persistent cache to " + cacheFile);
                    Log.stacktrace(Log.WARNING, e);
                }
            }
        }
    };

    public Z3PersistentCache() {
        String persistentCacheFileName =
                Settings.getInstance().getExpandedProperty("pseudo.z3.persistentCache", null);
        if(persistentCacheFileName != null) {
            this.cacheFile = new File(persistentCacheFileName);
            this.cache = new TreeMap<String, Result>();
            try {
                rereadCache();
                messageDigest = MessageDigest.getInstance("SHA-256");
                Runtime.getRuntime().addShutdownHook(shutdownHook);
            } catch (Exception e) {
                cache = null;
                this.cacheFile = null;
                Log.log(Log.WARNING, "Cannot load persistent cache from " + cacheFile);
                Log.stacktrace(Log.WARNING, e);
            }
        } else {
            this.cacheFile = null;
            this.cache = null;
        }
    }

    public static Z3PersistentCache getInstance() {
        if(instance == null) {
            instance = new Z3PersistentCache();
        }
        return instance;
    }

    public void rereadCache() throws IOException {
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

    public Result lookup(String challenge) {
        if (cache != null) {
            // ignore first line (with volatile date!)
            challenge = challenge.substring(challenge.indexOf('\n')+1);
            String hash = toHash(challenge);
            Result cached = cache.get(hash);
            Log.log(Log.DEBUG, "Looked up %s in persistent cache: %s", hash, cached);
            return cached;
        }
        return null;
    }

    private String toHash(String challenge) {
        messageDigest.reset();
        byte[] digest = messageDigest.digest(challenge.getBytes());
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < digest.length; i++) {
            sb.append(HEXCHARS.charAt((digest[i] >> 4) & 0xf));
            sb.append(HEXCHARS.charAt(digest[i] & 0xf));
        }
        return sb.toString();
    }

    public void inform(String challenge, Pair<Result, String> resultPair) {
        Result result = resultPair.fst();
        if(result == Result.NOT_VALID || result == Result.VALID) {
            String hash = toHash(challenge);
            cache.put(hash, result);
        }
    }

}
