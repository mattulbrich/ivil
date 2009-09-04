package de.uka.iti.pseudo.comp.rascal;

import java.util.ArrayList;
import java.util.List;

public class TokenNode extends SimpleNode {
    private Token token;

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public TokenNode(int id) {
        super(id);
    }

    public TokenNode(RascalParser p, int id) {
        super(p, id);
    }

    public String getImage() {
        return token.image;
    }

    @Override 
    public String toString() {
        return super.toString() + (token == null ? "" : (" -- " + token));
    }

    // we have only TokenNodes ...
    @Override 
    public TokenNode jjtGetChild(int i) {
        return (TokenNode) super.jjtGetChild(i);
    }
    
    @SuppressWarnings("unchecked") 
    public <T> List<T> getChildren(Class<T> type) {
        List<T> result = new ArrayList<T>();
        for(Node n : children) {
            if(type.isInstance(n))
                result.add((T) n);
        }
        return result;
    }
    
    @SuppressWarnings("unchecked") 
    public <T> T getFirstChild(Class<T> type) {
        for(Node n : children) {
            if(type.isInstance(n))
                return (T)n;
        }
        return null;
    }
    
}
