package de.uka.iti.pseudo.comp.rascal;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Procedure {
    
    private Token precondition;
    private Token postcondition;
    private Token modifies;
    private String name;
    private Type returnType;
    private List<Pair<String, Type>> parameters = new LinkedList<Pair<String, Type>>();
    
    public Procedure(String name) {
        this.name = name;
    }
    public Token getPrecondition() {
        return precondition;
    }
    public void setPrecondition(Token precondition) {
        this.precondition = precondition;
    }
    public Token getPostcondition() {
        return postcondition;
    }
    public void setPostcondition(Token postcondition) {
        this.postcondition = postcondition;
    }
    public Token getModifies() {
        return modifies;
    }
    public void setModifies(Token modifies) {
        this.modifies = modifies;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Type getReturnType() {
        return returnType;
    }
    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }
    public void addParameter(String param, Type type) {
        parameters.add(Pair.make(name, type));
    }
    public List<Pair<String, Type>> getParameters() {
        return parameters;
    }
    
}
