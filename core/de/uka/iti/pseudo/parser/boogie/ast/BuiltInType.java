package de.uka.iti.pseudo.parser.boogie.ast;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

final public class BuiltInType extends NamedType {

    private final boolean isInt;
    private final boolean isBool;
    private final int bvDimension;

    public BuiltInType(Token name) {
        super(name, 0);
        
        String n = name.image;

        if (n.equals("int")) {
            isInt = true;
            isBool = false;
            bvDimension = 0;
        } else if (n.equals("bool")) {
            isInt = false;
            isBool = true;
            bvDimension = 0;
        } else if (n.startsWith("bv")) {
            isInt = isBool = false;
            bvDimension = Integer.parseInt(n.substring(2));
        } else
            throw new IllegalArgumentException("The Type " + name.image + " is not built-in.");
    }

    @Override
    public String getPrettyName() {
        if (isInt)
            return "int";
        else if (isBool)
            return "bool";
        else
            return "bv" + bvDimension;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public boolean isInt() {
        return isInt;
    }

    public boolean isBool() {
        return isBool;
    }

    public int getBvDimension() {
        return bvDimension;
    }

}
