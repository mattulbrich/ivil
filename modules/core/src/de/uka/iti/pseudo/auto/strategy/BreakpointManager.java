/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto.strategy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.TreeSet;

import nonnull.DeepNonNull;
import nonnull.NonNull;

/**
 * This class is used to administrate breakpoints for all possible entities.
 * This includes breakpoints on source code and on ivil code level.
 * 
 * <p>
 * A breakpoint consists of a pair of an Object and an integer with the object
 * denoting the resource into which it points and the integer the
 * statement/source line it points to.
 * 
 * Breakpoints can be {@link #addBreakpoint(Object, int) added} and
 * {@link #removeBreakpoint(Object, int) removed} from the collections. A
 * resource/number pair can be {@link #hasBreakpoint(Object, int) queried} for
 * having a breakpoint.
 * 
 * The class implements the {@link Observable} interface, all modifications can
 * be subscribed to. The method {@link Observer#update(Observable, Object)} we
 * be called with the resource as argument.
 * 
 * This implementation is not threadsafe.
 */
public final class BreakpointManager extends Observable {

    /**
     * The collection of all defined breakpoints.
     * 
     * The key to the map is the resource and the values is a set of set
     * breakpoint locations.
     */
    private Map<Object, Set<Integer>> breakpointCollection =
            new HashMap<Object, Set<Integer>>();

    /**
     * Adds a breakpoint to the collection. All listeners are notified if the
     * breakpoint has not already been set.
     * 
     * @param breakPointResource
     *            the resource to break in.
     * @param line
     *            the line number / statment number / location to break at.
     */
    public void addBreakpoint(@NonNull Object breakPointResource, int line) {
        Set<Integer> list = breakpointCollection.get(breakPointResource);
        if(list == null) {
            list = new TreeSet<Integer>();
            breakpointCollection.put(breakPointResource, list);
        }

        Integer lineObject = line;
        boolean isNew = list.contains(lineObject);
        list.add(lineObject);

        if(isNew) {
            setChanged();
            notifyObservers(breakPointResource);
        }
    }

    /**
     * Removes a breakpoint from the collection. All listeners are notified if a
     * breakpoint had been set at the given location. Has no effect if the
     * breakpoint has not been set.
     * 
     * @param breakPointResource
     *            the resource to break in.
     * @param line
     *            the line number / statment number / location to break at.
     */
    public void removeBreakpoint(@NonNull Object breakPointResource, int line) {
        Set<Integer> list = breakpointCollection.get(breakPointResource);
        if(list != null) {
            boolean hasBeenPresent = list.remove(Integer.valueOf(line));
            if(hasBeenPresent) {
                setChanged();
                notifyObservers(breakPointResource);
            }
        }

    }

    /**
     * Gets all breakpoints for a resource.
     * 
     * @param breakPointResource
     *            the resource to lookup
     * @return an immutable collection of locations in the resource.
     */
    public @DeepNonNull Collection<Integer> getBreakpoints(Object breakPointResource) {
        Set<Integer> list = breakpointCollection.get(breakPointResource);
        if(list == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableCollection(list);
        }
    }

    /**
     * Checks for a breakpoint at a given location.
     * 
     * @param breakPointResource
     *            the resource to check in
     * @param line
     *            the line number / statement number / location to check
     * @return <code>true</code> iff a breakpoint has been previously set at
     *         this location.
     */
    public boolean hasBreakpoint(@NonNull Object breakPointResource, int line) {
        return getBreakpoints(breakPointResource).contains(line);
    }

}
