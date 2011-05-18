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

import java.util.ArrayList;
import java.util.HashMap;
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
 * Listener can be added more than once! They will be invoked for each addition.
 * Removinng them removes all instances.
 * 
 * Upon adding or removing a listener, the list is cloned. This is done to
 * prevent concurrent modification exceptions during iterations. Since
 * adding/removing a listener is far rarer an event than notification, the
 * copying is done then.
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
    private Map<String, NotificationListener[]> listeners = new HashMap<String, NotificationListener[]>();

    /**
     * A list of listeners listening to <i>all</i> signals. The list is lazily
     * created when needed.
     */
    private NotificationListener[] totalListeners = null;

    /**
     * Instantiates a new notification support.
     * 
     * @param source
     *            the object reference used as source for all notifications
     */
    public NotificationSupport(Object source) {
        this.source = source;
    }
    
    /*
     * retrieve the list of total listeners. This method is synchronized
     * to never expose arrays under construction.
     */
    private synchronized NotificationListener[] getTotalListeners() {
        return totalListeners;
    }
    
    /*
     * retrieve the list of total listeners. This method is synchronized
     * to never expose arrays under construction.
     */
    private synchronized NotificationListener[] getListeners(String signal) {
        return listeners.get(signal);
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
    public synchronized void addNotificationListener(String signal,
            NotificationListener listener) {
        NotificationListener[] list = listeners.get(signal);
        
        if (list == null) {
            NotificationListener[] array = { listener };
            listeners.put(signal, array);
        } else {
            NotificationListener[] array = new NotificationListener[list.length + 1];
            System.arraycopy(list, 0, array, 0, list.length);
            array[list.length] = listener;
            listeners.put(signal, array);
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
    public synchronized void addNotificationListener(NotificationListener listener) {
        if (totalListeners == null) {
            totalListeners = new NotificationListener[] { listener };
        } else {
            NotificationListener[] array = new NotificationListener[totalListeners.length + 1];
            System.arraycopy(totalListeners, 0, array, 0, totalListeners.length);
            array[totalListeners.length] = listener;
            totalListeners = array;
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

        NotificationListener[] list = getListeners(signalName);

        if (list == null && totalListeners == null)
            return;

        NotificationEvent event = new NotificationEvent(source, signalName,
                parameters);

        if (list != null) {
            for (NotificationListener listener : list) {
                listener.handleNotification(event);
            }
        }

        list = getTotalListeners();
        if (list != null) {
            for (NotificationListener listener : list) {
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
    public synchronized boolean removeNotificationListener(String signal,
            NotificationListener listener) {

        NotificationListener[] array = getListeners(signal);
        if(array == null) {
            return false;
        }
        
        List<NotificationListener> list =
            new ArrayList<NotificationListener>(Util.readOnlyArrayList(array));

        boolean result = list.remove(listener);

        // if this listener has been the last store null instead of empty list.
        if (result) {
            array = Util.listToArray(list, NotificationListener.class);
            listeners.put(signal, array);
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
    public synchronized boolean removeNotificationListener(NotificationListener listener) {
        
        NotificationListener[] array = getTotalListeners();
        if(array == null) {
            return false;
        }
        
        List<NotificationListener> list =
            new ArrayList<NotificationListener>(Util.readOnlyArrayList(array));

        boolean result = list.remove(listener);

        // if this listener has been the last store null instead of empty list.
        if (result) {
            array = Util.listToArray(list, NotificationListener.class);
            totalListeners = array;
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
    public synchronized boolean removeNotificationListenerCompletely(
            NotificationListener listener) {

        boolean result = removeNotificationListener(listener);

        for (String signalName : listeners.keySet()) {
            result |= removeNotificationListener(signalName, listener);
        }

        return result;
    }

}
