/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A class to facilitate the handling of {@link NotificationListener}s and
 * processing of {@link NotificationEvent}s.
 * 
 * Objects of this class keep a table of notification listeners and allow to
 * issue new notifications using the method
 * {@link #fireNotification(String, Object...)}.
 * 
 * @see NotificationEvent
 * @see NotificationListener
 * 
 * @author mattias ulbrich
 */
public final class NotificationSupport {

    /**
     * The object reference to be used as source for all notifications.
     */
    private Object source;

    /**
     * The table of listeners: A map from signal names to registered listeners.
     */
    private Map<String, List<NotificationListener>> listeners = new HashMap<String, List<NotificationListener>>();

    /**
     * A list of listeners listening to <i>all</i> signals. The list is lazily
     * created when needed.
     */
    private List<NotificationListener> totalListeners = null;

    /**
     * Instantiates a new notification support.
     * 
     * @param source
     *            the object reference used as source for all notifications
     */
    public NotificationSupport(Object source) {
        this.source = source;
    }

    /**
     * Adds a notification listener for a particular signal.
     * 
     * The listener will be invoked on notifications with the given signal.
     * 
     * @param signal
     *            the signal name to listen for
     * @param listener
     *            the listener
     */
    public void addNotificationListener(String signal,
            NotificationListener listener) {
        List<NotificationListener> list = listeners.get(signal);
        if (list == null) {
            list = new LinkedList<NotificationListener>();
            listeners.put(signal, list);
        }

        if (!list.contains(listener)) {
            list.add(listener);
        }
    }

    /**
     * Adds a total notification listener.
     * 
     * This listener gets invoked on any notification.
     * 
     * @param listener
     *            the listener
     */
    public void addNotificationListener(NotificationListener listener) {
        if (totalListeners == null) {
            totalListeners = new LinkedList<NotificationListener>();
        }

        if (!totalListeners.contains(listener)) {
            totalListeners.add(listener);
        }
    }

    /**
     * Fire a notification event.
     * 
     * All listeners registered for this particular signal will have their
     * {@link NotificationListener#handleNotification(NotificationEvent)} method
     * invoked.
     * 
     * Additionally, all listeners registered totally (i.e., not for a
     * particular signal) will be notified as well.
     * 
     * The generated notification event will use the source specified in the
     * constructor of this object.
     * 
     * @param signalName
     *            the name of the signal to be notified
     * @param parameters
     *            the parameters of the notification
     */
    public void fireNotification(String signalName, Object... parameters) {

        List<NotificationListener> list = listeners.get(signalName);

        if (list == null && totalListeners == null)
            return;

        NotificationEvent event = new NotificationEvent(source, signalName,
                parameters);

        if (list != null) {
            for (NotificationListener listener : list) {
                listener.handleNotification(event);
            }
        }

        if (totalListeners != null) {
            for (NotificationListener listener : totalListeners) {
                listener.handleNotification(event);
            }
        }
    }

    /**
     * Removes a notification listener for a particular signal.
     * 
     * <p>
     * <i>Please note:</i> The listener may still be registered for other
     * signals/as total listener. Those registrations will not be removed.
     * 
     * @param signal
     *            the name of the signal
     * @param listener
     *            the listener
     * 
     * @return true, if successfully removed, false, if nothing changed
     */
    public boolean removeNotificationListener(String signal,
            NotificationListener listener) {

        List<NotificationListener> list = listeners.get(signal);

        if (list == null) {
            return false;
        }

        boolean result = list.remove(listener);

        // if this listener has been the last store null instead of empty list.
        if (result && list.isEmpty()) {
            listeners.put(signal, null);
        }

        return result;

    }

    /**
     * Removes a total notification listener.
     * 
     * <p>
     * <i>Please note:</i> The listener may still be registered for individual
     * signals. Those registrations will not be removed.
     * 
     * @param listener
     *            the listener
     * 
     * @return true, if successfully removed, false if nothing has been changed.
     */
    public boolean removeNotificationListener(NotificationListener listener) {
        if (totalListeners == null)
            return false;

        boolean result = totalListeners.remove(listener);

        // if this listener has been the last store null instead of empty list.
        if (result && totalListeners.isEmpty()) {
            totalListeners = null;
        }

        return result;
    }

    /**
     * Removes a notification listener completely, i.e., from all registered
     * signals and totally, if registered
     * 
     * @param listener
     *            the listener to remove.
     * 
     * @return true, if successfully removed from at least one list; false if
     *         nothing has been changed.
     */
    public boolean removeNotificationListenerCompletely(
            NotificationListener listener) {

        boolean result = removeNotificationListener(listener);

        for (String signalName : listeners.keySet()) {
            result |= removeNotificationListener(signalName, listener);
        }

        return result;
    }

}
