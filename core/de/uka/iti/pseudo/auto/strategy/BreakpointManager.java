package de.uka.iti.pseudo.auto.strategy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.TreeSet;

import nonnull.NonNull;

// FIXME these are mock values

public class BreakpointManager extends Observable {
    
    private Map<Object, Set<Integer>> breakpointCollection =
        new HashMap<Object, Set<Integer>>();

    public void addBreakpoint(@NonNull Object breakPointResource, int line) {
        Set<Integer> list = breakpointCollection.get(breakPointResource);
        if(list == null) {
            list = new TreeSet<Integer>();
            breakpointCollection.put(breakPointResource, list);
        }
        
        list.add(line);
        setChanged();
        notifyObservers(breakPointResource);
    }

    public void removeBreakpoint(@NonNull Object breakPointResource, int line) {
        Set<Integer> list = breakpointCollection.get(breakPointResource);
        if(list != null) {
            list.remove(Integer.valueOf(line));
            setChanged();
            notifyObservers(breakPointResource);
        }
        
    }

    public Collection<Integer> getBreakpoints(Object breakPointResource) {
        Set<Integer> list = breakpointCollection.get(breakPointResource);
        if(list == null)
            return Collections.emptyList();
        else
            return Collections.unmodifiableCollection(list);
    }

    public boolean hasBreakpoint(Object breakPointResource, int line) {
        return getBreakpoints(breakPointResource).contains(line);
    }

}
