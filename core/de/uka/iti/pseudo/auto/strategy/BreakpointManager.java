package de.uka.iti.pseudo.auto.strategy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import nonnull.NonNull;

// FIXME these are mock values

public class BreakpointManager extends Observable {
    
    private Map<Object, List<Integer>> breakpointCollection =
        new HashMap<Object, List<Integer>>();

    public void addBreakpoint(@NonNull Object breakPointResource, int line) {
        List<Integer> list = breakpointCollection.get(breakPointResource);
        if(list == null) {
            list = new LinkedList<Integer>();
            breakpointCollection.put(breakPointResource, list);
        }
        
        list.add(line);
        setChanged();
        notifyObservers(breakPointResource);
    }

    public void removeBreakpoint(@NonNull Object breakPointResource, int line) {
        List<Integer> list = breakpointCollection.get(breakPointResource);
        if(list != null) {
            list.remove(Integer.valueOf(line));
            setChanged();
            notifyObservers(breakPointResource);
        }
        
    }

    public Collection<Integer> getBreakpoints(Object breakPointResource) {
        List<Integer> list = breakpointCollection.get(breakPointResource);
        if(list == null)
            return Collections.emptyList();
        else
            return Collections.unmodifiableCollection(list);
    }

    public boolean hasBreakpoint(Object breakPointResource, int line) {
        return getBreakpoints(breakPointResource).contains(line);
    }

}
