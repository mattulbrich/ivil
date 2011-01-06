package de.uka.iti.pseudo.environment.boogie;

import java.util.Collection;
import java.util.HashMap;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;

/**
 * Decorates information of Type T to ASTNodes. This behaves mostly like a map,
 * except for the fact, that annotations can only be written once. <b>
 * 
 * Note: ASTVisitException is used instead of ParseException, as the decorations
 * should be used and created by ASTVisitors, which use ASTVisitExceptions to
 * signal errors.
 * 
 * @author timm.felden@felden.com
 * 
 * @param <T>
 *            Type of whats annotated to the Decoration
 */
public final class Decoration<T> {
    
    private final HashMap<ASTElement, T> data = new HashMap<ASTElement, T>();
    
    public boolean has(ASTElement key){
        return data.containsKey(key);
    }
    
    public T get(ASTElement key) {
        assert (has(key));
        return data.get(key);
    }

    /**
     * This method tries to find value in O(data.size()) time
     * 
     * @param value
     *            value to be searched for
     * @return the ASTElement e, that will satisfy "value == get(e)" or null, if
     *         no such element exists. Its up to the creator of the Decoration
     *         to ensure the returned value is useful
     */
    public ASTElement find(T value){
        for (ASTElement e : data.keySet())
            if (data.get(e).equals(value))
                return e;

        return null;
    }

    public void add(ASTElement key, T annotation) throws ASTVisitException {
        assert !has(key);

        data.put(key, annotation);
    }

    public Collection<T> valueSet() {
        return data.values();
    }

    public int size() {
        return data.size();
    }
}
