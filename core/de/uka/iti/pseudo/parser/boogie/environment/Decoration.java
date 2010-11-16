package de.uka.iti.pseudo.parser.boogie.environment;

import java.util.HashMap;

import de.uka.iti.pseudo.parser.boogie.ASTElement;

/**
 * Decorates information of Type T to ASTNodes. This behaves mostly like a map,
 * except for the fact, that annotations can only be written once.
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
    
    public void add(ASTElement key, T annotation) {
        assert(!has(key));
        data.put(key, annotation);
    }
}
