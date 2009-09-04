package de.uka.iti.pseudo.comp.rascal;

public class TypeMaker extends DefaultVisitor {

    @Override 
    protected Object visitDefault(Node node, Object arg) {
        throw new IllegalArgumentException("I should not be called on " + node);
    }

    @Override 
    public Object visit(ASTIdentifier node, Object arg) {
        return new IdentifierType(node.getImage());
    }
    
    @Override 
    public Object visit(ASTArrayType node, Object arg) {
        Type wrapped = (Type)node.jjtGetChild(0).jjtAccept(this, null);
        return new ArrayType(wrapped);
    }
    
}
