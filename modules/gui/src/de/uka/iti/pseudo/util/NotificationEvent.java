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

import java.beans.PropertyChangeEvent;
import java.util.EventObject;

import nonnull.NonNull;

/**
 * A NotificationEvent get delivered when an action on the semantic level has
 * been triggered.
 * 
 * <p>
 * Notifications are named, the names are called "signals".
 * {@link NotificationListener} can usually be configured to listen only to
 * certain signals.
 * 
 * <p>
 * Notifications may also carry parameters, which are 0 or more Object
 * references.
 * 
 * <p>
 * <i> Please note:</i> If the event is recorded in a state change (the change
 * of the value of a property), a {@link PropertyChangeEvent} should be issued
 * instead.
 * 
 * @see NotificationListener
 * @see NotificationSupport
 * 
 * @author mattias ulbrich
 */
public class NotificationEvent extends EventObject {

    /**
     * The Serialisation Constant.
     */
    private static final long serialVersionUID = -4350007839854109727L;

    /**
     * The name of the signal of this event.
     */
    private @NonNull
    String signal;

    /**
     * The parameters of this signal. May be an empty array.
     */
    private @NonNull
    Object[] parameters;

    /**
     * Instantiates a new notification event.
     * 
     * @param source
     *            the source of the event.
     * @param signal
     *            the name of the contained signal
     * @param parameters
     *            the parameters of the event
     */
    public NotificationEvent(@NonNull Object source, @NonNull String signal,
            Object... parameters) {
        super(source);
        this.signal = signal;
        this.parameters = parameters;
    }

    /**
     * Retrieves the name of the signal of this event.
     * 
     * @return the name of the signal
     */
    public @NonNull
    String getSignal() {
        return signal;
    }

    /**
     * Retrieves one of the parameters of the event.
     * 
     * @param index
     *            the index of the selected parameter
     * @return the selected parameter
     * @throws IndexOutOfBoundsException
     *             if <code>index</code> is &lt; 0 or &ge;
     *             {@link #countParameters()}.
     */
    public Object getParameter(int index) {
        if (index < 0 || index >= parameters.length)
            throw new IndexOutOfBoundsException("The event has only "
                    + countParameters() + " parameters, invalid index: "
                    + index);
        return parameters[index];
    }

    /**
     * Gets the number of parameters specified for this event.
     * 
     * @return the number of parameters for this event.
     */
    public int countParameters() {
        return parameters.length;
    }

    /**
     * Checks if this evnet is of a certain signal.
     * 
     * @param signal
     *            the name of the signal to check for
     * 
     * @return true, if is this event is of the specified signal
     */
    public boolean isSignal(@NonNull String signal) {
        return this.signal.equals(signal);
    }
    
    @Override
    public String toString() {
        return "NotificationEvent('" + signal + "')[" + 
            Util.commatize(Util.readOnlyArrayList(parameters)) + "]";
    }
}
