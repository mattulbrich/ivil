/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * A BufferOutputStream is a simple variant of a {@link ByteArrayOutputStream}
 * which allows accessing the underyling data via a {@link ByteArrayInputStream}
 * , thus reducing the amount of needed memory.
 * 
 * It adds its functionality in the method {@link #inputStream()}.
 * 
 * The input stream reads directly from the byte array of the stream. It reads
 * only up to current filling level of the buffer. Any bytes added later will
 * not be read.
 * 
 * <b>Please note:</b> If this stream is {@linkplain #reset() reset} to its
 * inital state, the behaviour of the resulting input stream changes, too!
 * 
 * @author mattias ulbrich
 */
public class BufferOutputStream extends ByteArrayOutputStream {

    /**
     * Create a fresch input stream which reads the buffer of this stream up to
     * its current filling level.
     * 
     * @return a freshly created {@link ByteArrayInputStream}.
     */
    public InputStream inputStream() {
        return new ByteArrayInputStream(buf, 0, count);
    }

}
