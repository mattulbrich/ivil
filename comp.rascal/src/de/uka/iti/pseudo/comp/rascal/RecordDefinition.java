package de.uka.iti.pseudo.comp.rascal;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class RecordDefinition implements Iterable<Entry<String,Type>>{
    
    private String name;

    private Map<String, Type> fields = new LinkedHashMap<String, Type>();
    
    void addField(String id, Type type) {
        fields.put(id, type);
    }

    public RecordDefinition(String name) {
        super();
        this.name = name;
    }
       
    @Override public String toString() {
        return "RECORD[" + name + "(" + fields.size() + ")]";
    }
    
    public void dump(PrintWriter pw) {
        pw.println("TYPE " + name + " = RECORD");
        for (Entry<String, Type> entry : fields.entrySet()) {
            pw.println("    " + entry.getKey() + " : " + entry.getValue());
        }
        pw.println("  END;");
    }
    
    public Iterator<Entry<String,Type>> iterator() {
        return fields.entrySet().iterator();
    }
}
