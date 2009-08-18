package de.uka.iti.pseudo.util.settings;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * Settings are "pimped {@link Properties}".
 * 
 * Only they provide a little more getter-methods.
 * 
 * If a property is looked up, the order is the following:
 * <ol>
 * <li>the settings made on the command line
 * <li>the settings file "jatc.properties" in the system directory.
 * <li>the settings returned by System.getProperty (includes all -D...=...
 * commandline options!)
 * <li>the settings stored in the Settings_default.properites" file in the
 * resources
 * </ol>
 * 
 * Settings are used for settings on a per-user level. Other info should be
 * stored in szenario files.
 * 
 * Store results in caches for the conversion may be costly.
 * 
 * For convenience also store all keys in here.
 * 
 * This is a singleton.
 * 
 * @author mattze
 * 
 */
public class Settings extends Properties {

    private static final long serialVersionUID = -3915457392249145054L;

    private Map<String, Object> cache = new HashMap<String, Object>();

    private static Settings theInstance = new Settings();

    /**
     * create the settings object. Load the defaults from the defaults file, add
     * the system properties and load from the system directory file.
     */
    private Settings() {
        super();
        try {
            defaults = new Properties();
            defaults.load(getClass().getResourceAsStream(
                    "Settings_default.properties"));
            defaults.putAll(System.getProperties());
        } catch (Exception ex) {
            ex.printStackTrace();
            defaults = null;
        }
    }

    public static Settings getInstance() {
        return theInstance;
    }

    /*
     * put is overwritten to allow logging!
     */
    public synchronized Object put(Object key, Object value) {
//        if (logger.isLoggable(Level.CONFIG))
//            logger.config("Settings updated: " + key + " -> " + value);
        return super.put(key, value);
    }

    /*
     * check whether a key is present in the cache and if the valuei is of class
     * clss.
     */
    private boolean hasCache(String key, Class<?> clss) {
        return cache.containsKey(key) && clss.isInstance(cache.get(key));
    }

    /**
     * lookup a value in the properties and use its value as a class name and
     * create a new instance of that class
     * 
     * @param key
     *            key for the classname
     * @return new instance of the class of that class name
     * @throws InstantiationException
     *             instantiation fails
     * @throws IllegalAccessException
     *             instantiation fails
     * @throws ClassNotFoundException
     *             instantiation fails
     */
    public Object createInstance(String key) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        String value = getProperty(key);
        if (value == null)
            throw new NoSuchElementException();
        Class<?> clss = Class.forName(value);
        return clss.newInstance();
    }

    /**
     * lookup an integer value in the properties. Accepts different bases.
     * 
     * @see Integer#decode(String)
     * @param key
     *            key to look up
     * @return integer value of the key's value
     * @throws NumberFormatException
     *             if the value does not represent an integer
     */
    public int getInteger(String key) throws NumberFormatException {
        String value = getProperty(key);
        if (value == null)
            throw new NoSuchElementException();
        return Integer.decode(getProperty(key));
    }

    /**
     * same as super class but throw {@link NoSuchElementException} if the key
     * is not present.
     * 
     * @see java.util.Properties#getProperty(java.lang.String)
     * @throws NoSuchElementException
     *             if key has not been defined
     */
    public String getProperty(String key) throws NoSuchElementException {
        if (!containsKey(key) && !defaults.containsKey(key))
            throw new NoSuchElementException();
        return super.getProperty(key);
    }
    
    // TODO
    public String getProperty(String key, String defaultValue) {
        try {
            return getProperty(key);
        } catch (NoSuchElementException e) {
            return defaultValue;
        }
    }

    // / @todo DOC
    public String[] getStrings(String key) {
        String value = getProperty(key);
        return value.split(", *");
    }

    /**
     * return the boolean property that is stored in key.
     * If there is no such property false is returned.
     * 
     * @param key the key to lookup
     * @return true iff key stored and has the value "true"
     */
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }
    
    /**
     * return the boolean property that is stored in key.
     * If there is no such property defValue is returned.
     * 
     * @param key the key to lookup
     * @param defValue the default value to return if the
     * key is not defined
     * @return true iff key stored and has the value "true"
     */
    public boolean getBoolean(String key, boolean defValue) {
        String value = getProperty(key);
        if(value == null)
            return defValue;
        else
            return value.equalsIgnoreCase("true");
    }

    // @todo DOC throws!
    public double getDouble(String key) {
        String value = getProperty(key);
        return Double.parseDouble(value);
    }

    /**
     * Use the {@link ColorResolver} to resolve a color name or id.
     * 
     * @param key
     *            key to look up
     * @return a Color object
     * @throws NoSuchElementException
     *             if there is no value for key.
     */
    public Color getColor(String key) {

        if (!hasCache(key, Color.class)) {
            cache.put(key, ColorResolver.getInstance()
                    .resolve(getProperty(key)));
        }

        return (Color) cache.get(key);
    }

    /**
     * get a Font object for a key.
     * 
     * "Fontname, style, size" is the format. "Arial, PLAIN, 12" for instance.
     * 
     * Styles are: PLAIN, BOLD, ITALIC, BOLD+ITALIC. (not ITALIC+BOLD)
     * 
     * @param key
     *            key to look up
     * @return a Font object
     * @throws NoSuchElementException
     *             if there is no value for key.
     */
    public Font getFont(String key) {
        if (!hasCache(key, Font.class)) {
            String[] strings = getStrings(key);
            String fontname = strings[0];
            int style = decodeStyle(strings[1]);
            int size = Integer.decode(strings[2]);
            cache.put(key, new Font(fontname, style, size));
        }

        return (Font) cache.get(key);
    }

    private int decodeStyle(String string) {
        if (string.equals("PLAIN"))
            return Font.PLAIN;
        if (string.equals("BOLD"))
            return Font.BOLD;
        if (string.equals("ITALIC"))
            return Font.ITALIC;
        if (string.equals("BOLD+ITALIC"))
            return Font.ITALIC + Font.BOLD;

        throw new RuntimeException("No valid font style: " + string);
    }

    /**
     * once the command line args have been parsed we can now load the settings
     * to the defaults - not overwriting set arguments
     * 
     * @throws IOException
     *             file not found or reading error
     */
    public void loadFromSystemDirectory(String directoryKey, String fileName) throws IOException {
        String dir = getProperty(directoryKey);
        if(dir != null) {
            File f = new File(dir, fileName);
            if(f.canRead()) {
//            logger.fine("Load settings from system directory: " + f);
                defaults.load(new FileInputStream(f));
            }
        }
    }

}
