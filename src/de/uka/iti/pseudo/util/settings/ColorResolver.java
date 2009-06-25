package de.uka.iti.pseudo.util.settings;

import java.awt.Color;
import java.io.*;
import java.util.HashMap;

/**
 * Singleton to be used to resolve color names to Color objects.
 * 
 * Color names are read from a colors.properties file.
 * 
 * @author mattze
 * 
 */

public class ColorResolver {

    private static ColorResolver defaultInstance;

    private HashMap<String, Integer> lookuptable;
    private HashMap<Integer, Color> cache;

    private ColorResolver() throws IOException {
        lookuptable = new HashMap<String, Integer>();
        cache = new HashMap<Integer, Color>();
        load(getClass().getResourceAsStream("colors.properties"));
    }

    /**
     * get the instance of the colorresolver.
     * 
     * @return the color resolver, null if resource is unavailable.
     */
    public static ColorResolver getInstance() {
        if (defaultInstance == null) {
            try {
                defaultInstance = new ColorResolver();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return defaultInstance;
    }

    /**
     * resolve a String to a color.
     * 
     * Valid color names are either the ones stored in "colors.properties" or an
     * integer constant in either decimal or hexadecimal format.
     * 
     * Results are stored in a cache table.
     * 
     * @param colorString
     *            a name of a color or a string containing an integer
     * @return a Color object, possibly cached, null if the named color has not
     *         been found
     */
    public Color resolve(String colorString) {
        try {

            Integer entry = lookuptable.get(colorString);
            if (entry == null) {
                try {
                    entry = Integer.decode(colorString);
                } catch (NumberFormatException ex) {}
            }
            
            if(entry == null)
                return null;

            Color c = cache.get(entry);
            if(c == null) {
                c = new Color(entry);
                cache.put(entry, c);
            }
            
            return c;

        } catch (Exception ex) {
            throw new RuntimeException("colors.properties has bad format!", ex);
        }
    }

    private void load(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line = br.readLine();
        while (line != null) {
            line = line.trim();
            if (line.length() == 0 || line.charAt(0) == '#') {
                line = br.readLine();
                continue;
            }

            String[] parts = line.split(" += +");
            int intVal = Integer.decode(parts[1]);
            lookuptable.put(parts[0], intVal);
            line = br.readLine();
        }
    }
}
