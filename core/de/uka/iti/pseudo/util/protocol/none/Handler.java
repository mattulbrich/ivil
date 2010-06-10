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
package de.uka.iti.pseudo.util.protocol.none;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler {

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        // return null;// new SystemURLConnection(u);
        throw new UnsupportedOperationException("The protocol 'none' does not allow connections.");
    }
    
    public static void registerNoneHandler() {
        System.setProperty("java.protocol.handler.pkgs", 
                "de.uka.iti.pseudo.util.protocol");
    }

}

class SystemURLConnection extends URLConnection {

    protected SystemURLConnection(URL url) {
        super(url);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void connect() throws IOException {
        // TODO Implement URLConnection.connect
        
    }
   
}
