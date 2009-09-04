package de.uka.iti.pseudo.comp.rascal;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RegisterBank {
    
    private Set<String> usedRegisters = new HashSet<String>();
    private Map<String, Integer> usedPerType = new HashMap<String, Integer>();
    private Environment env;

    public RegisterBank(Environment env) {
        this.env = env;
    }
    
    public void clear() {
        usedPerType.clear();
    }
    
    public String getNewRegister(String type) {
        
        Integer no = usedPerType.get(type);
        if(no == null)
            no = 0;
        no++;
        usedPerType.put(type, no);
        
        String name = "$R" + type + no;  
        usedRegisters.add(type + " " + name);
        
        return name;
    }

    public void dump(PrintWriter pw) {
        pw.println("(* Temporary variables *)");
        pw.println("function");
        for (String reg : usedRegisters) {
            pw.println("   " + reg + " assignable");
        }
        pw.println();
    }

}
