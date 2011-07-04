package de.uka.iti.pseudo.algo;

import java.util.HashMap;
import java.util.Map;

public class IdentifierProducer {

    private Map<String, Integer> counterMap = new HashMap<String, Integer>();
    
    public String makeIdentifier(String type) {
        
        Integer counter = counterMap.get(type);
        if(counter == null)
            counter = 0;
        
        String result = type + counter;
        
        counterMap.put(type, counter+1);
        
        return result;
    }
    
}
