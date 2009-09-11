package de.uka.iti.pseudo.comp.rascal;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Environment {

    Map<String, RecordDefinition> recordMap = new HashMap<String, RecordDefinition>();
    
    Map<String, Procedure> contractMap = new HashMap<String, Procedure>();
    
    Map<String, Type> varMap = new HashMap<String, Type>();
    
    List<Token> invariants = new ArrayList<Token>();
    
    public void exportPseudo(PrintWriter pw) {

        if(!varMap.isEmpty()) {
            pw.println("(* Variables *)");
            pw.println("function");
            for (Entry<String, Type> entry : varMap.entrySet()) {
                pw.println("   " + entry.getValue().toSimpleType() + " " + entry.getKey() + " assignable");
            }
            pw.println();
        }

        // TODO what if only empty records?
        if(!recordMap.isEmpty()) {
            pw.println("(* Record fields *)");
            pw.println("function");
            for (Entry<String, RecordDefinition> entry : recordMap.entrySet()) {
                String rec = entry.getKey();
                RecordDefinition def = entry.getValue();
                for (Entry<String,Type> field : def) {
                    pw.println("   field(" + field.getValue().toSimpleType() + ") " + 
                            rec + "_" + field.getKey() + " unique");
                }
            }
            pw.println();
        }
        
    }
    
    public void dumpContracts(PrintWriter pw) {
        
        pw.println("Invariants:");
        for (Token inv : invariants) {
            pw.println("   " + formatToken(inv));
        }
        
        for (Procedure c : contractMap.values()) {
            pw.println();
            pw.println("Contract for " + c.getName());
            pw.println("   Pre:      " + formatToken(c.getPrecondition()));
            pw.println("   Post:     " + formatToken(c.getPostcondition()));
            pw.println("   Modifies: " + formatToken(c.getModifies()));
        }
    }
    
    private String formatToken(Token t) {
        if(t == null)
            return "---";
        else
            return "line " + t.beginLine + ": " + t.image;
    }
    
}
