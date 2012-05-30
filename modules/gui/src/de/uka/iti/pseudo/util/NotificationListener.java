/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.util;

import nonnull.NonNull;

/**
 * The listener interface for receiving notification events. The class that is
 * interested in processing a notification event implements this interface, and
 * the object created with that class is registered with using the
 * component's <code>addNotificationListener<code> method. When
 * the notification event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see NotificationEvent
 * @see NotificationSupport
 * 
 * @author mattias ulbrich
 */
public interface NotificationListener {

    /**
     * Handle a notification event.
     * 
     * @param event
     *            the event to handle.
     */
    public void handleNotification(@NonNull NotificationEvent event);
    
}
