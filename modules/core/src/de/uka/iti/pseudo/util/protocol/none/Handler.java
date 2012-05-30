/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
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
        throw new UnsupportedOperationException("The protocol 'none' does not allow connections.");
    }
    
}