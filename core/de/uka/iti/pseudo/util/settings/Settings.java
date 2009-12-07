package de.uka.iti.pseudo.util.settings;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
 * 
 * Defaults are read from the file <code>Settings_default.properties
 * <ol>
 * <li>the settings stored in the Settings_default.properties" file in the
 * resources
 * <li>the settings returned by System.getProperty (includes all -D...=...
 * commandline options!)
 * <li>the settings loaded/set by the program.
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
 */
public class Settings extends Properties {

    private static final long serialVersionUID = -3915457392249145054L;
    
    public static final String SETTINGS_PROPERTIES_FILE_KEY = "";

    private Map<String, Object> cache = new HashMap<String, Object>();

    private static Settings theInstance = new Settings();

    /**
     * create the settings object. Load the defaults from the defaults file, add
     * load from the system directory file and the system properties.
     */
    private Settings() {
        super();
        try {
            defaults = new Properties();
            InputStream stream = getClass().getResourceAsStream("Settings_default.properties");
            defaults.load(stream);
            stream.close();
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
     * Lookup an integer value in the properties.
     * 
     * <p>
     * The number can be specified as decimal constant, octal or hexadecimal. It
     * accepts the same input as {@link Integer#decode(String)}, see there for
     * details.
     * 
     * @see Integer#decode(String)
     * @param key
     *                key to look up
     * @return integer value of the key's value
     * @throws NumberFormatException
     *                 if the value does not represent an integer
     */
    public int getInteger(String key) throws NumberFormatException {
        String value = getProperty(key);
        if (value == null)
            throw new NoSuchElementException();
        return Integer.decode(getProperty(key));
    }

    /**
     * Searches for the property with the specified key in this property list.
     * If the key is not found in this property list, the default property list,
     * and its defaults, recursively, are then checked. The method throws a
     * {@link NoSuchElementException} if the key is not present in any property
     * list.
     * 
     * @see java.util.Properties#getProperty(java.lang.String)
     *  
     * @param key
     *                the property key.
     *                
     * @return the value in this property list with the specified key value.
     * 
     * @throws NoSuchElementException
     *                 if key has not been defined
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
    
    public String getExpandedProperty(String key) {
        return expandString(getProperty(key));
    }
    
    public String getExpandedProperty(String key, String defaultValue) {
        try {
            String value = getProperty(key);
            return expandString(value);
        } catch (NoSuchElementException e) {
            return defaultValue;
        }
    }

    private String expandString(String value) {
        StringBuilder result = new StringBuilder();
        int keyStart = -1;
        
        for(int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
            case '$':
                if(i < value.length() - 1 && value.charAt(i+1) == '{' && keyStart == -1) {
                    keyStart = i + 2;
                    i++;
                } else {
                    if(keyStart == -1) {
                        result.append(c);
                    }
                }
                break;
                
            case '}':
                if(keyStart == -1) {
                    result.append(c);
                } else {
                    String key = value.substring(keyStart, i);
                    String expansion;
                    try {
                        expansion = getProperty(key);
                    } catch(NoSuchElementException ex) {
                        // log
                        System.err.println("Cannot expand ${" + key + "}, no such key defined");
                        expansion = "";
                    }
                    
                    result.append(expansion);
                    keyStart = -1;
                }
                break;

            default:
                if(keyStart == -1) {
                    result.append(c);
                }
                break;
            }
        }
        
        return result.toString();
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
//    public void loadFromSystemDirectory(String directoryKey, String fileName) throws IOException {
//        String dir = getProperty(directoryKey);
//        if(dir != null) {
//            File f = new File(dir, fileName);
//            if(f.canRead()) {
////            logger.fine("Load settings from system directory: " + f);
//                FileInputStream fileInputStream = new FileInputStream(f);
//                
//                try {
//                    defaults.load(fileInputStream);
//                } finally {
//                    if(fileInputStream != null)
//                        fileInputStream.close();
//                }
//            }
//        }
//    }

    /**
     * Load properties from a file into the settings.
     * 
     * <p>
     * The filename is obtained by querying the settings for the argument key.
     * 
     * @throws IOException
     *                 if something goes wrong while reading properties or
     *                 resolving names.
     */
    public void loadKeyAsFile(String propertiesFileKey) throws IOException {
        String fileName;
        try {
            fileName = getExpandedProperty(propertiesFileKey);
        } catch (NoSuchElementException e) {
            IOException ex = new IOException("No properties file defined for key " + propertiesFileKey);
            ex.initCause(e);
            throw ex;
        }
        
        FileInputStream fileInputStream = new FileInputStream(fileName);
        try {
            load(fileInputStream);
        } finally {
            if(fileInputStream != null)
                fileInputStream.close();
        }
    }

}
